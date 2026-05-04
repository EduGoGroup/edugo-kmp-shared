package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra una navegacion entre pantallas.
 *
 * @param from Pantalla de origen.
 * @param to Pantalla de destino.
 */
public fun Telemetry.trackNavigation(from: String, to: String) {
    val labels = mapOf("from" to from, "to" to to)
    metrics.counter(MetricNames.NAVIGATION_TOTAL, labels = labels)
    analytics.trackEvent("navigation", mapOf("from" to from, "to" to to))
}

/**
 * Registra la vista de una pantalla.
 *
 * @param screenName Nombre de la pantalla.
 * @param properties Propiedades adicionales.
 */
public fun Telemetry.trackScreenView(screenName: String, properties: Map<String, Any?> = emptyMap()) {
    metrics.counter(MetricNames.SCREEN_VIEWS_TOTAL, labels = mapOf("screen" to screenName))
    analytics.trackScreen(screenName, properties)
}
