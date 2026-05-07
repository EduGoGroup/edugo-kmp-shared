package com.edugo.kmp.config

// Orden iOS: scheme env var `OTEL_EXPORTER_OTLP_ENDPOINT` (NSProcessInfo) →
// Info.plist key `OtelExporterOtlpEndpoint` (interpolada desde `Config.xcconfig`).
//
// Ambos lookups van por `IosEnvSeam` para que los tests inyecten fakes sin tocar
// `NSProcessInfo`/`NSBundle` (read-only en runtime).
internal actual fun readNativeOtelEndpoint(): String? {
    val processEnv = IosEnvSeam.envProvider("OTEL_EXPORTER_OTLP_ENDPOINT")?.trim()
    if (!processEnv.isNullOrEmpty()) return processEnv
    val plistValue = IosEnvSeam.plistProvider("OtelExporterOtlpEndpoint")?.trim()
    return plistValue?.takeIf { it.isNotEmpty() }
}

internal actual fun defaultOtelEndpoint(): String = "http://localhost:4318"
