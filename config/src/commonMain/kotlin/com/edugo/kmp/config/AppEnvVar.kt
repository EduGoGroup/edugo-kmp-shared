package com.edugo.kmp.config

/**
 * Plataforma destino del proyecto KMP. Discriminador para asociar cada
 * [AppEnvVar] con sus claves nativas por plataforma.
 *
 * Nota: este enum es interno al módulo `config` y sólo se usa para describir
 * las fuentes nativas de las variables de entorno. No es un reemplazo de
 * `PlatformDetector` (foundation), que tiene un propósito de runtime distinto.
 */
internal enum class TargetPlatform {
    DESKTOP,
    ANDROID,
    IOS,
    WEB
}

/**
 * Describe cómo cada plataforma expone una variable de entorno en runtime.
 *
 * Una misma variable canónica (`APP_ENVIRONMENT`) puede tener una clave
 * primaria y una clave secundaria por plataforma; el framework de tests
 * (Fase 4) las consulta para inyectar valores y para limpiarlas.
 */
internal sealed interface NativeKey {
    /** JVM system property (lectura via `System.getProperty`, mutable). */
    data class SystemProperty(val key: String) : NativeKey

    /** JVM environment variable (lectura via `System.getenv`, read-only en runtime). */
    data class EnvVar(val key: String) : NativeKey

    /** iOS `NSProcessInfo.processInfo.environment` key. */
    data class ProcessEnv(val key: String) : NativeKey

    /** iOS `NSBundle.mainBundle.infoDictionary` key (interpolada desde `Config.xcconfig`). */
    data class PlistKey(val key: String) : NativeKey

    /** WasmJS `window[key]` global. */
    data class WindowGlobal(val key: String) : NativeKey

    /** WasmJS `<meta name="..." content="...">` tag. */
    data class MetaTag(val name: String) : NativeKey
}

/**
 * Catálogo de variables de entorno que la aplicación entiende.
 *
 * Cada entrada describe:
 *  - su nombre canónico (el que aparece en docs y CLI: `APP_ENVIRONMENT`),
 *  - su(s) clave(s) nativa(s) primaria(s) por plataforma,
 *  - sus claves nativas de fallback por plataforma (si aplica),
 *  - un validador opcional que rechaza valores no soportados.
 *
 * El framework de tests (`EnvVarSource`, `EnvironmentDetectorContractTest`)
 * itera este catálogo para inyectar/limpiar valores de forma uniforme y
 * para preparar el terreno a futuras variables (LOG_LEVEL, OTEL_ENDPOINT,
 * feature flags, etc.) sin reescribir tests.
 */
internal enum class AppEnvVar(
    val canonicalName: String,
    val primaryKeys: Map<TargetPlatform, NativeKey>,
    val fallbackKeys: Map<TargetPlatform, NativeKey>,
    val validate: (String) -> Boolean
) {
    APP_ENVIRONMENT(
        canonicalName = "APP_ENVIRONMENT",
        primaryKeys = mapOf(
            TargetPlatform.DESKTOP to NativeKey.SystemProperty("app.environment"),
            TargetPlatform.ANDROID to NativeKey.SystemProperty("app.environment"),
            TargetPlatform.IOS to NativeKey.ProcessEnv("APP_ENVIRONMENT"),
            TargetPlatform.WEB to NativeKey.WindowGlobal("__APP_ENVIRONMENT__")
        ),
        fallbackKeys = mapOf(
            TargetPlatform.DESKTOP to NativeKey.EnvVar("APP_ENVIRONMENT"),
            TargetPlatform.ANDROID to NativeKey.EnvVar("APP_ENVIRONMENT"),
            TargetPlatform.IOS to NativeKey.PlistKey("AppEnvironment"),
            TargetPlatform.WEB to NativeKey.MetaTag("app-environment")
        ),
        validate = { Environment.fromString(it) != null }
    ),

    /**
     * Endpoint del Collector OTel (OTLP/HTTP). Catalogada v2026-05-06 por DA-MPH-2.
     *
     * Justificación de las primarias:
     *  - **Desktop**: `EnvVar` es la convención OTel oficial (`OTEL_EXPORTER_OTLP_ENDPOINT`);
     *    `SystemProperty` queda como fallback para tests (env var es read-only en runtime JVM).
     *  - **Android**: `SystemProperty` primario porque es testeable (mutable runtime);
     *    el override real de producción es `BuildConfig.OTEL_EXPORTER_OTLP_ENDPOINT`,
     *    bakeado por Gradle y resuelto en el callsite (mismo patrón que `APP_ENVIRONMENT`
     *    con `BuildConfig.BUILD_ENVIRONMENT` — ver `STANDARD.md §3.2 / §10 nota 4`).
     *  - **iOS**: `ProcessEnv` primario (env var del scheme, igual que `APP_ENVIRONMENT`);
     *    `PlistKey` fallback interpolado desde `Config.xcconfig`.
     *  - **Web**: `WindowGlobal` primario (override pre-bootstrap, paralelo a
     *    `__APP_ENVIRONMENT__`); `MetaTag` fallback (paralelo a `app-environment`).
     */
    OTEL_EXPORTER_OTLP_ENDPOINT(
        canonicalName = "OTEL_EXPORTER_OTLP_ENDPOINT",
        primaryKeys = mapOf(
            TargetPlatform.DESKTOP to NativeKey.EnvVar("OTEL_EXPORTER_OTLP_ENDPOINT"),
            TargetPlatform.ANDROID to NativeKey.SystemProperty("otel.exporter.otlp.endpoint"),
            TargetPlatform.IOS to NativeKey.ProcessEnv("OTEL_EXPORTER_OTLP_ENDPOINT"),
            TargetPlatform.WEB to NativeKey.WindowGlobal("__OTEL_EXPORTER_OTLP_ENDPOINT__")
        ),
        fallbackKeys = mapOf(
            TargetPlatform.DESKTOP to NativeKey.SystemProperty("otel.exporter.otlp.endpoint"),
            TargetPlatform.ANDROID to NativeKey.EnvVar("OTEL_EXPORTER_OTLP_ENDPOINT"),
            TargetPlatform.IOS to NativeKey.PlistKey("OtelExporterOtlpEndpoint"),
            TargetPlatform.WEB to NativeKey.MetaTag("otel-exporter-otlp-endpoint")
        ),
        validate = { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }
    );
}
