package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra un intento de login.
 *
 * @param success Si el login fue exitoso.
 * @param durationMs Duracion del login en milisegundos.
 */
public fun Telemetry.recordLogin(success: Boolean, durationMs: Long) {
    val labels = mapOf("success" to success.toString())
    metrics.counter(MetricNames.AUTH_LOGINS_TOTAL, labels = labels)
    metrics.histogram(MetricNames.AUTH_LOGIN_DURATION_MS, durationMs.toDouble(), labels)
    analytics.trackEvent("login", mapOf("success" to success, "duration_ms" to durationMs))
}

/**
 * Registra un refresh de token.
 *
 * @param success Si el refresh fue exitoso.
 */
public fun Telemetry.recordTokenRefresh(success: Boolean) {
    val labels = mapOf("success" to success.toString())
    metrics.counter(MetricNames.AUTH_TOKEN_REFRESH_TOTAL, labels = labels)
    analytics.trackEvent("token_refresh", mapOf("success" to success))
}

/**
 * Establece el userId en analytics y crash reporters.
 *
 * @param userId Identificador del usuario.
 */
public fun Telemetry.setUserId(userId: String?) {
    analytics.setUserId(userId)
    crash.setUserId(userId)
}

/**
 * Registra la vista de la pantalla de login.
 */
public fun Telemetry.trackLoginScreen() {
    analytics.trackScreen("login")
}
