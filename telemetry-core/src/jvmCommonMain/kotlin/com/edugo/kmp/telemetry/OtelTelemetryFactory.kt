package com.edugo.kmp.telemetry

import com.edugo.kmp.telemetry.tracing.JvmOtelTracer
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.MetricReader
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * Factory para construir un [Telemetry] respaldado por OpenTelemetry Java SDK.
 *
 * Uso típico (desde el entry point Desktop):
 * ```kotlin
 * val telemetry = OtelTelemetryFactory.create(
 *     TelemetryConfig(
 *         serviceName = "edugo-ui-kmp-desktop",
 *         serviceVersion = "0.1.0",
 *         deploymentEnvironment = "local",
 *         endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: "http://localhost:4318",
 *     )
 * )
 * Runtime.getRuntime().addShutdownHook(Thread { OtelTelemetryFactory.shutdown() })
 * ```
 *
 * Si `config.enabled = false`, devuelve [Telemetry.Noop] sin inicializar SDK.
 */
public object OtelTelemetryFactory {

    private val sdkRef = AtomicReference<OpenTelemetrySdk?>(null)

    /**
     * Construye un [Telemetry] enchufado a OTLP HTTP. Idempotente:
     * llamadas adicionales devuelven la misma instancia y NO re-inicializan el SDK.
     *
     * Para Android (disk buffer — Tarea #7), pasar [extraSpanProcessor],
     * [extraLogProcessor] y/o [extraMetricReader] que persistan a SQLite y dejen
     * que un Worker WorkManager los reenvíe al Collector. Desktop pasa todo
     * `null` y mantiene su pipeline en RAM.
     */
    public fun create(
        config: TelemetryConfig,
        extraSpanProcessor: SpanProcessor? = null,
        extraLogProcessor: LogRecordProcessor? = null,
        extraMetricReader: MetricReader? = null,
    ): Telemetry {
        if (!config.enabled) return Telemetry.Noop

        // Si ya hay un SDK construido, retornamos un Telemetry asociado.
        // Esto permite (por error) llamar create() dos veces sin romper.
        sdkRef.get()?.let { return buildTelemetry(it, config.serviceName) }

        val sdk = buildSdk(config, extraSpanProcessor, extraLogProcessor, extraMetricReader)
        if (sdkRef.compareAndSet(null, sdk)) {
            return buildTelemetry(sdk, config.serviceName)
        }
        // Otro hilo ganó la carrera: cerramos el nuestro y usamos el suyo.
        sdk.close()
        return buildTelemetry(sdkRef.get()!!, config.serviceName)
    }

    /** Flush de buffers + cierre limpio. Llamar al apagar la app (shutdown hook). */
    public fun shutdown() {
        sdkRef.getAndSet(null)?.close()
    }

    private fun buildTelemetry(sdk: OpenTelemetrySdk, serviceName: String): Telemetry {
        val scope = serviceName.ifEmpty { "edugo" }
        val meter = sdk.meterProvider.get(scope)
        val otelLogger = sdk.logsBridge.get(scope)
        val otelTracer = sdk.tracerProvider.get(scope)
        return Telemetry(
            metrics = OtelMetricsRecorder(meter),
            analytics = OtelAnalyticsRecorder(otelLogger),
            crash = OtelCrashRecorder(otelLogger),
            tracer = JvmOtelTracer(otelTracer),
        )
    }

    private fun buildSdk(
        config: TelemetryConfig,
        extraSpanProcessor: SpanProcessor?,
        extraLogProcessor: LogRecordProcessor?,
        extraMetricReader: MetricReader?,
    ): OpenTelemetrySdk {
        val resource = buildResource(config)

        val spanExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("${config.endpoint.removeSuffix("/")}/v1/traces")
            .build()
        val metricExporter = OtlpHttpMetricExporter.builder()
            .setEndpoint("${config.endpoint.removeSuffix("/")}/v1/metrics")
            .build()
        val logExporter = OtlpHttpLogRecordExporter.builder()
            .setEndpoint("${config.endpoint.removeSuffix("/")}/v1/logs")
            .build()

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(
                BatchSpanProcessor.builder(spanExporter)
                    .setScheduleDelay(Duration.ofSeconds(5))
                    .build()
            )
            .apply { extraSpanProcessor?.let { addSpanProcessor(it) } }
            .build()

        val meterProvider = SdkMeterProvider.builder()
            .setResource(resource)
            .registerMetricReader(
                PeriodicMetricReader.builder(metricExporter)
                    .setInterval(Duration.ofSeconds(15))
                    .build()
            )
            .apply { extraMetricReader?.let { registerMetricReader(it) } }
            .build()

        val loggerProvider = SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(
                BatchLogRecordProcessor.builder(logExporter)
                    .setScheduleDelay(Duration.ofSeconds(5))
                    .build()
            )
            .apply { extraLogProcessor?.let { addLogRecordProcessor(it) } }
            .build()

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setLoggerProvider(loggerProvider)
            .build()
    }

    private fun buildResource(config: TelemetryConfig): Resource {
        val builder = Attributes.builder().apply {
            put(AttributeKey.stringKey("service.namespace"), config.serviceNamespace)
            put(AttributeKey.stringKey("service.name"), config.serviceName)
            put(AttributeKey.stringKey("service.version"), config.serviceVersion)
            put(AttributeKey.stringKey("service.instance.id"), UUID.randomUUID().toString())
            put(AttributeKey.stringKey("deployment.environment"), config.deploymentEnvironment)
            for ((k, v) in config.resourceAttributes) {
                put(AttributeKey.stringKey(k), v)
            }
        }
        return Resource.getDefault().merge(Resource.create(builder.build()))
    }
}
