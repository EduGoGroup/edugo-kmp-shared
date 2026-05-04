package com.edugo.kmp.core.platform

/**
 * JVM implementation of PlatformVolatile.
 *
 * Maps directly to kotlin.jvm.Volatile for proper memory visibility between threads
 * on JVM platforms (Android/Desktop).
 */
actual typealias PlatformVolatile = Volatile
