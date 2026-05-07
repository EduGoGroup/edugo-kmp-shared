package com.edugo.kmp.config

// Orden Android: system property `otel.exporter.otlp.endpoint` (testeable —
// el override real de producción viaja por `BuildConfig.OTEL_EXPORTER_OTLP_ENDPOINT`
// que el callsite pasa como `buildOverride` de `OtelEndpointResolver.resolve`)
// → env var `OTEL_EXPORTER_OTLP_ENDPOINT` (raro en Android pero válido).
internal actual fun readNativeOtelEndpoint(): String? {
    val sysProp = System.getProperty("otel.exporter.otlp.endpoint")?.trim()
    if (!sysProp.isNullOrEmpty()) return sysProp
    val envVar = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")?.trim()
    return envVar?.takeIf { it.isNotEmpty() }
}

// 10.0.2.2 es el host de la máquina visto desde el emulador stock. Para
// dispositivos físicos, override con la IP real del Collector vía JSON
// (`dev-lan.json`) o env var nativa.
internal actual fun defaultOtelEndpoint(): String = "http://10.0.2.2:4318"
