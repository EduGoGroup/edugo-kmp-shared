package com.edugo.kmp.config

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Check system property (set via Gradle -Dapp.environment=STAGING
    //             or via JVM Args in IntelliJ Run Configuration)
    val sysProp = System.getProperty("app.environment")
    if (sysProp != null) {
        return Environment.fromString(sysProp) ?: Environment.DEV
    }

    // Strategy 2: Check environment variable (set via export APP_ENVIRONMENT=PROD)
    val envVar = System.getenv("APP_ENVIRONMENT")
    if (envVar != null) {
        return Environment.fromString(envVar) ?: Environment.DEV
    }

    // Strategy 3: Default to DEV for local development.
    // NOTE: The presence of a debugger (JDWP) is intentionally NOT used to select
    // the environment, because when debugging from IntelliJ you want to choose the
    // target environment explicitly via -Dapp.environment=<ENV> in the Run Configuration.
    return Environment.DEV
}
