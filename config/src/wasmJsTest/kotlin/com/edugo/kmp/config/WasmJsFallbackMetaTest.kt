package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests específicos de Web/WasmJS: cuando `window.__APP_ENVIRONMENT__` está
 * ausente, el detector cae al `<meta name="app-environment">` (orden
 * documentado en STANDARD.md §3.4).
 */
internal class WasmJsFallbackMetaTest {

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
    fun fallsBackToMetaTagWhenWindowGlobalIsEmpty() {
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
