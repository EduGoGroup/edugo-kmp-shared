package com.edugo.kmp.config

import kotlinx.serialization.Serializable

/**
 * Configuración de telemetría (OpenTelemetry).
 *
 * Agrupa propiedades del cliente OTel — actualmente solo el endpoint del
 * Collector OTLP/HTTP. Forma parte del catálogo cross-platform `AppEnvVar`
 * (entry `OTEL_EXPORTER_OTLP_ENDPOINT`) y se sobreescribe por entorno
 * vía las fuentes nativas declaradas en `STANDARD.md §3`.
 *
 * Cuando [otelEndpoint] viene vacío, el callsite de cada plataforma debe
 * aplicar su default propio (ej. `http://10.0.2.2:4318` en Android emulator,
 * `http://localhost:4318` en Desktop / iOS Simulator).
 */
interface TelemetryConfig {
    /**
     * URL del Collector OTel (esquema OTLP/HTTP).
     *
     * Vacío significa "callsite usa default propio de plataforma". Si tiene
     * valor, debe empezar con `http://` o `https://`; cualquier otra cosa
     * provoca `IllegalArgumentException` en construcción.
     */
    val otelEndpoint: String
}

/**
 * Implementación serializable de [TelemetryConfig].
 *
 * Validación URL en `init` con `require(...)` → lanza `IllegalArgumentException`.
 * La divergencia con `AppConfigImpl` (que usa `IllegalStateException` para sus
 * invariantes de combinación) es intencional: `require` es la API canónica de
 * Kotlin para validación de argumentos de constructor; las otras clases del
 * módulo validan combinaciones de campos ya presentes (no el campo en sí).
 */
@Serializable
data class TelemetryConfigImpl(
    override val otelEndpoint: String = ""
) : TelemetryConfig {
    init {
        require(
            otelEndpoint.isEmpty() ||
                otelEndpoint.startsWith("http://") ||
                otelEndpoint.startsWith("https://")
        ) {
            "TelemetryConfig.otelEndpoint debe estar vacío o empezar con http:// o https://. " +
                "Recibido: \"$otelEndpoint\"."
        }
    }
}
