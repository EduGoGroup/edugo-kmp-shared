package com.edugo.kmp.core.platform

/**
 * JVM implementation of platformSynchronized.
 *
 * Uses kotlin.synchronized for actual thread-safety on JVM platforms (Android/Desktop).
 */
actual inline fun <T> platformSynchronized(lock: Any, block: () -> T): T =
    synchronized(lock, block)
