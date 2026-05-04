/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage

/**
 * Interface for obfuscating/deobfuscating sensitive storage values.
 *
 * Current implementation uses XOR + Base64 as an intermediate obfuscation layer.
 * Designed to be replaced with AES-256 encryption when a KMP-compatible crypto
 * library is adopted.
 */
public interface StorageObfuscator {
    /**
     * Obfuscates a plain text string for storage.
     *
     * @param plainText The original text to obfuscate
     * @return The obfuscated string (prefixed with [OBFUSCATION_PREFIX])
     */
    public fun obfuscate(plainText: String): String

    /**
     * Deobfuscates a previously obfuscated string.
     *
     * @param obfuscatedText The obfuscated string, optionally prefixed with [OBFUSCATION_PREFIX].
     * Values without the prefix are returned unchanged to support migration from plain-text storage.
     * @return The original plain text, or the input value if it was not prefixed
     */
    public fun deobfuscate(obfuscatedText: String): String

    public companion object {
        /** Prefix added to obfuscated values to distinguish them from plain text. */
        public const val OBFUSCATION_PREFIX: String = "ENC:"
    }
}
