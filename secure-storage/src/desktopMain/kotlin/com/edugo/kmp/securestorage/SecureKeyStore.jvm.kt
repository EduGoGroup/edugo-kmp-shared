/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

/**
 * Stub de Desktop (JVM).
 *
 * Desktop **no** es objetivo de F5 (mensajería corre en móvil) y la JVM de escritorio no ofrece un
 * almacén respaldado por hardware equivalente a Android Keystore / iOS Keychain. Guardar `Kd_priv` o la
 * DEK en un fichero en claro violaría la garantía zero-knowledge del Plan 025, así que **fallamos
 * ruidosamente** en vez de fingir custodia segura.
 *
 * Existe solo para que el módulo compile en el target `desktop` (que el convention plugin habilita
 * siempre). Si en el futuro se necesita mensajería en escritorio, implementar custodia real aquí
 * (p.ej. integración con el llavero del SO vía JNA) y reemplazar estos lanzamientos.
 */
actual class SecureKeyStore internal constructor() {

    actual fun putBytes(key: String, value: ByteArray): Unit = unsupported()

    actual fun getBytes(key: String): ByteArray? = unsupported()

    actual fun remove(key: String): Unit = unsupported()

    actual fun contains(key: String): Boolean = unsupported()

    private fun unsupported(): Nothing = throw SecureStorageException(
        "SecureKeyStore no está soportado en Desktop (JVM): no hay almacén seguro respaldado por el SO. " +
            "La mensajería EduGo es móvil (Android/iOS).",
    )
}

actual fun createSecureKeyStore(serviceName: String): SecureKeyStore = SecureKeyStore()
