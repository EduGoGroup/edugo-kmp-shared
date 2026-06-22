/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

import android.content.Context

/**
 * Punto de inyección del `Context` que [createSecureKeyStore] necesita en Android.
 *
 * `EncryptedSharedPreferences` exige un `Context`, pero la factory `createSecureKeyStore` es común a
 * todas las plataformas y no puede recibirlo por parámetro. La app lo registra **una sola vez** al
 * arrancar (típicamente en `Application.onCreate`):
 *
 * ```kotlin
 * class EduGoApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         SecureStorageInitializer.install(this) // o applicationContext
 *     }
 * }
 * ```
 *
 * Se guarda siempre el `applicationContext` (no la `Activity`) para no filtrar ningún contexto con
 * ciclo de vida corto. Es seguro llamar a [install] más de una vez; la última gana.
 *
 * **Nota para F5.5 (UI nativa):** si la app usa Koin, basta con que el `Application` llame a
 * `SecureStorageInitializer.install(this)` antes de que cualquier `module` resuelva un
 * [SecureKeyStore]. No hace falta inyectar el `Context` en el grafo de DI del store.
 */
object SecureStorageInitializer {

    @Volatile
    private var appContext: Context? = null

    /** Registra el `Context` de la app. Idempotente; se conserva el `applicationContext`. */
    fun install(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Devuelve el `Context` registrado o lanza si la app no llamó a [install].
     */
    internal fun requireContext(): Context =
        appContext ?: throw SecureStorageException(
            "SecureStorageInitializer.install(context) no fue llamado antes de createSecureKeyStore(). " +
                "Llámalo una vez en Application.onCreate().",
        )
}
