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
                "identityBaseUrl": "http://localhost:8070",
                "academicBaseUrl": "http://localhost:8060",
                "learningBaseUrl": "http://localhost:8065",
                "platformBaseUrl": "http://localhost:8075"
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
              }
            }
        """.trimIndent()
    )
}