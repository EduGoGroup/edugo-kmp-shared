package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.MetricNames.HTTP_ERRORS_TOTAL
import com.edugo.kmp.telemetry.Telemetry

/**
 * Sanitiza un path HTTP reemplazando UUIDs e IDs numericos por `:id`
 * para evitar alta cardinalidad en labels de metricas.
 */
public fun sanitizePath(path: String): String {
    return path.replace(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), ":id")
               .replace(Regex("/\\d+"), "/:id")
}

/**
 * Registra una peticion HTTP completada.
 *
 * @param method Metodo HTTP (GET, POST, etc.).
 * @param path Path del endpoint.
 * @param statusCode Codigo de respuesta HTTP.
 * @param durationMs Duracion de la peticion en milisegundos.
 */
public fun Telemetry.recordHttpRequest(
    method: String,
    path: String,
    statusCode: Int,
    durationMs: Long
) {
    val sanitized = sanitizePath(path)
    val labels = mapOf("method" to method, "path" to sanitized, "status" to statusCode.toString())
    metrics.counter(MetricNames.HTTP_REQUESTS_TOTAL, labels = labels)
    metrics.histogram(MetricNames.HTTP_REQUEST_DURATION_MS, durationMs.toDouble(), labels)
    analytics.trackEvent("http_request", mapOf("method" to method, "path" to sanitized, "status" to statusCode, "duration_ms" to durationMs))
}

/**
 * Registra un error de red.
 *
 * @param path Path del endpoint que fallo.
 * @param error Tipo de error (nombre de clase de la excepcion).
 */
public fun Telemetry.recordNetworkError(path: String, error: String) {
    val sanitized = sanitizePath(path)
    metrics.counter(HTTP_ERRORS_TOTAL, 1.0, mapOf("path" to sanitized, "error_type" to error))
    crash.log("Network error: $sanitized - $error")
}
