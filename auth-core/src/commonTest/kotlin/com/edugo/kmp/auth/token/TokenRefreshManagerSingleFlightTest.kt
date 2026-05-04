package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.test.FakeRefreshTokenSource
import com.edugo.kmp.auth.test.FakeTokenProvider
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Regression tests for F6·H5.a — N concurrent callers must collapse onto a single
 * POST /auth/refresh. Without single-flight, 4 simultaneous clicks would each fire
 * their own refresh, generating N× traffic to identity even though only one is needed.
 *
 * Tras la inversión de dependencias (DA-12, Fase 5) los tests se construyen
 * contra los puertos `TokenProvider` / `RefreshTokenSource` con dobles `Fake*`
 * en lugar de `StubAuthRepository`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshManagerSingleFlightTest {
    /**
     * Decorator alrededor de [FakeRefreshTokenSource] que añade un retraso por
     * llamada para simular latencia de red y forzar concurrencia real.
     */
    private class DelayingRefreshTokenSource(
        private val delegate: FakeRefreshTokenSource,
        private val responseDelayMs: Long,
    ) : RefreshTokenSource {
        val callCount: Int get() = delegate.callCount

        override suspend fun refresh(refreshToken: String): Result<TokenPair> {
            if (responseDelayMs > 0) delay(responseDelayMs)
            return delegate.refresh(refreshToken)
        }
    }

    private fun createSource(responseDelayMs: Long = 50): DelayingRefreshTokenSource {
        val fake =
            FakeRefreshTokenSource(
                nextResult =
                    success(
                        TokenPair(
                            accessToken = "new_access_token",
                            expiresAt = Clock.System.now() + 1.hours,
                            refreshToken = "new_refresh_token",
                        ),
                    ),
            )
        return DelayingRefreshTokenSource(fake, responseDelayMs)
    }

    private fun createManager(
        scope: TestScope,
        source: RefreshTokenSource,
        provider: FakeTokenProvider = FakeTokenProvider(),
    ): TokenRefreshManagerImpl =
        TokenRefreshManagerImpl(
            tokenProvider = provider,
            refreshTokenSource = source,
            config =
                TokenRefreshConfig(
                    refreshThresholdSeconds = 300,
                    maxRetryAttempts = 0,
                    retryDelayMs = 0,
                ),
            scope = scope,
        )

    @Test
    fun fourConcurrentRefreshIfNeededCallersFireExactlyOneServerRefresh() =
        runTest {
            val source = createSource()
            val manager = createManager(this, source)

            val results = (1..4).map { async { manager.refreshIfNeeded() } }.awaitAll()

            assertEquals(
                1,
                source.callCount,
                "4 concurrent refreshIfNeeded() callers must collapse to a single POST /auth/refresh",
            )
            results.forEach { result ->
                check(result is Result.Success) {
                    "Each caller should receive the same successful token, got $result"
                }
            }
        }

    @Test
    fun fourConcurrentForceRefreshCallersFireExactlyOneServerRefresh() =
        runTest {
            val source = createSource()
            val manager = createManager(this, source)

            val results = (1..4).map { async { manager.forceRefresh() } }.awaitAll()

            assertEquals(
                1,
                source.callCount,
                "4 concurrent forceRefresh() callers must collapse to a single POST /auth/refresh",
            )
            results.forEach { result ->
                check(result is Result.Success)
            }
        }

    @Test
    fun mixedRefreshIfNeededAndForceRefreshCollapseToSingleRefresh() =
        runTest {
            val source = createSource()
            val manager = createManager(this, source)

            val results =
                listOf(
                    async { manager.refreshIfNeeded() },
                    async { manager.forceRefresh() },
                    async { manager.refreshIfNeeded() },
                    async { manager.forceRefresh() },
                ).awaitAll()

            assertEquals(
                1,
                source.callCount,
                "Mixed concurrent callers (refreshIfNeeded + forceRefresh) must single-flight",
            )
            results.forEach { result ->
                check(result is Result.Success)
            }
        }
}
