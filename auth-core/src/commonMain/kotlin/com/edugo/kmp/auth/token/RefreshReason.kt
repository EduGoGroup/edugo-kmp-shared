package com.edugo.kmp.auth.token

/**
 * Razón por la que el cliente dispara un refresh de token. Se propaga al server
 * vía header HTTP `X-Refresh-Reason` y aparece en el atributo OTel
 * `refresh_context` del span `usecase.RefreshToken`. Permite distinguir, sin
 * leer trace_ids manualmente, si un refresh fallido fue parte del bootstrap
 * normal (esperado al arrancar con tokens viejos) o de una regresión durante
 * un flujo activo.
 *
 * @property wireValue valor snake_case que viaja por el header HTTP. Debe
 *   coincidir con las constantes server-side `refreshtelemetry.Reason*` para
 *   que el normalizador no lo degrade a `unknown`.
 */
enum class RefreshReason(
    val wireValue: String,
) {
    /**
     * El refresh viene del path de `restoreSession` al arrancar la app: el
     * cliente encontró un access token expirado en storage y está intentando
     * recuperarlo antes de pintar la primera pantalla. Una falla aquí es
     * esperada cuando el refresh token también expiró o fue revocado.
     */
    Bootstrap("bootstrap"),

    /**
     * El refresh ocurre durante uso normal de la app: o bien retry reactivo
     * tras un 401, o bien el scheduler proactivo (`scheduleNextRefresh`) que
     * se anticipa a la expiración del access token mientras la app está en
     * foreground. Una falla aquí es candidata a regresión y debería
     * investigarse.
     */
    ApiCall("api_call"),
}
