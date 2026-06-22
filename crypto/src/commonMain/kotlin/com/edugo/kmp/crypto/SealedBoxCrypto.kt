@file:OptIn(ExperimentalUnsignedTypes::class)

package com.edugo.kmp.crypto

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.box.Box
import com.ionspin.kotlin.crypto.box.BoxCorruptedOrTamperedDataException
import com.ionspin.kotlin.crypto.box.crypto_box_PUBLICKEYBYTES
import com.ionspin.kotlin.crypto.box.crypto_box_SECRETKEYBYTES

/**
 * Par de claves X25519 de un dispositivo (`Kd`).
 *
 * Ambas claves miden 32 bytes ([crypto_box_PUBLICKEYBYTES] / [crypto_box_SECRETKEYBYTES]).
 * La pública se publica al backend (`POST /devices/link`); la privada **nunca sale del dispositivo**
 * (custodia en Keystore/Keychain — esa capa la añade F5.2, no este módulo).
 */
data class DeviceKeyPair(
    val publicKey: ByteArray,
    val privateKey: ByteArray,
) {
    // ByteArray no implementa equals/hashCode por contenido; lo hacemos explícito para que dos pares
    // con el mismo material sean iguales (útil en tests y comparaciones).
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as DeviceKeyPair
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!privateKey.contentEquals(other.privateKey)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + privateKey.contentHashCode()
        return result
    }
}

/**
 * Se lanza cuando [SealedBoxCrypto.open] no puede descifrar/autenticar un sello: clave equivocada,
 * datos corruptos o manipulados. Es la traducción de la
 * [BoxCorruptedOrTamperedDataException] de libsodium a un tipo propio del ecosistema.
 *
 * Esta es una primitiva de bajo nivel: la capa de negocio que la consuma (p.ej. el repositorio de
 * mensajería) es la responsable de envolver el resultado en `Result<T>` según las convenciones del
 * proyecto. No propagar esta excepción más allá de esa frontera.
 */
class CryptoOpenException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Sellado anónimo (`crypto_box_seal`) de libsodium para la mensajería EduGo.
 *
 * Equivale exactamente a `box.SealAnonymous` de `golang.org/x/crypto/nacl/box` en el backend Go:
 * X25519 + XSalsa20-Poly1305, sello **anónimo** (sin clave del emisor; el receptor no sabe quién
 * selló). El layout del blob sellado es `eph_pk(32) ‖ ciphertext ‖ tag(16)`, con un overhead de
 * **48 bytes** sobre el plaintext (una DEK de 32 B → 80 B sellados). Al usar la misma libsodium C
 * subyacente, el formato es byte-compatible entre Go y Kotlin en ambos sentidos.
 *
 * Trabaja con `ByteArray` **puro** (sin base64): el encoding base64 lo hace la capa HTTP, no aquí.
 *
 * **Gotcha de inicialización:** libsodium (Ionspin) exige llamar a [ensureInitialized] una vez antes
 * de usar cualquier primitiva (carga la librería nativa). Todas las funciones de este objeto lo hacen
 * por sí mismas, pero como [ensureInitialized] es `suspend`, el resto de funciones también lo son.
 */
object SealedBoxCrypto {

    /**
     * Overhead en bytes que [seal] añade al plaintext: `eph_pk(32) + tag(16) = 48`.
     * Espejo de `crypto_box_SEALBYTES`.
     */
    const val SEAL_OVERHEAD_BYTES: Int = 48

    /** Longitud en bytes de las claves pública/privada X25519 (32). */
    const val KEY_BYTES: Int = 32

    /**
     * Inicializa libsodium de forma idempotente.
     *
     * En la JVM esto carga la librería nativa (JNA); en iOS enlaza la libsodium estática del binding.
     * Llamar más de una vez recargaría la nativa innecesariamente, así que cortocircuitamos con
     * [LibsodiumInitializer.isInitialized]. Seguro de invocar repetidamente y desde varias funciones.
     */
    suspend fun ensureInitialized() {
        if (!LibsodiumInitializer.isInitialized()) {
            LibsodiumInitializer.initialize()
        }
    }

    /**
     * Genera un par de claves X25519 (`crypto_box_keypair`) para identificar un dispositivo.
     *
     * @return [DeviceKeyPair] con pública/privada de 32 B cada una.
     */
    suspend fun generateDeviceKeyPair(): DeviceKeyPair {
        ensureInitialized()
        val keyPair = Box.keypair()
        return DeviceKeyPair(
            publicKey = keyPair.publicKey.toByteArray(),
            privateKey = keyPair.secretKey.toByteArray(),
        )
    }

    /**
     * Sella [plaintext] de forma anónima hacia [recipientPublicKey] (`crypto_box_seal`).
     *
     * Equivalente a `box.SealAnonymous` en Go. El resultado mide `plaintext.size + `
     * [SEAL_OVERHEAD_BYTES].
     *
     * @param recipientPublicKey clave pública X25519 del receptor (32 B).
     * @param plaintext datos a sellar (p.ej. la DEK de 32 B).
     * @return blob sellado `eph_pk(32) ‖ ciphertext ‖ tag(16)`.
     */
    suspend fun seal(recipientPublicKey: ByteArray, plaintext: ByteArray): ByteArray {
        ensureInitialized()
        require(recipientPublicKey.size == KEY_BYTES) {
            "recipientPublicKey debe medir $KEY_BYTES bytes, midió ${recipientPublicKey.size}"
        }
        return Box.seal(plaintext.toUByteArray(), recipientPublicKey.toUByteArray()).toByteArray()
    }

    /**
     * Abre un blob [sealed] sellado hacia [recipientPublicKey] usando [recipientPrivateKey]
     * (`crypto_box_seal_open`).
     *
     * @param recipientPublicKey clave pública X25519 del receptor (32 B).
     * @param recipientPrivateKey clave privada X25519 del receptor (32 B).
     * @param sealed blob producido por [seal] o por `box.SealAnonymous` en Go.
     * @return el plaintext recuperado.
     * @throws CryptoOpenException si el sello no descifra/autentica (clave equivocada o datos
     *   manipulados). La capa de negocio debe traducirlo a `Result<T>`.
     */
    suspend fun open(
        recipientPublicKey: ByteArray,
        recipientPrivateKey: ByteArray,
        sealed: ByteArray,
    ): ByteArray {
        ensureInitialized()
        require(recipientPublicKey.size == KEY_BYTES) {
            "recipientPublicKey debe medir $KEY_BYTES bytes, midió ${recipientPublicKey.size}"
        }
        require(recipientPrivateKey.size == KEY_BYTES) {
            "recipientPrivateKey debe medir $KEY_BYTES bytes, midió ${recipientPrivateKey.size}"
        }
        return try {
            Box.sealOpen(
                sealed.toUByteArray(),
                recipientPublicKey.toUByteArray(),
                recipientPrivateKey.toUByteArray(),
            ).toByteArray()
        } catch (e: BoxCorruptedOrTamperedDataException) {
            throw CryptoOpenException(
                "No se pudo abrir el sello: clave equivocada o datos corruptos/manipulados.",
                e,
            )
        }
    }
}
