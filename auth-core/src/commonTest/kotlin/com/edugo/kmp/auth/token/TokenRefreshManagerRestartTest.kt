package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.test.FakeTokenProvider
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Bug 0132: reiniciar (o parar) la renovación automática mientras su renovación está en vuelo
 * no puede dejarla huérfana. Si el hueco del single-flight se vacía al cancelar a quien la
 * espera, la siguiente renovación arranca otra con el refresh aún sin rotar e identity-core
 * rechaza la segunda por reuso.
 *
 * El orden se controla a mano: la fuente retiene cada petición hasta que el test la suelta, y
 * el gestor vive en `backgroundScope`, en el despachador del test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshManagerRestartTest {
    /**
     * Fuente que rota como identity-core: cada petición queda retenida hasta [releaseNext] y,
     * al soltarse, deja el refresh nuevo en el proveedor (lo que en la app hace la fuente
     * EduGo antes de canjear).
     */
    private class HeldRefreshTokenSource(
        private val provider: FakeTokenProvider,
    ) : RefreshTokenSource {
        val sent = mutableListOf<String>()
        private val held = ArrayDeque<CompletableDeferred<Unit>>()
        private var rotation = 0

        override suspend fun refresh(
            refreshToken: String,
            reason: RefreshReason,
        ): Result<TokenPair> {
            sent += refreshToken
            val gate = CompletableDeferred<Unit>()
            held.addLast(gate)
            gate.await()
            rotation++
            provider.refreshToken = success("r$rotation")
            return success(
                TokenPair(
                    accessToken = "a$rotation", // edugo:not-a-secret
                    expiresAt = Clock.System.now() + 1.hours,
                    refreshToken = "r$rotation", // edugo:not-a-secret
                ),
            )
        }

        fun releaseNext() {
            held.removeFirst().complete(Unit)
        }
    }

    private fun expiringSoon(): TokenPair =
        TokenPair(
            accessToken = "a0",
            // Vence ya: el programador espera el mínimo (5 s) y renueva.
            expiresAt = Clock.System.now() + 1.seconds,
            refreshToken = "r0",
        )

    private fun freshToken(): TokenPair =
        TokenPair(
            accessToken = "switched", // edugo:not-a-secret
            expiresAt = Clock.System.now() + 1.hours,
            refreshToken = "r0",
        )

    private fun config() = TokenRefreshConfig(refreshThresholdSeconds = 300, maxRetryAttempts = 0, retryDelayMs = 0)

    @Test
    fun restartingTheScheduleDuringAnInFlightRefreshNeverSendsTheSameRefreshTwice() =
        runTest {
            val provider = FakeTokenProvider(refreshToken = success("r0"))
            val source = HeldRefreshTokenSource(provider)
            val manager = TokenRefreshManagerImpl(provider, source, config(), scope = backgroundScope)

            manager.startAutomaticRefresh(expiringSoon())
            advanceTimeBy(6.seconds)
            runCurrent()
            assertEquals(listOf("r0"), source.sent, "La renovación programada está en vuelo")

            // Lo que hace el cambio de escuela: reinicia el programador con el token nuevo.
            manager.startAutomaticRefresh(freshToken())
            runCurrent()

            val explicit = async { manager.forceRefresh(RefreshReason.ApiCall) }
            runCurrent()
            assertEquals(
                listOf("r0"),
                source.sent,
                "La renovación explícita se une a la que está en vuelo; no manda otra vez r0",
            )

            source.releaseNext()
            runCurrent()
            val result = explicit.await()
            assertTrue(result is Result.Success, "La renovación explícita recibe la que estaba en vuelo: $result")
            assertEquals("r1", result.data.refreshToken)

            // La siguiente ya manda el refresh rotado.
            val next = async { manager.forceRefresh(RefreshReason.ApiCall) }
            runCurrent()
            source.releaseNext()
            runCurrent()
            assertTrue(next.await() is Result.Success)
            assertEquals(listOf("r0", "r1"), source.sent)
        }

    @Test
    fun cancelPendingRefreshWaitsForTheRefreshLeftInFlightByAStoppedSchedule() =
        runTest {
            val provider = FakeTokenProvider(refreshToken = success("r0"))
            val source = HeldRefreshTokenSource(provider)
            val manager = TokenRefreshManagerImpl(provider, source, config(), scope = backgroundScope)

            manager.startAutomaticRefresh(expiringSoon())
            advanceTimeBy(6.seconds)
            runCurrent()
            manager.stopAutomaticRefresh()
            runCurrent()

            // El logout cuenta con esto: al volver no queda ninguna renovación en vuelo y el
            // refresh guardado es el último que identity-core tiene vivo.
            val pending = async { manager.cancelPendingRefresh() }
            runCurrent()
            assertFalse(pending.isCompleted, "cancelPendingRefresh espera a la renovación que sigue en vuelo")

            source.releaseNext()
            runCurrent()
            assertTrue(pending.isCompleted, "Termina cuando termina la renovación")
            assertEquals(success("r1"), provider.refreshToken, "La renovación en vuelo terminó y rotó")
            assertEquals(listOf("r0"), source.sent)
        }
}
