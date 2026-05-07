package com.edugo.kmp.config

// Order: scheme env var `APP_ENVIRONMENT` (NSProcessInfo) → Info.plist key
// `AppEnvironment` (resolved at build time from `Config.xcconfig`). If neither
// is present with a non-blank value, fail with an actionable message.
//
// Both lookups van through `IosEnvSeam` para que los tests puedan inyectar
// fakes sin tocar `NSProcessInfo`/`NSBundle` (read-only en runtime).
internal actual fun detectPlatformEnvironment(): Environment {
    val processEnv = IosEnvSeam.envProvider("APP_ENVIRONMENT")?.trim()
    if (!processEnv.isNullOrEmpty()) {
        return Environment.fromString(processEnv)
            ?: environmentInvalidError("iOS", processEnv)
    }

    val plistValue = IosEnvSeam.plistProvider("AppEnvironment")?.trim()
    if (!plistValue.isNullOrEmpty()) {
        return Environment.fromString(plistValue)
            ?: environmentInvalidError("iOS", plistValue)
    }

    environmentMissingError("iOS")
}
