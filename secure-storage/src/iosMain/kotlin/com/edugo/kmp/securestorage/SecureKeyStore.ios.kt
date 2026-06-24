/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

@file:OptIn(ExperimentalForeignApi::class)

package com.edugo.kmp.securestorage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Implementación iOS respaldada por Keychain Services.
 *
 * Cada secreto es un item `kSecClassGenericPassword` identificado por la pareja
 * (`kSecAttrService` = [serviceName], `kSecAttrAccount` = la clave lógica). La accesibilidad es
 * [kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly]:
 * - **`AfterFirstUnlock`**: legible tras el primer desbloqueo del dispositivo (sobrevive a relanzar la
 *   app en background sin re-desbloquear), pero no antes del primer unlock tras un reinicio.
 * - **`ThisDeviceOnly`**: el item **no migra** a backups de iCloud/iTunes ni a otros dispositivos. Para
 *   `Kd_priv` y la DEK esto es deseable: el secreto está atado a este dispositivo.
 *
 * El Keychain ya cifra los valores con claves protegidas por el Secure Enclave; aquí no añadimos otra
 * capa. Los `ByteArray` se transportan como `NSData` sin transformar.
 */
actual class SecureKeyStore internal constructor(
    private val serviceName: String,
) {

    actual fun putBytes(key: String, value: ByteArray) {
        // Upsert: el Keychain no sobrescribe en SecItemAdd (devolvería errSecDuplicateItem). Borramos
        // la entrada previa y reinsertamos para garantizar atomicidad lógica del "guardar".
        deleteRaw(key)
        val data = value.toNSData()
        val attributes = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )
        val status = SecItemAdd(attributes.toCFDictionary(), null)
        if (status != errSecSuccess) {
            throw SecureStorageException("SecItemAdd falló al guardar '$key' (OSStatus=$status).")
        }
    }

    actual fun getBytes(key: String): ByteArray? = memScoped {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            // kCFBooleanTrue (CFBoolean crudo): el Keychain exige específicamente un CFBoolean
            // aquí ("add_return: value 1 is not CFBoolean" si se pasa un NSNumber). Ahora que
            // toCFDictionary usa CFDictionaryCreate nativo (drift F6c.2), el CFBoolean crudo
            // llega intacto a la query sin el bridging por NSDictionary que antes lo corrompía.
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
        when (status) {
            errSecSuccess -> {
                // CFBridgingRelease toma posesión del CFTypeRef devuelto (regla "Copy" → debemos
                // liberarlo) y lo entrega como NSData gestionado por ARC.
                val data = CFBridgingRelease(result.value) as? NSData
                data?.toByteArray()
            }
            errSecItemNotFound -> null
            else -> throw SecureStorageException(
                "SecItemCopyMatching falló al leer '$key' (OSStatus=$status).",
            )
        }
    }

    actual fun remove(key: String) {
        val status = deleteRaw(key)
        // Borrar algo inexistente no es error para nuestro contrato (remove es no-op si no existe).
        if (status != errSecSuccess && status != errSecItemNotFound) {
            throw SecureStorageException("SecItemDelete falló al eliminar '$key' (OSStatus=$status).")
        }
    }

    actual fun contains(key: String): Boolean {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        // Sin kSecReturnData: solo comprobamos existencia, sin materializar el secreto.
        val status = SecItemCopyMatching(query.toCFDictionary(), null)
        return status == errSecSuccess
    }

    /** Borrado de bajo nivel reutilizado por put (upsert) y remove. Devuelve el OSStatus crudo. */
    private fun deleteRaw(key: String): Int {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to serviceName,
            kSecAttrAccount to key,
        )
        return SecItemDelete(query.toCFDictionary())
    }
}

/**
 * En iOS no hay configuración previa: el Keychain está disponible sin `Context`.
 */
actual fun createSecureKeyStore(serviceName: String): SecureKeyStore = SecureKeyStore(serviceName)
