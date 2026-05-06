package com.edugo.kmp.config

// Order: JVM system property `app.environment` → env var `APP_ENVIRONMENT`. If
// neither is set with a non-blank value, fail with an actionable message.
internal actual fun detectPlatformEnvironment(): Environment {
    val sysProp = System.getProperty("app.environment")?.trim()
    if (!sysProp.isNullOrEmpty()) {
        return Environment.fromString(sysProp)
            ?: environmentInvalidError("Desktop", sysProp)
    }

    val envVar = System.getenv("APP_ENVIRONMENT")?.trim()
    if (!envVar.isNullOrEmpty()) {
        return Environment.fromString(envVar)
            ?: environmentInvalidError("Desktop", envVar)
    }

    environmentMissingError("Desktop")
}
