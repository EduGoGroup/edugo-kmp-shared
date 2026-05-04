package com.edugo.kmp.telemetry

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity
import java.util.concurrent.atomic.AtomicReference

/**
 * Implementación de [AnalyticsRecorder] basada en OpenTelemetry Logs API.
 *
 * Modelo (ver docs/observability/01-arquitectura.md §4.1):
 * - trackEvent  -> LogRecord con `event.name` attribute (semántica OTel "event").
 * - trackScreen -> LogRecord con `event.name="screen_view"` y `screen.name`.
 * - setUserId / setUserProperty -> almacenados internamente y añadidos como
 *   `enduser.id` / `enduser.<prop>` a cada subsecuente emit.
 */
internal class OtelAnalyticsRecorder(
    private val otelLogger: Logger,
) : AnalyticsRecorder {

    private val userId = AtomicReference<String?>(null)
    // Concurrent-safe: snapshot semantics (read-mostly).
    private val userProperties = java.util.concurrent.ConcurrentHashMap<String, String>()

    override fun trackEvent(name: String, properties: Map<String, Any?>) {
        emitEvent(eventName = name, properties = properties, screenName = null)
    }

    override fun trackScreen(screenName: String, properties: Map<String, Any?>) {
        emitEvent(eventName = "screen_view", properties = properties, screenName = screenName)
    }

    override fun setUserProperty(name: String, value: String?) {
        if (value == null) userProperties.remove(name) else userProperties[name] = value
    }

    override fun setUserId(userId: String?) {
        this.userId.set(userId)
    }

    private fun emitEvent(eventName: String, properties: Map<String, Any?>, screenName: String?) {
        val attrs = Attributes.builder().apply {
            put(AttributeKey.stringKey("event.name"), eventName)
            screenName?.let { put(AttributeKey.stringKey("screen.name"), it) }
            userId.get()?.let { put(AttributeKey.stringKey("enduser.id"), it) }
            for ((k, v) in userProperties) {
                put(AttributeKey.stringKey("enduser.$k"), v)
            }
            for ((k, v) in properties) {
                if (v != null) put(AttributeKey.stringKey(k), v.toString())
            }
        }.build()

        otelLogger.logRecordBuilder()
            .setSeverity(Severity.INFO)
            .setSeverityText("INFO")
            .setBody(eventName)
            .setAllAttributes(attrs)
            .emit()
    }
}
