package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra el tiempo de arranque de la aplicacion.
 *
 * @param durationMs Duracion del arranque en milisegundos.
 */
public fun Telemetry.recordAppStart(durationMs: Long) {
    metrics.histogram(MetricNames.APP_START_DURATION_MS, durationMs.toDouble())
    analytics.trackEvent("app_start", mapOf("duration_ms" to durationMs))
}

/**
 * Registra un cambio de estado de la aplicacion.
 *
 * @param state Estado nuevo (foreground, background, etc.).
 */
public fun Telemetry.recordAppStateChange(state: String) {
    val labels = mapOf("state" to state)
    metrics.counter(MetricNames.APP_STATE_CHANGES_TOTAL, labels = labels)
    analytics.trackEvent("app_state_change", mapOf("state" to state))
}
