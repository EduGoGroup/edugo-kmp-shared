package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests específicos de iOS: cuando `NSProcessInfo` env var está vacía, el
 * detector cae al `Info.plist["AppEnvironment"]` (orden documentado en
 * STANDARD.md §3.3).
 */
internal class IosFallbackPlistTest {

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
    fun fallsBackToPlistWhenProcessEnvIsEmpty() {
        // Sólo Plist está poblado; ProcessEnv vacío
        source.setFallback(AppEnvVar.APP_ENVIRONMENT, "STAGING")
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())
    }

    @Test
    fun primarySourceWinsOverFallback() {
        source.set(AppEnvVar.APP_ENVIRONMENT, "PROD")
        source.setFallback(AppEnvVar.APP_ENVIRONMENT, "DEV")
        assertEquals(Environment.PROD, EnvironmentDetector.detect())
    }
}
