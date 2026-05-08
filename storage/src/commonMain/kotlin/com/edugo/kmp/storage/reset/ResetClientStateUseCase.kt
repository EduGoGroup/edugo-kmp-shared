/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage.reset

import com.edugo.kmp.telemetry.Telemetry

/**
 * Use case **dev/QA** para limpiar el estado cliente local de la app a un estado
 * equivalente a primera instalación. NO reemplaza el flujo PROD de logout
 * (`LogoutUseCase`) — el reset es local-only y NO invoca server-side revoke.
 *
 * Itera la lista [stores] inyectada por DI invocando [ClientStateStore.clear] de
 * cada una en orden. Cada operación se envuelve en `runCatching`:
 * - **Éxito**: emite `analytics.trackEvent("client_state_reset", {"store": id})`.
 * - **Fallo**: registra `crash.log("Reset failed for $id: $msg")` y continúa con
 *   los siguientes stores (best-effort, no abort).
 *
 * Tras invocar este use case, la app debe navegar a `LoginScreen` (limpiar rutas
 * y ViewModels) — esa lógica vive en el caller (ej: menú dev en `kmp-screens`).
 *
 * Gateado por convención a `BUILD_ENVIRONMENT in (DEV, DEV_LAN, STAGING)`. NUNCA
 * exponer la entrada UI en PROD.
 */
public class ResetClientStateUseCase(
    private val stores: List<ClientStateStore>,
    private val telemetry: Telemetry,
) {
    public suspend operator fun invoke() {
        for (store in stores) {
            runCatching { store.clear() }
                .onSuccess {
                    telemetry.analytics.trackEvent(
                        EVENT_NAME,
                        mapOf("store" to store.id),
                    )
                }
                .onFailure { error ->
                    telemetry.crash.log("Reset failed for ${store.id}: ${error.message}")
                }
        }
    }

    public companion object {
        public const val EVENT_NAME: String = "client_state_reset"
    }
}
