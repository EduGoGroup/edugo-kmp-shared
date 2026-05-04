package com.edugo.kmp.telemetry.tracing

/**
 * Tipo de span — alineado con OTel SpanKind (proto values 1..5).
 *
 * Para el cliente HTTP KMP siempre se usa [CLIENT]. [SERVER] queda disponible
 * por simetria, aunque KMP no expone HTTP servers; igual la usaria un futuro
 * sink que envuelva un endpoint local.
 */
public enum class SpanKind(public val protoValue: Int) {
    INTERNAL(1),
    SERVER(2),
    CLIENT(3),
    PRODUCER(4),
    CONSUMER(5),
}
