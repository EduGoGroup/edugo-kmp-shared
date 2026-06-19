package com.edugo.kmp.config

/**
 * Resuelve `OTEL_EXPORTER_OTLP_ENDPOINT` con orden uniforme cross-platform
 * (ver `STANDARD.md §3.2`):
 *
 *   1. `buildOverride` — bake-de-build (Android `BuildConfig.OTEL_EXPORTER_OTLP_ENDPOINT`,
 *      Web `BUILD_OTEL_ENDPOINT`). `null` o vacío en plataformas sin canal de bake
 *      (Desktop, iOS).
 *   2. [readNativeOtelEndpoint] — override runtime nativo:
 *        · Desktop: env var `OTEL_EXPORTER_OTLP_ENDPOINT` → system property `otel.exporter.otlp.endpoint`.
 *        · Android: system property `otel.exporter.otlp.endpoint` → env var `OTEL_EXPORTER_OTLP_ENDPOINT`.
 *        · iOS: `NSProcessInfo["OTEL_EXPORTER_OTLP_ENDPOINT"]` → `Info.plist["OtelExporterOtlpEndpoint"]`.
 *        · Web: `window.__OTEL_EXPORTER_OTLP_ENDPOINT__` → `<meta name="otel-exporter-otlp-endpoint">`.
 *   3. `appConfig.telemetry.otelEndpoint` — JSON del entorno cargado por [ConfigLoader].
 *   4. [defaultOtelEndpoint] — default-plataforma: `http://10.0.2.2:4318` en Android
 *      (emulador → host); `http://localhost:4318` en Desktop/iOS/Web.
 *
 * Esta indirección es análoga a [EnvironmentDetector]: la lógica de orquestación
 * vive en `commonMain` y cada plataforma sólo provee los lectores nativos. Cierra
 * la deuda de `STANDARD.md §7` que la primera iteración de DA-MPH-2 dejó como
 * resolvers inline duplicados.
 */
object OtelEndpointResolver {
    /**
     * Resuelve el endpoint final aplicando el orden uniforme.
     *
     * @param appConfig Config del entorno actual (red de seguridad por JSON).
     * @param buildOverride Constante baked-by-build cuando la plataforma la
     *                      expone (Android `BuildConfig`, Web `BUILD_OTEL_ENDPOINT`).
     *                      `null` en plataformas sin canal de bake.
     * @return URL del Collector OTLP/HTTP — siempre no-vacío.
     */
    fun resolve(appConfig: AppConfig, buildOverride: String? = null): String {
        buildOverride?.takeIf { it.isNotBlank() }?.let { return it }
        readNativeOtelEndpoint()?.takeIf { it.isNotBlank() }?.let { return it }
        appConfig.telemetry.otelEndpoint.takeIf { it.isNotBlank() }?.let { return it }
        return defaultOtelEndpoint()
    }

    /**
     * Punto ÚNICO cross-platform de la regla "sin endpoint OTLP real →
     * telemetría desactivada" (ADR 0027 — collectors cloud retirados).
     *
     * Devuelve `true` solo si alguna fuente **real** aporta endpoint:
     *   · `buildOverride` no vacío (Android `BuildConfig`, Web `BUILD_OTEL_ENDPOINT`), o
     *   · [readNativeOtelEndpoint] no vacío (env var / system property / Info.plist / meta tag), o
     *   · `appConfig.telemetry.otelEndpoint` no vacío (JSON del entorno: DEV/DEV_LAN traen
     *     `localhost`/LAN explícito; STAGING/PROD lo traen vacío → desactivado).
     *
     * NO usa [resolve]: aquel cae a [defaultOtelEndpoint] (siempre `localhost:4318`)
     * cuando todo viene vacío, por lo que "vacío = off" debe decidirse mirando las
     * fuentes reales, no el endpoint ya resuelto. Si esto da `false`, el callsite
     * de cada plataforma construye su `TelemetryConfig` con `enabled = false` y el
     * factory devuelve `Telemetry.Noop` sin inicializar el exporter OTLP. Así la
     * regla la heredan web, Android e iOS (y Desktop) desde un solo lugar, sin
     * guards duplicados por plataforma.
     *
     * @param appConfig Config del entorno actual.
     * @param buildOverride Constante baked-by-build cuando la plataforma la expone;
     *                      `null` en plataformas sin canal de bake (Desktop, iOS).
     * @return `true` si la telemetría debe activarse (hay endpoint real),
     *         `false` si debe quedar en `Telemetry.Noop`.
     */
    fun resolveEnabled(appConfig: AppConfig, buildOverride: String? = null): Boolean {
        if (buildOverride?.isNotBlank() == true) return true
        if (readNativeOtelEndpoint()?.isNotBlank() == true) return true
        return appConfig.telemetry.otelEndpoint.isNotBlank()
    }
}

/**
 * Lectura runtime del endpoint OTel desde fuentes nativas. Devuelve `null` si
 * ni la primaria ni la fallback (cuando la plataforma define una) tienen valor
 * no-vacío.
 */
internal expect fun readNativeOtelEndpoint(): String?

/** Default por plataforma — `10.0.2.2` en Android emulator, `localhost` en el resto. */
internal expect fun defaultOtelEndpoint(): String
