package com.edugo.kmp.config

/**
 * Fallback configurations used when external config files
 * cannot be loaded (e.g., during unit tests or when resources
 * are not bundled).
 *
 * Single source of truth for hardcoded config values.
 * These values MUST match the JSON files in resources/config/.
 */
internal object DefaultConfigs {

    fun get(path: String): String? = configs[path]

    private val configs = mapOf(
        "config/dev.json" to """
            {
              "environmentName": "DEV",
              "network": {
                "timeout": 30000,
                "webPort": 8080,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "http://10.0.2.2:8070",
                "academicBaseUrl": "http://10.0.2.2:8060",
                "learningBaseUrl": "http://10.0.2.2:8065",
                "platformBaseUrl": "http://10.0.2.2:8075"
              },
              "telemetry": {
                "otelEndpoint": "http://10.0.2.2:4318"
              }
            }
        """.trimIndent(),

        "config/dev-lan.json" to """
            {
              "environmentName": "DEV_LAN",
              "network": {
                "timeout": 30000,
                "webPort": 8080,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "http://192.168.100.20:8070",
                "academicBaseUrl": "http://192.168.100.20:8060",
                "learningBaseUrl": "http://192.168.100.20:8065",
                "platformBaseUrl": "http://192.168.100.20:8075"
              },
              "telemetry": {
                "otelEndpoint": "http://192.168.100.20:4318"
              }
            }
        """.trimIndent(),

        "config/staging.json" to """
            {
              "environmentName": "STAGING",
              "network": {
                "timeout": 60000,
                "webPort": 8080,
                "debugMode": true
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "https://edugo-api-iam-platform.wittyhill-f6d656fb.eastus.azurecontainerapps.io",
                "academicBaseUrl": "https://edugo-api-academic.wittyhill-f6d656fb.eastus.azurecontainerapps.io",
                "learningBaseUrl": "https://edugo-api-learning.wittyhill-f6d656fb.eastus.azurecontainerapps.io",
                "platformBaseUrl": "https://edugo-api-platform.wittyhill-f6d656fb.eastus.azurecontainerapps.io"
              },
              "telemetry": {
                "otelEndpoint": ""
              }
            }
        """.trimIndent(),

        "config/prod.json" to """
            {
              "environmentName": "PROD",
              "network": {
                "timeout": 60000,
                "webPort": 80,
                "debugMode": false
              },
              "behavior": {
                "mockMode": false
              },
              "api": {
                "identityBaseUrl": "https://api.edugo.com/iam",
                "academicBaseUrl": "https://api.edugo.com/academic",
                "learningBaseUrl": "https://api.edugo.com/learning",
                "platformBaseUrl": "https://api.edugo.com/platform"
              },
              "telemetry": {
                "otelEndpoint": ""
              }
            }
        """.trimIndent()
    )
}