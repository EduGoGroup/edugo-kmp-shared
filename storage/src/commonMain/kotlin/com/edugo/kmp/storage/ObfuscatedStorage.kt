/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage

import com.edugo.kmp.foundation.result.Result

/**
 * Storage wrapper that obfuscates sensitive keys before writing and
 * deobfuscates them after reading.
 *
 * Only string values for keys in [sensitiveKeys] are obfuscated.
 * All other keys and non-string operations pass through unchanged.
 *
 * Supports transparent migration: if a stored value lacks the obfuscation
 * prefix, it is returned as-is (plain text from before obfuscation was enabled).
 *
 * @param delegate The underlying [SafeEduGoStorage] to delegate operations to
 * @param obfuscator The [StorageObfuscator] used to obfuscate/deobfuscate values
 * @param sensitiveKeys The set of keys whose string values should be obfuscated
 */
public class ObfuscatedStorage(
    @PublishedApi internal val delegate: SafeEduGoStorage,
    private val obfuscator: StorageObfuscator = XorObfuscator(),
    private val sensitiveKeys: Set<String> = AUTH_SENSITIVE_KEYS
) {

    // =========================================================================
    // STRING OPERATIONS (obfuscation applied to sensitive keys)
    // =========================================================================

    public fun putStringSafe(key: String, value: String): Result<Unit> {
        val storedValue = if (key in sensitiveKeys) obfuscator.obfuscate(value) else value
        return delegate.putStringSafe(key, storedValue)
    }

    public fun getStringSafe(key: String, default: String = ""): String {
        val storedValue = delegate.getStringSafe(key, default)
        if (key !in sensitiveKeys) return storedValue
        // If the stored value is the default (key not found), return default as-is
        if (storedValue == default && !delegate.containsSafe(key)) return default
        return obfuscator.deobfuscate(storedValue)
    }

    // =========================================================================
    // PASS-THROUGH OPERATIONS (no obfuscation needed)
    // =========================================================================

    public fun putIntSafe(key: String, value: Int): Result<Unit> = delegate.putIntSafe(key, value)
    public fun getIntSafe(key: String, default: Int = 0): Int = delegate.getIntSafe(key, default)

    public fun putLongSafe(key: String, value: Long): Result<Unit> = delegate.putLongSafe(key, value)
    public fun getLongSafe(key: String, default: Long = 0L): Long = delegate.getLongSafe(key, default)

    public fun putBooleanSafe(key: String, value: Boolean): Result<Unit> = delegate.putBooleanSafe(key, value)
    public fun getBooleanSafe(key: String, default: Boolean = false): Boolean = delegate.getBooleanSafe(key, default)

    public fun putFloatSafe(key: String, value: Float): Result<Unit> = delegate.putFloatSafe(key, value)
    public fun getFloatSafe(key: String, default: Float = 0f): Float = delegate.getFloatSafe(key, default)

    public fun putDoubleSafe(key: String, value: Double): Result<Unit> = delegate.putDoubleSafe(key, value)
    public fun getDoubleSafe(key: String, default: Double = 0.0): Double = delegate.getDoubleSafe(key, default)

    public inline fun <reified T> putObjectSafe(key: String, value: T): Result<Unit> =
        delegate.putObjectSafe(key, value)

    public inline fun <reified T> getObjectSafe(key: String, default: T? = null): T? =
        delegate.getObjectSafe(key, default)

    public fun removeSafe(key: String): Result<Unit> = delegate.removeSafe(key)
    public fun containsSafe(key: String): Boolean = delegate.containsSafe(key)
    public fun clearSafe(): Result<Unit> = delegate.clearSafe()

    /** Direct access to the underlying [SafeEduGoStorage]. */
    public val safe: SafeEduGoStorage get() = delegate

    public companion object {
        /** The 3 auth keys that must be obfuscated. */
        public val AUTH_SENSITIVE_KEYS: Set<String> = setOf(
            "auth_token",
            "auth_user",
            "auth_context"
        )
    }
}
