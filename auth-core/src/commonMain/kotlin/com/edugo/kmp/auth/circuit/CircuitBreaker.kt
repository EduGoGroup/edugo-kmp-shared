package com.edugo.kmp.auth.circuit

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

class CircuitBreaker(
    private val config: CircuitBreakerConfig = CircuitBreakerConfig.default(),
) {
    private val mutex = Mutex()
    private var state: CircuitState = CircuitState.Closed
    private var failureCount: Int = 0
    private var successCount: Int = 0

    suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        // 1. Verificar estado (con lock)
        val canProceed =
            mutex.withLock {
                when (val currentState = state) {
                    is CircuitState.Open -> {
                        val elapsed = Clock.System.now() - currentState.openedAt
                        if (elapsed >= config.timeout) {
                            state = CircuitState.HalfOpen(attempt = 0)
                            successCount = 0
                            true
                        } else {
                            false
                        }
                    }

                    is CircuitState.HalfOpen -> true
                    is CircuitState.Closed -> true
                }
            }

        if (!canProceed) return Result.Failure("Circuit breaker is open")

        // 2. Ejecutar operación (sin lock) — permite concurrencia
        val result = operation()

        // 3. Actualizar estado según resultado (con lock)
        mutex.withLock {
            when (result) {
                is Result.Success -> onSuccess()
                is Result.Failure -> onFailure()
                is Result.Loading -> { // no-op
                }
            }
        }

        return result
    }

    private fun onSuccess() {
        when (state) {
            is CircuitState.HalfOpen -> {
                successCount++
                if (successCount >= config.successThreshold) {
                    state = CircuitState.Closed
                    failureCount = 0
                    successCount = 0
                }
            }

            is CircuitState.Closed -> {
                failureCount = 0
            }

            is CircuitState.Open -> { // no-op
            }
        }
    }

    private fun onFailure() {
        when (state) {
            is CircuitState.HalfOpen -> {
                state = CircuitState.Open(openedAt = Clock.System.now())
                successCount = 0
            }

            is CircuitState.Closed -> {
                failureCount++
                if (failureCount >= config.failureThreshold) {
                    state = CircuitState.Open(openedAt = Clock.System.now())
                }
            }

            is CircuitState.Open -> { // no-op
            }
        }
    }

    suspend fun getState(): CircuitState = mutex.withLock { state }

    suspend fun forceOpen(): Unit =
        mutex.withLock {
            state = CircuitState.Open(openedAt = Clock.System.now())
            failureCount = 0
            successCount = 0
        }

    suspend fun forceClose(): Unit =
        mutex.withLock {
            state = CircuitState.Closed
            failureCount = 0
            successCount = 0
        }
}
