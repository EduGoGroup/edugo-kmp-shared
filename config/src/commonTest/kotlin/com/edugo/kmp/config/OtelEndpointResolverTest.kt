package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Cobertura de la orquestación de [OtelEndpointResolver.resolve] (commonMain).
 *
 * El orden bajo test:
 *   1. `buildOverride` (no vacío) gana sobre todo.
 *   2. `readNativeOtelEndpoint()` (no vacío) gana sobre `appConfig` + default.
 *   3. `appConfig.telemetry.otelEndpoint` (no vacío) gana sobre default.
 *   4. `defaultOtelEndpoint()` cuando todos están vacíos.
 *
 * Las fuentes nativas se mutan vía [EnvVarSource] (seam de tests). El default
 * por plataforma varía (`localhost` vs `10.0.2.2`) — se asserta sólo el shape.
 */
internal class OtelEndpointResolverTest {

    private val source = EnvVarSource()

    @BeforeTest
    fun setUp() {
        source.installSnapshot()
        AppEnvVar.entries.forEach(source::clear)
    }

    @AfterTest
    fun tearDown() {
        AppEnvVar.entries.forEach(source::clear)
        source.restoreSnapshot()
    }

    @Test
    fun buildOverrideWinsOverNativeAndAppConfig() {
        source.set(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT, "http://native:4318")
        val config = appConfigWithOtel("http://json:4318")

        val resolved = OtelEndpointResolver.resolve(config, buildOverride = "http://build:4318")

        assertEquals("http://build:4318", resolved)
    }

    @Test
    fun blankBuildOverrideFallsThroughToNative() {
        source.set(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT, "http://native:4318")
        val config = appConfigWithOtel("http://json:4318")

        val resolved = OtelEndpointResolver.resolve(config, buildOverride = "")

        assertEquals("http://native:4318", resolved)
    }

    @Test
    fun nullBuildOverrideAndBlankNativeFallsThroughToAppConfig() {
        // EnvVarSource.clear ya hizo la limpieza en setUp; reasegura intent.
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("http://json:4318")

        val resolved = OtelEndpointResolver.resolve(config, buildOverride = null)

        assertEquals("http://json:4318", resolved)
    }

    @Test
    fun emptyEverywhereFallsThroughToPlatformDefault() {
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("")

        val resolved = OtelEndpointResolver.resolve(config, buildOverride = null)

        // El default plataforma-específico es localhost:4318 (Desktop/iOS/Web)
        // o 10.0.2.2:4318 (Android). Asertar shape, no exactitud.
        assertTrue(resolved.startsWith("http://"), "Default debe ser HTTP URL: $resolved")
        assertTrue(resolved.endsWith(":4318"), "Default debe apuntar al puerto 4318: $resolved")
    }

    // ==================== resolveEnabled — punto único "vacío = off" ====================

    @Test
    fun resolveEnabledFalseWhenConfigBlankAndNoOverride() {
        // STAGING/PROD: JSON con endpoint vacío, sin build override ni nativo.
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("")

        assertFalse(
            OtelEndpointResolver.resolveEnabled(config, buildOverride = null),
            "Config vacío sin override debe desactivar la telemetría (Noop).",
        )
    }

    @Test
    fun resolveEnabledTrueWhenConfigHasLocalhost() {
        // DEV/DEV_LAN: JSON con endpoint local/LAN explícito → telemetría activa.
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("http://localhost:4318")

        assertTrue(
            OtelEndpointResolver.resolveEnabled(config, buildOverride = null),
            "Endpoint local explícito (dev) debe activar la telemetría.",
        )
    }

    @Test
    fun resolveEnabledTrueWhenBuildOverridePresentEvenIfConfigBlank() {
        // Override de build (`-PotelEndpoint=`) reactiva aunque el JSON venga vacío.
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("")

        assertTrue(
            OtelEndpointResolver.resolveEnabled(config, buildOverride = "http://build:4318"),
            "Build override no vacío debe activar la telemetría aunque el config esté vacío.",
        )
    }

    @Test
    fun resolveEnabledTrueWhenNativeSourcePresentEvenIfConfigBlank() {
        // Fuente nativa (env var / system property) reactiva aunque el JSON venga vacío.
        source.set(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT, "http://native:4318")
        val config = appConfigWithOtel("")

        assertTrue(
            OtelEndpointResolver.resolveEnabled(config, buildOverride = null),
            "Fuente nativa no vacía debe activar la telemetría aunque el config esté vacío.",
        )
    }

    @Test
    fun resolveEnabledFalseWhenBlankBuildOverrideAndBlankEverything() {
        // Build override "" (no null) tampoco cuenta como fuente real.
        source.clear(AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT)
        val config = appConfigWithOtel("")

        assertFalse(
            OtelEndpointResolver.resolveEnabled(config, buildOverride = ""),
            "Build override en blanco no cuenta como fuente: debe quedar desactivado.",
        )
    }

    private fun appConfigWithOtel(otelEndpoint: String): AppConfig = AppConfigImpl(
        environmentName = "DEV",
        network = NetworkConfigImpl(
            timeout = 30000L,
            webPort = 3000,
            debugMode = true,
        ),
        behavior = BehaviorConfigImpl(mockMode = false),
        api = ApiConfigImpl(
            identityBaseUrl = "http://localhost:8070",
            academicBaseUrl = "http://localhost:8060",
            learningBaseUrl = "http://localhost:8065",
            platformBaseUrl = "http://localhost:8075",
        ),
        telemetry = TelemetryConfigImpl(otelEndpoint = otelEndpoint),
    )
}
