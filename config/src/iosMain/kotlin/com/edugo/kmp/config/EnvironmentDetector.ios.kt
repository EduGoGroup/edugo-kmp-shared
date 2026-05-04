package com.edugo.kmp.config

import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Runtime env var set by Xcode scheme (APP_ENVIRONMENT=STAGING/DEV)
    // This is what the scheme's "Environment Variables" section injects at launch time.
    val processEnv = NSProcessInfo.processInfo.environment["APP_ENVIRONMENT"] as? String
    if (processEnv != null) {
        return Environment.fromString(processEnv) ?: Environment.DEV
    }

    // Strategy 2: Info.plist build-time substitution ($(APP_ENVIRONMENT) from xcconfig)
    val infoDictionary = NSBundle.mainBundle.infoDictionary
    val plistValue = infoDictionary?.get("AppEnvironment") as? String
    if (plistValue != null) {
        return Environment.fromString(plistValue) ?: Environment.DEV
    }

    return Environment.DEV
}
