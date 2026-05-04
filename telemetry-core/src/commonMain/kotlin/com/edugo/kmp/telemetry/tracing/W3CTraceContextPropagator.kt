package com.edugo.kmp.telemetry.tracing

/**
 * Propagador W3C Trace Context — formato `00-{traceId}-{spanId}-{flags}` (version 00).
 *
 * Spec: <https://www.w3.org/TR/trace-context/>.
 *
 * Acoplado al cliente HTTP KMP en `:modules:network` (interceptor Ktor) — D11.
 * NO depende del SDK Java OTel: trabaja sobre [SpanContext] cross-platform.
 */
public object W3CTraceContextPropagator {

    public const val TRACEPARENT: String = "traceparent"
    public const val TRACESTATE: String = "tracestate"
    private const val VERSION: String = "00"

    /**
     * Inyecta `traceparent` (y `tracestate` si no esta vacio) en [headers].
     *
     * Si [context] es invalido o todo-ceros, NO inyecta nada — evita que el
     * receptor reciba un trace-id basura. Caller idiomatico:
     * ```
     * if (span.context.isValid) propagator.inject(span.context, headers)
     * ```
     * pero el guard interno hace que el caller no se pueda equivocar.
     */
    public fun inject(context: SpanContext, headers: MutableMap<String, String>) {
        if (!context.isValid) return
        val flagsHex = context.flags.toInt().and(0xFF).toString(16).padStart(2, '0')
        headers[TRACEPARENT] = "$VERSION-${context.traceIdHex}-${context.spanIdHex}-$flagsHex"
        if (context.traceState.isNotEmpty()) {
            headers[TRACESTATE] = context.traceState
        }
    }

    /**
     * Extrae [SpanContext] de [headers]. Devuelve `null` si el header no esta
     * presente, esta malformado o tiene el trace-id/span-id invalido (todo-ceros).
     *
     * Solo soporta version "00" (la unica publicada por W3C). Si llega otra,
     * devuelve `null` — comportamiento conservador per spec recomendacion.
     */
    public fun extract(headers: Map<String, String>): SpanContext? {
        val rawTp = headers[TRACEPARENT] ?: headers[TRACEPARENT.uppercase()] ?: return null
        val parts = rawTp.split("-")
        if (parts.size != 4) return null
        val (version, traceId, spanId, flagsStr) = parts
        if (version != VERSION) return null
        if (traceId.length != SpanContext.TRACE_ID_HEX_LEN) return null
        if (spanId.length != SpanContext.SPAN_ID_HEX_LEN) return null
        if (flagsStr.length != 2) return null
        val flags = flagsStr.toIntOrNull(16)?.toByte() ?: return null
        val traceState = headers[TRACESTATE] ?: headers[TRACESTATE.uppercase()] ?: ""
        val ctx = SpanContext(
            traceIdHex = traceId.lowercase(),
            spanIdHex = spanId.lowercase(),
            flags = flags,
            traceState = traceState,
        )
        return if (ctx.isValid) ctx else null
    }
}
