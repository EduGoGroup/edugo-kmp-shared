/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Implementación Android respaldada por `EncryptedSharedPreferences`.
 *
 * Los pares clave→valor se guardan en un fichero de `SharedPreferences` donde **tanto la clave como el
 * valor están cifrados** (AES256-SIV para la clave, AES256-GCM para el valor). El secreto que cifra ese
 * fichero es la `MasterKey` AES256-GCM que vive en el **Android Keystore**: nunca abandona el almacén
 * de claves del SO y es hardware-backed cuando el SoC lo soporta (`minSdk 26` garantiza Keystore con
 * AES). Así, los bytes en disco son inútiles sin la `MasterKey`, que la app no puede exportar.
 *
 * Como `EncryptedSharedPreferences` solo maneja `String`, los `ByteArray` se serializan en Base64
 * (`NO_WRAP`). El Base64 solo afecta a la representación interna; la API expone bytes puros.
 */
actual class SecureKeyStore internal constructor(
    private val prefs: SharedPreferences,
) {

    actual fun putBytes(key: String, value: ByteArray) {
        try {
            val encoded = Base64.encodeToString(value, Base64.NO_WRAP)
            // commit() (síncrono) en lugar de apply(): la operación es bloqueante por contrato y
            // queremos propagar un fallo de escritura como excepción, no perderlo en background.
            val ok = prefs.edit().putString(key, encoded).commit()
            if (!ok) {
                throw SecureStorageException("No se pudo persistir la clave '$key' en el almacén seguro.")
            }
        } catch (e: SecureStorageException) {
            throw e
        } catch (e: Exception) {
            throw SecureStorageException("Fallo al escribir '$key' en EncryptedSharedPreferences.", e)
        }
    }

    actual fun getBytes(key: String): ByteArray? {
        return try {
            val encoded = prefs.getString(key, null) ?: return null
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecureStorageException("Fallo al leer '$key' de EncryptedSharedPreferences.", e)
        }
    }

    actual fun remove(key: String) {
        try {
            val ok = prefs.edit().remove(key).commit()
            if (!ok) {
                throw SecureStorageException("No se pudo eliminar la clave '$key' del almacén seguro.")
            }
        } catch (e: SecureStorageException) {
            throw e
        } catch (e: Exception) {
            throw SecureStorageException("Fallo al eliminar '$key' de EncryptedSharedPreferences.", e)
        }
    }

    actual fun contains(key: String): Boolean = prefs.contains(key)
}

/**
 * Construye el [SecureKeyStore] Android leyendo el `Context` registrado en
 * [SecureStorageInitializer]. Lanza [SecureStorageException] si la app no llamó a
 * [SecureStorageInitializer.install] al arrancar.
 */
actual fun createSecureKeyStore(serviceName: String): SecureKeyStore {
    val context = SecureStorageInitializer.requireContext()
    val prefs = buildEncryptedPrefs(context, serviceName)
    return SecureKeyStore(prefs)
}

/**
 * Crea (o abre) el fichero `EncryptedSharedPreferences` cifrado con una `MasterKey` AES256-GCM del
 * Android Keystore. Idempotente: reabrir con el mismo [serviceName] recupera los secretos previos.
 */
private fun buildEncryptedPrefs(context: Context, serviceName: String): SharedPreferences {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            serviceName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        throw SecureStorageException(
            "No se pudo inicializar EncryptedSharedPreferences (MasterKey/Keystore inaccesible).",
            e,
        )
    }
}
