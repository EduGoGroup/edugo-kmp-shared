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
    );
}
