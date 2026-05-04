package com.edugo.kmp.telemetry.tracing

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanContext as OtelNativeSpanContext
import io.opentelemetry.api.trace.SpanKind as OtelNativeSpanKind
import io.opentelemetry.api.trace.StatusCode as OtelNativeStatusCode
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.api.trace.Tracer as OtelNativeTracer
import io.opentelemetry.context.Context

/**
 * Implementacion JVM del [Tracer] cross-platform — envuelve un
 * `io.opentelemetry.api.trace.Tracer` del SDK Java.
 *
 * Usado por Desktop (jvm) y Android (jvm). El SDK trasero ya esta wireado en
 * [OtelTelemetryFactory.buildSdk] (`SdkTracerProvider`) — aqui solo adaptamos
 * shapes.
 */
internal class JvmOtelTracer(
    private val delegate: OtelNativeTracer,
) : Tracer {

    override fun startSpan(
        name: String,
        kind: SpanKind,
        attributes: Map<String, String>,
        parent: SpanContext?,
    ): Span {
        val builder = delegate.spanBuilder(name).setSpanKind(kind.toNative())
        for ((k, v) in attributes) builder.setAttribute(k, v)
        val parentContext = parent?.let { ctx ->
            io.opentelemetry.api.trace.Span
                .wrap(ctx.toNative())
                .storeInContext(Context.current())
        }
        if (parentContext != null) builder.setParent(parentContext)
        val nativeSpan = builder.startSpan()
        return JvmOtelSpan(nativeSpan)
    }
}

private fun SpanKind.toNative(): OtelNativeSpanKind = when (this) {
    SpanKind.INTERNAL -> OtelNativeSpanKind.INTERNAL
    SpanKind.SERVER -> OtelNativeSpanKind.SERVER
    SpanKind.CLIENT -> OtelNativeSpanKind.CLIENT
    SpanKind.PRODUCER -> OtelNativeSpanKind.PRODUCER
    SpanKind.CONSUMER -> OtelNativeSpanKind.CONSUMER
}

private fun SpanContext.toNative(): OtelNativeSpanContext {
    val flags = TraceFlags.fromByte(this.flags)
    val state = if (traceState.isEmpty()) TraceState.getDefault() else parseTraceState(traceState)
    return OtelNativeSpanContext.createFromRemoteParent(traceIdHex, spanIdHex, flags, state)
}

private fun parseTraceState(raw: String): TraceState {
    val builder = TraceState.builder()
    raw.split(',').forEach { entry ->
        val idx = entry.indexOf('=')
        if (idx > 0 && idx < entry.length - 1) {
            val k = entry.substring(0, idx).trim()
            val v = entry.substring(idx + 1).trim()
            if (k.isNotEmpty() && v.isNotEmpty()) builder.put(k, v)
        }
    }
    return builder.build()
}

internal class JvmOtelSpan(
    private val delegate: io.opentelemetry.api.trace.Span,
) : Span {

    override val context: SpanContext by lazy { delegate.spanContext.toCrossPlatform() }

    override fun setAttribute(key: String, value: String): Span = apply {
        delegate.setAttribute(key, value)
    }

    override fun setAttribute(key: String, value: Long): Span = apply {
        delegate.setAttribute(key, value)
    }

    override fun setAttribute(key: String, value: Double): Span = apply {
        delegate.setAttribute(key, value)
    }

    override fun setAttribute(key: String, value: Boolean): Span = apply {
        delegate.setAttribute(key, value)
    }

    override fun setStatus(code: SpanStatusCode, description: String?): Span = apply {
        val nativeCode = when (code) {
            SpanStatusCode.UNSET -> OtelNativeStatusCode.UNSET
            SpanStatusCode.OK -> OtelNativeStatusCode.OK
            SpanStatusCode.ERROR -> OtelNativeStatusCode.ERROR
        }
        if (description != null) delegate.setStatus(nativeCode, description)
        else delegate.setStatus(nativeCode)
    }

    override fun recordException(throwable: Throwable, attributes: Map<String, String>): Span = apply {
        if (attributes.isEmpty()) {
            delegate.recordException(throwable)
        } else {
            val builder = Attributes.builder()
            for ((k, v) in attributes) builder.put(AttributeKey.stringKey(k), v)
            delegate.recordException(throwable, builder.build())
        }
    }

    override fun end() {
        delegate.end()
    }
}

private fun OtelNativeSpanContext.toCrossPlatform(): SpanContext = SpanContext(
    traceIdHex = traceId,
    spanIdHex = spanId,
    flags = traceFlags.asByte(),
    traceState = traceState.asMap()
        .entries
        .joinToString(",") { (k, v) -> "$k=$v" },
)
