package com.edugo.kmp.crypto

import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Pruebas de la capa de sellado anónimo ([SealedBoxCrypto]).
 *
 * Ejecutables en JVM/desktop (`:crypto:desktopTest`). Verifican round-trip, tamaños/overhead, el caso
 * negativo y la **compatibilidad de bytes con el backend Go** mediante un vector fijo generado por Go
 * en el spike F5.0 (`/tmp/spike-crypto-f5/vectors.json`).
 */
@OptIn(ExperimentalEncodingApi::class)
class SealedBoxCryptoTest {

    @Test
    fun roundTrip_seal_then_open_recovers_plaintext() = runTest {
        val keyPair = SealedBoxCrypto.generateDeviceKeyPair()
        val message = "DEK de 256 bits simulada para round-trip".encodeToByteArray()

        val sealed = SealedBoxCrypto.seal(keyPair.publicKey, message)
        val opened = SealedBoxCrypto.open(keyPair.publicKey, keyPair.privateKey, sealed)

        assertTrue(opened.contentEquals(message), "el plaintext recuperado debe ser idéntico al original")
    }

    @Test
    fun generated_keypair_has_32_byte_keys() = runTest {
        val keyPair = SealedBoxCrypto.generateDeviceKeyPair()

        assertEquals(SealedBoxCrypto.KEY_BYTES, keyPair.publicKey.size, "la pública debe medir 32 B")
        assertEquals(SealedBoxCrypto.KEY_BYTES, keyPair.privateKey.size, "la privada debe medir 32 B")
    }

    @Test
    fun seal_of_32_bytes_produces_80_bytes_overhead_48() = runTest {
        val keyPair = SealedBoxCrypto.generateDeviceKeyPair()
        val dek = ByteArray(32) { it.toByte() } // DEK de 256 bits

        val sealed = SealedBoxCrypto.seal(keyPair.publicKey, dek)

        assertEquals(48, SealedBoxCrypto.SEAL_OVERHEAD_BYTES, "el overhead anunciado debe ser 48")
        assertEquals(80, sealed.size, "sellar 32 B debe dar 80 B (overhead 48)")
        assertEquals(dek.size + SealedBoxCrypto.SEAL_OVERHEAD_BYTES, sealed.size)
    }

    @Test
    fun open_with_wrong_keypair_throws_CryptoOpenException() = runTest {
        val recipient = SealedBoxCrypto.generateDeviceKeyPair()
        val attacker = SealedBoxCrypto.generateDeviceKeyPair()
        val sealed = SealedBoxCrypto.seal(recipient.publicKey, "secreto".encodeToByteArray())

        // Abrir con el par equivocado debe fallar de forma explícita, no devolver basura.
        assertFailsWith<CryptoOpenException> {
            SealedBoxCrypto.open(attacker.publicKey, attacker.privateKey, sealed)
        }
    }

    /**
     * Compatibilidad de bytes Go → Kotlin: vector fijo generado por el backend Go
     * (`envelope.SealFor` = `box.SealAnonymous`) en el spike F5.0. Si Kotlin recupera el plaintext
     * esperado a partir del `sealed` que produjo Go, el formato del sello es idéntico entre ambos.
     */
    @Test
    fun go_vector_open_recovers_expected_plaintext_byte_compat() = runTest {
        // /tmp/spike-crypto-f5/vectors.json (Go genera; sealed_len=80, plaintext = bytes 0..31)
        val pub = Base64.decode("XFlkdK8t1B12LiQWO2CVpmuFRMrhZIg69v2KF5hYKG4=")
        val priv = Base64.decode("9pV17Wts0kAm35fuGZwwJ0ra9XOxytIfBVQCJgi1x5Q=")
        val expectedPlaintext = Base64.decode("AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=")
        val sealedByGo = Base64.decode(
            "BNTdvJdhmg7bZLspCq7YSPrnSYVeNMsOHOpm9Spz9hBm832e9cAtfW0/lO6pVPN6tBD4ZXbTXgDsN4vn336qPKkPWCxv3BQ5/jk9Zxu/lcw=",
        )

        assertEquals(32, expectedPlaintext.size, "el vector plaintext debe medir 32 B")
        assertEquals(80, sealedByGo.size, "el vector sealed de Go debe medir 80 B")

        val opened = SealedBoxCrypto.open(pub, priv, sealedByGo)

        assertTrue(
            opened.contentEquals(expectedPlaintext),
            "Kotlin debe recuperar el mismo plaintext que selló Go (byte-compat)",
        )
    }
}
