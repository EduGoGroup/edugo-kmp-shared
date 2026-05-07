package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Contrato parametrizado que cada plataforma debe satisfacer.
 *
 * El framework de Fase 4 promete que todas las plataformas comparten el
 * mismo comportamiento observable:
 *
 * 1. Resuelven los 4 valores de `Environment` desde su fuente primaria.
 * 2. Hacen `trim()` y son case-insensitive.
 * 3. Fallan accionablemente cuando no hay valor (mensaje menciona
 *    `APP_ENVIRONMENT`, la plataforma y los 4 valores válidos).
 * 4. Fallan accionablemente cuando el valor es inválido (mensaje incluye
 *    el `rawValue` y los 4 valores válidos).
 * 5. `EnvironmentDetector.forceEnvironment(...)` tiene precedencia sobre
 *    cualquier valor en la fuente nativa.
 *
 * Cada subclase per-plataforma sólo declara su [expectedPlatformLabel] —
 * la lógica de aserciones vive aquí.
 */
internal abstract class EnvironmentDetectorContractTest {

    protected abstract val expectedPlatformLabel: String

    private val source = EnvVarSource()

    @BeforeTest
    fun setUp() {
        source.installSnapshot()
        AppEnvVar.entries.forEach(source::clear)
        EnvironmentDetector.reset()
    }

    @AfterTest
    fun tearDown() {
        AppEnvVar.entries.forEach(source::clear)
        EnvironmentDetector.reset()
        source.restoreSnapshot()
    }

    @Test
    fun resolvesEachEnvironmentValue() {
        val matrix = EnvVarMatrix.build {
            resolves(AppEnvVar.APP_ENVIRONMENT, "DEV", to = Environment.DEV)
            resolves(AppEnvVar.APP_ENVIRONMENT, "DEV_LAN", to = Environment.DEV_LAN)
            resolves(AppEnvVar.APP_ENVIRONMENT, "STAGING", to = Environment.STAGING)
            resolves(AppEnvVar.APP_ENVIRONMENT, "PROD", to = Environment.PROD)
        }
        assertResolvesAll(matrix)
    }

    @Test
    fun trimsAndIsCaseInsensitive() {
        val matrix = EnvVarMatrix.build {
            resolves(AppEnvVar.APP_ENVIRONMENT, "  staging  ", to = Environment.STAGING)
            resolves(AppEnvVar.APP_ENVIRONMENT, "Dev", to = Environment.DEV)
            resolves(AppEnvVar.APP_ENVIRONMENT, "prod", to = Environment.PROD)
            resolves(AppEnvVar.APP_ENVIRONMENT, "dev_lan", to = Environment.DEV_LAN)
        }
        assertResolvesAll(matrix)
    }

    @Test
    fun failsActionablyWhenMissing() {
        AppEnvVar.entries.forEach(source::clear)
        val ex = assertFailsWith<IllegalStateException> {
            EnvironmentDetector.detect()
        }
        val msg = ex.message ?: fail("Excepción sin mensaje")
        assertTrue("APP_ENVIRONMENT" in msg, "Mensaje debe mencionar APP_ENVIRONMENT: $msg")
        // El hint de missing lista las 4 plataformas, así que basta con buscar
        // el label suelto sería un falso positivo. Verificamos la marca
        // discriminante "(plataforma: <Label>)" que produce environmentMissingError.
        val platformMarker = "(plataforma: $expectedPlatformLabel)"
        assertTrue(
            platformMarker in msg,
            "Mensaje debe identificar la plataforma actual con '$platformMarker': $msg"
        )
        Environment.entries.forEach { env ->
            assertTrue(env.name in msg, "Mensaje debe mencionar valor válido ${env.name}: $msg")
        }
    }

    @Test
    fun failsActionablyWhenInvalid() {
        source.set(AppEnvVar.APP_ENVIRONMENT, "FOO")
        val ex = assertFailsWith<IllegalStateException> { EnvironmentDetector.detect() }
        val msg = ex.message ?: fail("Excepción sin mensaje")
        assertTrue("APP_ENVIRONMENT" in msg, "Mensaje debe mencionar APP_ENVIRONMENT: $msg")
        assertTrue("FOO" in msg, "Mensaje debe ecoar el valor inválido: $msg")
        val platformMarker = "(plataforma: $expectedPlatformLabel)"
        assertTrue(
            platformMarker in msg,
            "Mensaje debe identificar la plataforma actual con '$platformMarker': $msg"
        )
    }

    @Test
    fun forceEnvironmentBeatsNativeSource() {
        source.set(AppEnvVar.APP_ENVIRONMENT, "STAGING")
        EnvironmentDetector.forceEnvironment(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())

        EnvironmentDetector.reset()
        // Tras reset, vuelve a leer de la fuente nativa.
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
    }

    private fun assertResolvesAll(matrix: EnvVarMatrix) {
        matrix.cases.forEach { case ->
            val raw = case.rawValue
                ?: fail("Test data error: case con rawValue=null para ResolvesTo")
            val expected = (case.expected as? EnvVarMatrix.Outcome.ResolvesTo)
                ?: fail("Test data error: case sin Outcome.ResolvesTo")
            source.clear(case.variable)
            source.set(case.variable, raw)
            assertEquals(
                expected.env,
                EnvironmentDetector.detect(),
                "Caso ${case.variable.name}=\"$raw\" en $expectedPlatformLabel"
            )
        }
    }
}
