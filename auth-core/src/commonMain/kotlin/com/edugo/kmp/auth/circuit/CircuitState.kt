package com.edugo.kmp.auth.circuit

import kotlinx.datetime.Instant

sealed class CircuitState {
    object Closed : CircuitState()

    data class Open(
        val openedAt: Instant,
    ) : CircuitState()

    data class HalfOpen(
        val attempt: Int,
    ) : CircuitState()
}
