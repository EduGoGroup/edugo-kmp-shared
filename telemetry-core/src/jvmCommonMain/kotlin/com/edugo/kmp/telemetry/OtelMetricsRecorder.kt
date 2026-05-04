package com.edugo.kmp.telemetry

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementación de [MetricsRecorder] basada en OpenTelemetry Java SDK.
 *
 * - counter -> LongCounter (incremento entero, valor redondeado).
 * - histogram -> DoubleHistogram (latencias, tamaños, cualquier distribución).
 * - gauge -> no soportado en MVP (la API OTel async requiere callback). Se loguea como warn.
 *
 * Los instruments se cachean por nombre para evitar re-creación en cada llamada
 * (la API OTel ya idempotiza pero el cache evita lookups).
 */
internal class OtelMetricsRecorder(
    private val meter: Meter,
) : MetricsRecorder {

    private val counters = ConcurrentHashMap<String, LongCounter>()
    private val histograms = ConcurrentHashMap<String, DoubleHistogram>()

    override fun counter(name: String, value: Double, labels: Map<String, String>) {
        val instrument = counters.getOrPut(name) {
            meter.counterBuilder(name).build()
        }
        instrument.add(value.toLong().coerceAtLeast(0L), labels.toAttributes())
    }

    override fun histogram(name: String, value: Double, labels: Map<String, String>) {
        val instrument = histograms.getOrPut(name) {
            // Convención EduGo: nombres terminados en _ms son milisegundos.
            val unit = if (name.endsWith("_ms")) "ms" else "1"
            meter.histogramBuilder(name).setUnit(unit).build()
        }
        instrument.record(value, labels.toAttributes())
    }

    override fun gauge(name: String, value: Double, labels: Map<String, String>) {
        // OTel Gauge sincrónico está marcado experimental.
        // Reportamos como histogram con un único valor para no perder la señal.
        // TODO(Fase 3 refinement): migrar a ObservableDoubleGauge con AtomicReference.
        histogram(name, value, labels)
    }

    private fun Map<String, String>.toAttributes(): Attributes {
        if (isEmpty()) return Attributes.empty()
        val builder = Attributes.builder()
        for ((k, v) in this) {
            builder.put(AttributeKey.stringKey(k), v)
        }
        return builder.build()
    }
}
