package com.edugo.kmp.telemetry.tracing

/**
 * Punto de entrada cross-platform para emitir spans.
 *
 * Implementaciones reales:
 * - jvmCommon: `JvmOtelTracer` envuelve `io.opentelemetry.api.trace.Tracer` (Desktop+Android).
 * - iosMain: `IosOtelTracer` puro Kotlin/Native, exporta a OTLP/JSON.
 * - wasmJs: queda como [NoopTracer] — la auto-instrumentation `@opentelemetry/instrumentation-fetch`
 *   ya emite spans CLIENT desde el navegador (ver D9 en `01-arquitectura.md`).
 *
 * Convenciones de naming OTel para spans HTTP cliente:
 * `"<METHOD> <route_sanitizada>"` (eg `"POST /auth/login"`). NUNCA incluir IDs
 * en la ruta — eso explota cardinalidad en Tempo.
 */
public interface Tracer {
    /**
     * Crea un span y lo deja "en marcha". El caller es responsable de llamar
     * [Span.end] al final del trabajo medido.
     *
     * @param name nombre legible — `"<METHOD> <route>"` para HTTP, `"<dominio>.<accion>"` para
     *   trabajo interno.
     * @param kind tipo de span — usar [SpanKind.CLIENT] cuando se llama a una API externa.
     * @param attributes attrs iniciales (mas se pueden anadir con [Span.setAttribute]).
     * @param parent contexto del span padre. Si es `null`, se intenta tomar el span actual
     *   del [kotlin.coroutines.CoroutineContext] via [SpanContextElement]; si tampoco hay,
     *   se crea un trace nuevo (root span).
     */
    public fun startSpan(
        name: String,
        kind: SpanKind = SpanKind.INTERNAL,
        attributes: Map<String, String> = emptyMap(),
        parent: SpanContext? = null,
    ): Span
}

/** Tracer no-op — todo span devuelto es [NoopSpan]. Default seguro pre-init. */
public class NoopTracer : Tracer {
    override fun startSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, String>,
        parent: SpanContext?,
    ): Span = NoopSpan.Instance

    public companion object {
        public val Instance: NoopTracer = NoopTracer()
    }
}
