package com.edugo.kmp.config

import com.edugo.kmp.config.EnvironmentDetector.forceEnvironment
import kotlin.concurrent.Volatile

/**
 * Resolves the current environment (DEV, DEV_LAN, STAGING, PROD) reading the
 * canonical variable `APP_ENVIRONMENT` (or its platform-native equivalent).
 *
 * Detection strategies per platform — see `STANDARD.md` §3 for the full contract:
 * - Desktop: JVM system property `app.environment` → env var `APP_ENVIRONMENT`
 * - Android: JVM system property `app.environment` (typically populated from
 *            `BuildConfig.BUILD_ENVIRONMENT` by the host app before Koin)
 * - iOS:     `NSProcessInfo` env var `APP_ENVIRONMENT` → `Info.plist["AppEnvironment"]`
 * - Web:     `window.__APP_ENVIRONMENT__` → `<meta name="app-environment">`
 *
 * No platform falls back to silent defaults or heuristics: if the variable is
 * missing the detector throws [IllegalStateException] with an actionable message.
 *
 * Supports manual override (tests, bootstrap) via [forceEnvironment].
 */
object EnvironmentDetector {
    @Volatile
    private var manualOverride: Environment? = null

    /**
     * Detects the current environment automatically.
     *
     * If [forceEnvironment] was called, returns the forced value.
     * Otherwise, delegates to platform-specific implementation.
     *
     * Thread-safe: can be called from any coroutine or thread.
     *
     * @return The detected or forced environment
     */
    fun detect(): Environment {
        return manualOverride ?: detectPlatformEnvironment()
    }

    /**
     * Forces a specific environment, overriding automatic detection.
     * Useful for testing or manual configuration.
     *
     * Thread-safe: can be called from any coroutine or thread.
     *
     * @param environment The environment to force
     */
    fun forceEnvironment(environment: Environment) {
        manualOverride = environment
    }

    /**
     * Clears the forced environment, restoring automatic detection.
     *
     * Thread-safe: can be called from any coroutine or thread.
     */
    fun reset() {
        manualOverride = null
    }
}

/**
 * Platform-specific environment detection.
 *
 * Each platform implements its own detection strategy independently,
 * without relying on Platform.isDebug (which is unreliable on some targets).
 */
internal expect fun detectPlatformEnvironment(): Environment
