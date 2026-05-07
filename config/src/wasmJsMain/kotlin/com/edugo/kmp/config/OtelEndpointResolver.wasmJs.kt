package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try { return (typeof window !== 'undefined' && window.__OTEL_EXPORTER_OTLP_ENDPOINT__) ? String(window.__OTEL_EXPORTER_OTLP_ENDPOINT__) : ''; }
        catch (e) { console.error('[OtelEndpointResolver] window.__OTEL_EXPORTER_OTLP_ENDPOINT__ read failed:', e); return ''; }
    }"""
)
private external fun readWindowOtelEndpoint(): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try {
            if (typeof document === 'undefined') return '';
            var meta = document.querySelector('meta[name="otel-exporter-otlp-endpoint"]');
            return meta ? (meta.getAttribute('content') || '') : '';
        } catch (e) { console.error('[OtelEndpointResolver] meta[name=otel-exporter-otlp-endpoint] read failed:', e); return ''; }
    }"""
)
private external fun readMetaOtelEndpoint(): String

// Orden Web: window.__OTEL_EXPORTER_OTLP_ENDPOINT__ (runtime injection) →
// <meta name="otel-exporter-otlp-endpoint"> (build-time injection en index.html,
// pendiente TD-MPH-2.3). Si ambos vacíos, devuelve null y el orchestrador cae a
// BUILD_OTEL_ENDPOINT (callsite) → AppConfig → default.
internal actual fun readNativeOtelEndpoint(): String? {
    val window = readWindowOtelEndpoint().trim()
    if (window.isNotEmpty()) return window
    val meta = readMetaOtelEndpoint().trim()
    return meta.ifEmpty { null }
}

internal actual fun defaultOtelEndpoint(): String = "http://localhost:4318"
