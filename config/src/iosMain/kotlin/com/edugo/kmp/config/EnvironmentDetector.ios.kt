package com.edugo.kmp.config

import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

// Order: scheme env var `APP_ENVIRONMENT` (NSProcessInfo) → Info.plist key
// `AppEnvironment` (resolved at build time from `Config.xcconfig`). If neither
// is present with a non-blank value, fail with an actionable message.
internal actual fun detectPlatformEnvironment(): Environment {
    val processEnv = (NSProcessInfo.processInfo.environment["APP_ENVIRONMENT"] as? String)?.trim()
    if (!processEnv.isNullOrEmpty()) {
        return Environment.fromString(processEnv)
            ?: environmentInvalidError("iOS", processEnv)
    }

    val plistValue = (NSBundle.mainBundle.infoDictionary?.get("AppEnvironment") as? String)?.trim()
    if (!plistValue.isNullOrEmpty()) {
        return Environment.fromString(plistValue)
            ?: environmentInvalidError("iOS", plistValue)
    }

    environmentMissingError("iOS")
}
