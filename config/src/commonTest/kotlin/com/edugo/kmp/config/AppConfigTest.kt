package com.edugo.kmp.config

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para AppConfig y AppConfigImpl con submodelos.
 */
class AppConfigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun appConfigImpl_serializes_and_deserializes() {
        val config = AppConfigImpl(
            environmentName = "DEV",
            network = NetworkConfigImpl(
                timeout = 30000L,
                webPort = 3000,
                debugMode = true
            ),
            behavior = BehaviorConfigImpl(mockMode = false),
            api = ApiConfigImpl(
                identityBaseUrl = "http://localhost:8070",
                academicBaseUrl = "http://localhost:8060",
                learningBaseUrl = "http://localhost:8065",
                platformBaseUrl = "http://localhost:8075"
            )
        )

        val jsonStr = json.encodeToString(AppConfigImpl.serializer(), config)
        val deserialized = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)

        assertEquals(config, deserialized)
    }

    @Test
    fun appConfigImpl_environment_maps_from_name() {
        val config = AppConfigImpl(
            environmentName = "PROD",
            network = NetworkConfigImpl(
                timeout = 60000L,
                webPort = 80,
                debugMode = false
            ),
            behavior = BehaviorConfigImpl(mockMode = false),
            api = ApiConfigImpl(
                identityBaseUrl = "http://localhost:8070",
                academicBaseUrl = "http://localhost:8060",
                learningBaseUrl = "http://localhost:8065",
                platformBaseUrl = "http://localhost:8075"
            )
        )

        assertEquals(Environment.PROD, config.environment)
    }

    @Test
    fun appConfigImpl_environment_throws_for_unknown() {
        // Post Fase 1: AppConfigImpl no acepta defaults silenciosos para
        // environmentName. Si la deserialización trae un valor que no mapea a
        // Environment, falla con IllegalStateException accionable.
        try {
            AppConfigImpl(
                environmentName = "UNKNOWN",
                network = NetworkConfigImpl(
                    timeout = 30000L,
                    webPort = 3000,
                    debugMode = true
                ),
                behavior = BehaviorConfigImpl(mockMode = false),
                api = ApiConfigImpl(
                    identityBaseUrl = "http://localhost:8070",
                    academicBaseUrl = "http://localhost:8060",
                    learningBaseUrl = "http://localhost:8065",
                    platformBaseUrl = "http://localhost:8075"
                )
            )
            kotlin.test.fail("Should throw IllegalStateException for unknown environmentName")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("UNKNOWN"))
            assertTrue(e.message!!.contains("DEV"))
        }
    }

    @Test
    fun academicBaseUrl_returns_correct_value() {
        val config = AppConfigImpl(
            environmentName = "DEV",
            network = NetworkConfigImpl(
                timeout = 30000L,
                webPort = 3000,
                debugMode = true
            ),
            behavior = BehaviorConfigImpl(mockMode = false),
            api = ApiConfigImpl(
                identityBaseUrl = "http://localhost:8070",
                academicBaseUrl = "http://localhost:8060",
                learningBaseUrl = "http://localhost:8065",
                platformBaseUrl = "http://localhost:8075"
            )
        )

        assertEquals("http://localhost:8060", config.api.academicBaseUrl)
    }

    @Test
    fun learningBaseUrl_returns_correct_value() {
        val config = AppConfigImpl(
            environmentName = "DEV",
            network = NetworkConfigImpl(
                timeout = 30000L,
                webPort = 3000,
                debugMode = true
            ),
            behavior = BehaviorConfigImpl(mockMode = false),
            api = ApiConfigImpl(
                identityBaseUrl = "http://localhost:8070",
                academicBaseUrl = "http://localhost:8060",
                learningBaseUrl = "http://localhost:8065",
                platformBaseUrl = "http://localhost:8075"
            )
        )

        assertEquals("http://localhost:8065", config.api.learningBaseUrl)
    }

    @Test
    fun appConfigImpl_data_class_equality() {
        val config1 = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        val config2 = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )

        assertEquals(config1, config2)
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun debugMode_is_true_for_dev() {
        val config = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertTrue(config.network.debugMode)
    }

    @Test
    fun debugMode_is_false_for_prod() {
        val config = AppConfigImpl(
            "PROD",
            NetworkConfigImpl(60000L, 80, false),
            BehaviorConfigImpl(false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertFalse(config.network.debugMode)
    }

    @Test
    fun mockMode_default_is_false() {
        val config = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(mockMode = false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertFalse(config.behavior.mockMode)
    }

    @Test
    fun mockMode_can_be_enabled_for_dev() {
        val config = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(mockMode = true),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertTrue(config.behavior.mockMode)
    }

    @Test
    fun mockMode_forced_false_in_prod() {
        // Constructor should throw IllegalStateException for mockMode=true in PROD
        try {
            AppConfigImpl(
                "PROD",
                NetworkConfigImpl(60000L, 80, false),
                BehaviorConfigImpl(mockMode = true),
                ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
            )
            kotlin.test.fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            // Expected
        }
    }

    @Test
    fun mockMode_prod_json_deserialization_throws() {
        val jsonStr = """
            {
                "environmentName": "PROD",
                "network": {
                    "timeout": 60000,
                    "webPort": 80,
                    "debugMode": false
                },
                "behavior": {
                    "mockMode": true
                },
                "api": {
                    "identityBaseUrl": "https://api.edugo.com/iam",
                    "academicBaseUrl": "https://api.edugo.com/academic",
                    "learningBaseUrl": "https://api.edugo.com/learning",
                    "platformBaseUrl": "https://api.edugo.com/platform"
                }
            }
        """
        try {
            json.decodeFromString(AppConfigImpl.serializer(), jsonStr)
            kotlin.test.fail("Should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            // Expected: mockMode cannot be enabled in PROD
        }
    }

    @Test
    fun mockMode_allowed_in_staging() {
        val config = AppConfigImpl(
            "STAGING",
            NetworkConfigImpl(60000L, 8080, true),
            BehaviorConfigImpl(mockMode = true),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertTrue(config.behavior.mockMode)
    }

    @Test
    fun mockMode_deserializes_from_json() {
        val jsonStr = """
            {
                "environmentName": "DEV",
                "network": {
                    "timeout": 30000,
                    "webPort": 3000,
                    "debugMode": true
                },
                "behavior": {
                    "mockMode": true
                },
                "api": {
                    "identityBaseUrl": "http://localhost:8070",
                    "academicBaseUrl": "http://localhost:8060",
                    "learningBaseUrl": "http://localhost:8065",
                    "platformBaseUrl": "http://localhost:8075"
                }
            }
        """
        val config = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)
        assertTrue(config.behavior.mockMode)
    }

    @Test
    fun mockMode_missing_in_json_defaults_to_false() {
        val jsonStr = """
            {
                "environmentName": "DEV",
                "network": {
                    "timeout": 30000,
                    "webPort": 3000,
                    "debugMode": true
                },
                "behavior": {},
                "api": {
                    "identityBaseUrl": "http://localhost:8070",
                    "academicBaseUrl": "http://localhost:8060",
                    "learningBaseUrl": "http://localhost:8065",
                    "platformBaseUrl": "http://localhost:8075"
                }
            }
        """
        val config = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)
        assertFalse(config.behavior.mockMode)
    }

    @Test
    fun appConfigImpl_telemetry_default_when_omitted_in_constructor() {
        val config = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(mockMode = false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )
        assertEquals("", config.telemetry.otelEndpoint)
    }

    @Test
    fun new_schema_properties_are_accessible() {
        val config = AppConfigImpl(
            "DEV",
            NetworkConfigImpl(30000L, 3000, true),
            BehaviorConfigImpl(mockMode = false),
            ApiConfigImpl("http://localhost:8070", "http://localhost:8060", "http://localhost:8065", "http://localhost:8075")
        )

        // New properties via submodels should work
        assertEquals(30000L, config.network.timeout)
        assertEquals(3000, config.network.webPort)
        assertEquals(true, config.network.debugMode)
        assertEquals(false, config.behavior.mockMode)
        assertEquals("http://localhost:8070", config.api.identityBaseUrl)
        assertEquals("http://localhost:8060", config.api.academicBaseUrl)
        assertEquals("http://localhost:8065", config.api.learningBaseUrl)
        assertEquals("http://localhost:8075", config.api.platformBaseUrl)
    }
}
