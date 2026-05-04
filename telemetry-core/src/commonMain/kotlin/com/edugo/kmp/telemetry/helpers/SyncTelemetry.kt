package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra una operacion de sincronizacion.
 *
 * @param type Tipo de sincronizacion (full, incremental, etc.).
 * @param durationMs Duracion de la operacion en milisegundos.
 * @param success Si la operacion fue exitosa.
 */
public fun Telemetry.recordSyncOperation(type: String, durationMs: Long, success: Boolean) {
    val labels = mapOf("type" to type, "success" to success.toString())
    metrics.counter(MetricNames.SYNC_OPERATIONS_TOTAL, labels = labels)
    metrics.histogram(MetricNames.SYNC_DURATION_MS, durationMs.toDouble(), labels)
    analytics.trackEvent("sync_operation", mapOf("type" to type, "duration_ms" to durationMs, "success" to success))
}
