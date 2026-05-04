package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra un intento de evaluacion.
 *
 * @param action Accion realizada (start, submit, save_draft, etc.).
 * @param durationMs Duracion de la accion en milisegundos.
 */
public fun Telemetry.recordAssessmentAttempt(action: String, durationMs: Long) {
    val labels = mapOf("action" to action)
    metrics.counter(MetricNames.ASSESSMENT_ATTEMPTS_TOTAL, labels = labels)
    metrics.histogram(MetricNames.ASSESSMENT_DURATION_MS, durationMs.toDouble(), labels)
    analytics.trackEvent("assessment_attempt", mapOf("action" to action, "duration_ms" to durationMs))
}

/**
 * Registra la vista de la pantalla de una evaluacion.
 *
 * @param assessmentId Identificador de la evaluacion.
 */
public fun Telemetry.trackAssessmentScreen(assessmentId: String) {
    analytics.trackScreen("assessment", mapOf("assessment_id" to assessmentId))
}
