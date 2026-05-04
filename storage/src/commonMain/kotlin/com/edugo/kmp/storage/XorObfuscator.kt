/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * XOR-based obfuscator with Base64 encoding.
 *
 * This is an intermediate obfuscation layer — NOT cryptographically secure.
 * It prevents casual inspection of sensitive values in storage (e.g., via
 * preferences viewers or plist editors) but should be replaced with AES-256
 * when a KMP-compatible crypto library is available.
 *
 * Format: "ENC:" + Base64(XOR(input, key))
 */
@OptIn(ExperimentalEncodingApi::class)
public class XorObfuscator(
    private val key: ByteArray = DEFAULT_KEY
) : StorageObfuscator {

    init {
        require(key.isNotEmpty()) { "XorObfuscator key must not be empty" }
    }

    override fun obfuscate(plainText: String): String {
        if (plainText.isEmpty()) return StorageObfuscator.OBFUSCATION_PREFIX
        val inputBytes = plainText.encodeToByteArray()
        val xored = xorWithKey(inputBytes)
        val encoded = Base64.encode(xored)
        return StorageObfuscator.OBFUSCATION_PREFIX + encoded
    }

    override fun deobfuscate(obfuscatedText: String): String {
        if (!obfuscatedText.startsWith(StorageObfuscator.OBFUSCATION_PREFIX)) {
            // Not obfuscated — return as-is (migration support)
            return obfuscatedText
        }
        val encoded = obfuscatedText.removePrefix(StorageObfuscator.OBFUSCATION_PREFIX)
        if (encoded.isEmpty()) return ""
        return try {
            val decoded = Base64.decode(encoded)
            val xored = xorWithKey(decoded)
            xored.decodeToString()
        } catch (_: IllegalArgumentException) {
            // Invalid Base64 — return original text to avoid breaking storage reads
            obfuscatedText
        } catch (_: Throwable) {
            // Any other decoding error (e.g., invalid UTF-8)
            obfuscatedText
        }
    }

    private fun xorWithKey(input: ByteArray): ByteArray {
        return ByteArray(input.size) { i ->
            (input[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
    }

    internal companion object {
        // 32-byte key for XOR obfuscation (not a secret — obfuscation only)
        val DEFAULT_KEY: ByteArray = byteArrayOf(
            0x4E, 0x64, 0x75, 0x47, 0x6F, 0x2D, 0x4B, 0x6D,
            0x70, 0x53, 0x74, 0x6F, 0x72, 0x61, 0x67, 0x65,
            0x53, 0x65, 0x63, 0x75, 0x72, 0x69, 0x74, 0x79,
            0x4B, 0x65, 0x79, 0x32, 0x30, 0x32, 0x36, 0x21
        )
    }
}
