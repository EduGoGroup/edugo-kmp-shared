/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage.reset

/**
 * Contrato para limpiar un store lógico del estado cliente.
 *
 * Cada implementación encapsula la limpieza de un concepto lógico (ej: `auth.tokens`,
 * `sdui.bundle`, `app.settings`) sobre su backing físico real (Preferences,
 * SharedPreferences, NSUserDefaults, localStorage, SQLite, etc.). Varios stores
 * lógicos pueden compartir el mismo backing físico — la granularidad es por
 * concepto, no por archivo.
 *
 * Las implementaciones se registran como singletons individuales en Koin y se
 * agrupan en una `List<ClientStateStore>` consumida por [ResetClientStateUseCase].
 *
 * Ejemplo de implementación:
 * ```kotlin
 * class AuthTokensStore(private val storage: SafeEduGoStorage) : ClientStateStore {
 *     override val id: String = "auth.tokens"
 *     override suspend fun clear() {
 *         storage.removeSafe("auth_token")
 *         storage.removeSafe("auth_user")
 *         storage.removeSafe("auth_context")
 *     }
 * }
 * ```
 */
public interface ClientStateStore {
    /**
     * Identificador estable del store lógico, usado como label en eventos OTel
     * (`event.name="client_state_reset"`, atributo `store=<id>`).
     *
     * Convención: `<dominio>.<concepto>` en snake/dot-case (ej: `auth.tokens`,
     * `sdui.bundle`, `app.settings`). Estable a través de versiones para mantener
     * dashboards Loki/Grafana funcionales.
     */
    public val id: String

    /**
     * Limpia el store lógico. Operación idempotente: invocar dos veces no produce
     * efectos adicionales más allá del primer borrado.
     *
     * Las implementaciones DEBEN ser tolerantes a "store ya vacío" (no lanzar si
     * no hay nada que borrar). Cualquier excepción se propaga al caller (típicamente
     * [ResetClientStateUseCase] que la captura via `runCatching`).
     */
    public suspend fun clear()
}
