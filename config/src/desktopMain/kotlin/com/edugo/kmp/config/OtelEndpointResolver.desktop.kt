package com.edugo.kmp.config

// Orden Desktop: env var `OTEL_EXPORTER_OTLP_ENDPOINT` (convención OTel oficial)
// → system property `otel.exporter.otlp.endpoint` (fallback testeable, ver
// `STANDARD.md §3` — env var es read-only en runtime JVM).
internal actual fun readNativeOtelEndpoint(): String? {
    val envVar = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")?.trim()
    if (!envVar.isNullOrEmpty()) return envVar
    val sysProp = System.getProperty("otel.exporter.otlp.endpoint")?.trim()
    return sysProp?.takeIf { it.isNotEmpty() }
}

internal actual fun defaultOtelEndpoint(): String = "http://localhost:4318"
