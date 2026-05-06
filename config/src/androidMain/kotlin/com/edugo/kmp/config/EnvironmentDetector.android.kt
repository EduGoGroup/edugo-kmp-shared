package com.edugo.kmp.config

// Order: JVM system property `app.environment` → env var `APP_ENVIRONMENT`. If
// neither is set with a non-blank value, fail with an actionable message.
// `BuildConfig.BUILD_ENVIRONMENT` (preferred per STANDARD.md §3.2) lives in the
// host app module; the bridge to this detector is performed in MainActivity via
// `EnvironmentDetector.forceEnvironment(...)` before Koin starts.
internal actual fun detectPlatformEnvironment(): Environment {
    val sysProp = System.getProperty("app.environment")?.trim()
    if (!sysProp.isNullOrEmpty()) {
        return Environment.fromString(sysProp)
            ?: environmentInvalidError("Android", sysProp)
    }

    val envVar = System.getenv("APP_ENVIRONMENT")?.trim()
    if (!envVar.isNullOrEmpty()) {
        return Environment.fromString(envVar)
            ?: environmentInvalidError("Android", envVar)
    }

    environmentMissingError("Android")
}
