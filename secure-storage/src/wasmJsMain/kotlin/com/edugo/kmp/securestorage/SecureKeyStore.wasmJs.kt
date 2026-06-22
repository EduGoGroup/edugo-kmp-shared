/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

/**
 * Stub de Web (WasmJS).
 *
 * Web está **fuera** de F5 y el navegador no expone un almacén respaldado por hardware comparable a
 * Keystore/Keychain (`localStorage` es texto plano accesible por cualquier script de la página).
 * Custodiar `Kd_priv` o la DEK ahí rompería la garantía zero-knowledge del Plan 025, así que fallamos
 * ruidosamente en lugar de simular custodia segura.
 *
 * Solo existe para que el módulo compile cuando se habilita el target `wasmJs` (`enableWeb=true`). Una
 * eventual mensajería web debería diseñar su propio esquema (p.ej. claves no extraíbles de WebCrypto)
 * y reemplazar estos lanzamientos.
 */
actual class SecureKeyStore internal constructor() {

    actual fun putBytes(key: String, value: ByteArray): Unit = unsupported()

    actual fun getBytes(key: String): ByteArray? = unsupported()

    actual fun remove(key: String): Unit = unsupported()

    actual fun contains(key: String): Boolean = unsupported()

    private fun unsupported(): Nothing = throw SecureStorageException(
        "SecureKeyStore no está soportado en Web (WasmJS): el navegador no ofrece un almacén seguro " +
            "respaldado por el SO. La mensajería EduGo es móvil (Android/iOS).",
    )
}

actual fun createSecureKeyStore(serviceName: String): SecureKeyStore = SecureKeyStore()
