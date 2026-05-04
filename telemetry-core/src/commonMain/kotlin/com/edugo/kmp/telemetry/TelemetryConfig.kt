package com.edugo.kmp.telemetry

/**
 * Configuración cross-platform para inicializar un sink de telemetría.
 * Cada plataforma construye su propia instancia y se la pasa al factory correspondiente
 * (ej. OtelTelemetryFactory en JVM/Desktop).
 *
 * Mapea 1:1 con los resource attributes definidos en
 * docs/observability/01-arquitectura.md §2.
 */
public data class TelemetryConfig(
    /** Identificador del servicio. Convención: edugo-<dominio>-<plataforma>. */
    public val serviceName: String,

    /** Versión semver del servicio + opcionalmente git short sha. */
    public val serviceVersion: String,

    /** Namespace del ecosistema. Constante "edugo" salvo razón de peso. */
    public val serviceNamespace: String = "edugo",

    /** local | dev | staging | prod. */
    public val deploymentEnvironment: String,

    /** Endpoint OTLP del Collector. Local: http://localhost:4318. */
    public val endpoint: String,

    /** HTTP/protobuf por defecto (más portable que gRPC, soportado por todos). */
    public val protocol: Protocol = Protocol.HTTP_PROTOBUF,

    /** Si false, el factory devuelve Telemetry.Noop sin gastar recursos. */
    public val enabled: Boolean = true,

    /** Resource attrs adicionales para el deployment (ej. host.name, device.model). */
    public val resourceAttributes: Map<String, String> = emptyMap(),
) {
    public enum class Protocol { GRPC, HTTP_PROTOBUF }
}
