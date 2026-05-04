package com.edugo.kmp.auth.throttle

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class RateLimiter(
    private val maxRequests: Int = 5,
    private val timeWindow: Duration = 1.minutes,
) {
    private val mutex = Mutex()
    private val timestamps = mutableListOf<Instant>()

    init {
        require(maxRequests > 0) { "maxRequests debe ser > 0" }
        require(timeWindow.isPositive()) { "timeWindow debe ser positivo" }
    }

    suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> =
        mutex.withLock {
            cleanExpired()
            if (timestamps.size >= maxRequests) {
                Result.Failure("Rate limit exceeded")
            } else {
                timestamps.add(Clock.System.now())
                operation()
            }
        }

    suspend fun remainingRequests(): Int =
        mutex.withLock {
            cleanExpired()
            (maxRequests - timestamps.size).coerceAtLeast(0)
        }

    suspend fun reset(): Unit =
        mutex.withLock {
            timestamps.clear()
        }

    private fun cleanExpired() {
        val cutoff = Clock.System.now() - timeWindow
        timestamps.removeAll { it < cutoff }
    }
}
