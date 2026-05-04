package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigLoaderIntegrationTest {

    @AfterTest
    fun cleanup() {
        EnvironmentDetector.reset()
    }

    @Test
    fun loadDevConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.DEV)

        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://localhost:8060", config.api.academicBaseUrl)
        assertEquals("http://localhost:8065", config.api.learningBaseUrl)
        assertEquals("http://localhost:8075", config.api.platformBaseUrl)
        assertEquals(8080, config.network.webPort)
        assertEquals(30000L, config.network.timeout)
        assertEquals(true, config.network.debugMode)
    }

    @Test
    fun loadStagingConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.api.academicBaseUrl.isNotBlank())
        assertTrue(config.api.learningBaseUrl.isNotBlank())
        assertTrue(config.api.identityBaseUrl.isNotBlank())
        assertTrue(config.api.platformBaseUrl.isNotBlank())
        assertEquals(60000L, config.network.timeout)
        assertEquals(true, config.network.debugMode)
    }

    @Test
    fun loadProdConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.PROD)

        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.edugo.com/academic", config.api.academicBaseUrl)
        assertEquals("https://api.edugo.com/learning", config.api.learningBaseUrl)
        assertEquals("https://api.edugo.com/platform", config.api.platformBaseUrl)
        assertEquals(80, config.network.webPort)
        assertEquals(60000L, config.network.timeout)
        assertEquals(false, config.network.debugMode)
    }

    @Test
    fun forceEnvironmentAffectsConfigLoading() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        val env = EnvironmentDetector.detect()
        val config = ConfigLoader.load(env)

        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.api.academicBaseUrl.isNotBlank())
    }

    @Test
    fun loadFromStringParsesValidJson() {
        val json = """
            {
              "environmentName": "DEV",
              "network": {
                "timeout": 5000,
                "webPort": 3000,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "http://custom-server:8070",
                "academicBaseUrl": "http://custom-server:8060",
                "learningBaseUrl": "http://custom-server:8065",
                "platformBaseUrl": "http://custom-server:8075"
              }
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(json)

        assertEquals("http://custom-server:8060", config.api.academicBaseUrl)
        assertEquals("http://custom-server:8065", config.api.learningBaseUrl)
        assertEquals("http://custom-server:8075", config.api.platformBaseUrl)
    }
}
