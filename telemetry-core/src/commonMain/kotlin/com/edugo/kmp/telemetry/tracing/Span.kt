package com.edugo.kmp.telemetry.tracing

/**
 * Span activo — fluent API para enriquecer + finalizar.
 *
 * Cada [Span] tiene un [context] estable desde el momento en que [Tracer.startSpan]
 * lo crea. Despues de [end] todos los setters son no-op (idempotente y seguro).
 */
public interface Span {
    public val context: SpanContext

    public fun setAttribute(key: String, value: String): Span
    public fun setAttribute(key: String, value: Long): Span
    public fun setAttribute(key: String, value: Double): Span
    public fun setAttribute(key: String, value: Boolean): Span

    public fun setStatus(code: SpanStatusCode, description: String? = null): Span

    public fun recordException(
        throwable: Throwable,
        attributes: Map<String, String> = emptyMap(),
    ): Span

    public fun end()
}

/**
 * Span no-op — devuelto por [NoopTracer] y usable como default seguro en cualquier
 * lugar donde el tracer no haya inicializado todavia (boot temprano, tests).
 *
 * Su [context] es [SpanContext.Invalid] — el propagador W3C lo detecta y NO inyecta
 * `traceparent`, evitando que las APIs Go reciban un trace-id "00..00" invalido.
 */
public class NoopSpan : Span {
    override val context: SpanContext = SpanContext.Invalid

    override fun setAttribute(key: String, value: String): Span = this
    override fun setAttribute(key: String, value: Long): Span = this
    override fun setAttribute(key: String, value: Double): Span = this
    override fun setAttribute(key: String, value: Boolean): Span = this
    override fun setStatus(code: SpanStatusCode, description: String?): Span = this
    override fun recordException(throwable: Throwable, attributes: Map<String, String>): Span = this
    override fun end() {}

    public companion object {
        public val Instance: NoopSpan = NoopSpan()
    }
}
