package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para ConfigLoader y AppConfig.
 */
class ConfigLoaderTest {

    @Test
    fun load_dev_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.DEV)

        assertNotNull(config)
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://localhost:8060", config.api.academicBaseUrl)
        assertEquals("http://localhost:8065", config.api.learningBaseUrl)
        assertEquals("http://localhost:8075", config.api.platformBaseUrl)
        assertEquals(8080, config.network.webPort)
        assertEquals(30000L, config.network.timeout)
        assertTrue(config.network.debugMode)
        assertEquals("http://localhost:4318", config.telemetry.otelEndpoint)
    }

    @Test
    fun load_staging_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertNotNull(config)
        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.api.academicBaseUrl.isNotBlank())
        assertTrue(config.api.learningBaseUrl.isNotBlank())
        assertTrue(config.api.identityBaseUrl.isNotBlank())
        assertTrue(config.api.platformBaseUrl.isNotBlank())
        assertEquals(8080, config.network.webPort)
        assertEquals(60000L, config.network.timeout)
        assertTrue(config.network.debugMode)
        assertEquals("", config.telemetry.otelEndpoint)
    }

    @Test
    fun load_prod_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.PROD)

        assertNotNull(config)
        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.edugo.com/academic", config.api.academicBaseUrl)
        assertEquals("https://api.edugo.com/learning", config.api.learningBaseUrl)
        assertEquals("https://api.edugo.com/platform", config.api.platformBaseUrl)
        assertEquals(80, config.network.webPort)
        assertEquals(60000L, config.network.timeout)
        assertFalse(config.network.debugMode)
        assertEquals("", config.telemetry.otelEndpoint)
    }

    @Test
    fun loadFromString_parses_json_correctly() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "network": {
                "timeout": 15000,
                "webPort": 3000,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "http://test.com:8070",
                "academicBaseUrl": "http://test.com:8060",
                "learningBaseUrl": "http://test.com:8065",
                "platformBaseUrl": "http://test.com:8075"
              }
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)

        assertNotNull(config)
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://test.com:8060", config.api.academicBaseUrl)
        assertEquals("http://test.com:8065", config.api.learningBaseUrl)
        assertEquals("http://test.com:8075", config.api.platformBaseUrl)
        assertEquals(3000, config.network.webPort)
        assertEquals(15000L, config.network.timeout)
        assertTrue(config.network.debugMode)
    }

    @Test
    fun loadFromString_ignores_unknown_keys() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "network": {
                "timeout": 15000,
                "webPort": 3000,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "http://test.com:8070",
                "academicBaseUrl": "http://test.com:8060",
                "learningBaseUrl": "http://test.com:8065",
                "platformBaseUrl": "http://test.com:8075"
              },
              "extraField": "should be ignored"
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)
        assertNotNull(config)
        assertEquals("http://test.com:8060", config.api.academicBaseUrl)
    }
}
