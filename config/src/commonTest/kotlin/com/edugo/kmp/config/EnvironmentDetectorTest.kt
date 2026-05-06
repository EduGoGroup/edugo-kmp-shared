package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests del contrato común de [EnvironmentDetector] — sólo cubren las
 * semánticas de `forceEnvironment`/`reset` que son agnósticas de la plataforma.
 *
 * Las pruebas de detección automática (lectura de la variable real desde el
 * mecanismo nativo de cada target) y de "fallar cuando no llega la variable"
 * viven en los source sets específicos de plataforma y se construyen en la
 * Fase 4 sobre el framework parametrizado, donde podemos manipular el entorno
 * (System properties, env vars, mocks de `NSBundle`, etc.) de forma controlada.
 */
class EnvironmentDetectorTest {

    @AfterTest
    fun cleanup() {
        EnvironmentDetector.reset()
    }

    @Test
    fun forceEnvironmentOverridesDetection() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)

        val env = EnvironmentDetector.detect()

        assertEquals(Environment.STAGING, env, "Should return forced environment")
    }

    @Test
    fun multipleForceCallsUsesLatest() {
        EnvironmentDetector.forceEnvironment(Environment.DEV)
        assertEquals(Environment.DEV, EnvironmentDetector.detect())

        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())

        EnvironmentDetector.forceEnvironment(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())
    }

    @Test
    fun forcedEnvironmentPersistsAcrossMultipleDetectCalls() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)

        repeat(5) {
            assertEquals(Environment.STAGING, EnvironmentDetector.detect())
        }
    }

    @Test
    fun resetClearsTheForcedSlot() {
        EnvironmentDetector.forceEnvironment(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())

        EnvironmentDetector.reset()

        // Tras reset el slot de override está limpio: confirmamos que un nuevo
        // forceEnvironment puede tomar control sin que persista el valor previo.
        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
    }

    @Test
    fun resetIsIdempotent() {
        EnvironmentDetector.forceEnvironment(Environment.PROD)
        EnvironmentDetector.reset()
        EnvironmentDetector.reset()
        EnvironmentDetector.reset()

        EnvironmentDetector.forceEnvironment(Environment.DEV)
        assertEquals(Environment.DEV, EnvironmentDetector.detect())
    }
}
