package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Valida que [EnvVarMatrix] / [AppEnvVar] estén diseñados para soportar
 * más variables además de `APP_ENVIRONMENT` sin reescribir tests.
 *
 * Este test no agrega ninguna variable productiva nueva — usa el catálogo
 * actual y un validador hipotético para demostrar que el DSL acepta
 * múltiples variables y los validadores funcionan.
 */
internal class EnvVarMatrixExtensibilityTest {

    @Test
    fun matrixAcceptsMultipleCasesPerVariable() {
        val matrix = EnvVarMatrix.build {
            resolves(AppEnvVar.APP_ENVIRONMENT, "DEV", to = Environment.DEV)
            resolves(AppEnvVar.APP_ENVIRONMENT, "PROD", to = Environment.PROD)
            missing(AppEnvVar.APP_ENVIRONMENT)
            invalid(AppEnvVar.APP_ENVIRONMENT, "FOO")
        }
        assertEquals(4, matrix.cases.size)
        assertTrue(matrix.cases.any { it.expected is EnvVarMatrix.Outcome.ResolvesTo })
        assertTrue(matrix.cases.any { it.expected is EnvVarMatrix.Outcome.FailsWithMissing })
        assertTrue(matrix.cases.any { it.expected is EnvVarMatrix.Outcome.FailsWithInvalid })
    }

    @Test
    fun appEnvVarValidatorRejectsUnknownValues() {
        val v = AppEnvVar.APP_ENVIRONMENT
        assertTrue(v.validate("DEV"))
        assertTrue(v.validate("STAGING"))
        assertTrue(v.validate("dev"))
        assertFalse(v.validate("FOO"))
        assertFalse(v.validate(""))
    }

    @Test
    fun appEnvVarDeclaresKeysForAllPlatforms() {
        val v = AppEnvVar.APP_ENVIRONMENT
        TargetPlatform.entries.forEach { platform ->
            assertTrue(
                platform in v.primaryKeys,
                "${v.name} debe declarar primary key para $platform"
            )
            assertTrue(
                platform in v.fallbackKeys,
                "${v.name} debe declarar fallback key para $platform"
            )
        }
    }

    @Test
    fun nativeKeyTypesAreCorrectPerPlatform() {
        val v = AppEnvVar.APP_ENVIRONMENT
        assertTrue(v.primaryKeys[TargetPlatform.DESKTOP] is NativeKey.SystemProperty)
        assertTrue(v.primaryKeys[TargetPlatform.ANDROID] is NativeKey.SystemProperty)
        assertTrue(v.primaryKeys[TargetPlatform.IOS] is NativeKey.ProcessEnv)
        assertTrue(v.primaryKeys[TargetPlatform.WEB] is NativeKey.WindowGlobal)
        assertTrue(v.fallbackKeys[TargetPlatform.DESKTOP] is NativeKey.EnvVar)
        assertTrue(v.fallbackKeys[TargetPlatform.ANDROID] is NativeKey.EnvVar)
        assertTrue(v.fallbackKeys[TargetPlatform.IOS] is NativeKey.PlistKey)
        assertTrue(v.fallbackKeys[TargetPlatform.WEB] is NativeKey.MetaTag)
    }

    @Test
    fun otelEndpointDeclaresKeysForAllFourPlatforms() {
        val v = AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT
        TargetPlatform.entries.forEach { platform ->
            assertTrue(
                platform in v.primaryKeys,
                "${v.name} debe declarar primary key para $platform"
            )
            assertTrue(
                platform in v.fallbackKeys,
                "${v.name} debe declarar fallback key para $platform"
            )
        }
    }

    @Test
    fun otelEndpointValidatorAcceptsHttpAndHttps() {
        val v = AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT
        assertTrue(v.validate("http://x"))
        assertTrue(v.validate("https://x"))
    }

    @Test
    fun otelEndpointValidatorRejectsBlankAndNonUrl() {
        val v = AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT
        assertFalse(v.validate(""))
        assertFalse(v.validate("foo"))
    }

    @Test
    fun multipleVariablesIterableViaEnumEntries() {
        val entries = AppEnvVar.entries
        assertTrue(entries.size >= 2, "AppEnvVar debe tener al menos 2 variables")
        val canonicalNames = entries.map { it.canonicalName }
        assertEquals(canonicalNames.toSet().size, canonicalNames.size, "Los nombres canónicos deben ser distintos")
    }
}
