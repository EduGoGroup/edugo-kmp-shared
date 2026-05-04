package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.test.FakeRefreshTokenSource
import com.edugo.kmp.auth.test.FakeTokenProvider
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Tras la inversión de dependencias (DA-12, Fase 5) los tests construyen el
 * manager con dobles `FakeTokenProvider` / `FakeRefreshTokenSource` en lugar
 * de `StubAuthRepository`. La semántica funcional (auto-refresh schedule, stop,
 * replace, min delay) se preserva 1:1.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshManagerAutoRefreshTest {
    private fun createManager(
        testScope: TestScope,
        source: FakeRefreshTokenSource =
            FakeRefreshTokenSource(
                nextResult =
                    success(
                        TokenPair(
                            accessToken = "new_access",
                            expiresAt = Clock.System.now() + 1.hours,
                            refreshToken = "new_refresh",
                        ),
                    ),
            ),
        provider: FakeTokenProvider = FakeTokenProvider(),
        config: TokenRefreshConfig =
            TokenRefreshConfig(
                refreshThresholdSeconds = 300,
                maxRetryAttempts = 1,
                retryDelayMs = 100,
            ),
    ): TokenRefreshManagerImpl =
        TokenRefreshManagerImpl(
            tokenProvider = provider,
            refreshTokenSource = source,
            config = config,
            scope = testScope,
        )

    private fun createTokenPair(secondsUntilExpiry: Long): TokenPair {
        val now = Clock.System.now()
        return TokenPair(
            accessToken = "test_access_token",
            expiresAt = now + secondsUntilExpiry.seconds,
            refreshToken = "test_refresh_token",
        )
    }

    @Test
    fun testStartAutomaticRefreshSchedulesRefresh() =
        runTest {
            val manager = createManager(testScope = this)
            val token = createTokenPair(600)

            var refreshedPair: TokenPair? = null
            val collectJob =
                launch {
                    manager.onRefreshSuccess.first().let { refreshedPair = it }
                }

            manager.startAutomaticRefresh(token)

            // threshold=300s, token expires in 600s => delay = 600-300 = 300s (> MIN 5s)
            advanceTimeBy(301.seconds)

            collectJob.cancel()
            manager.stopAutomaticRefresh()

            assertTrue(refreshedPair != null, "startAutomaticRefresh() should have emitted a success after 300s")
        }

    @Test
    fun testStopAutomaticRefreshCancelsScheduledJob() =
        runTest {
            val manager = createManager(testScope = this)
            val token = createTokenPair(600)

            var refreshTriggered = false
            val collectJob =
                launch {
                    manager.onRefreshSuccess.collect { refreshTriggered = true }
                }

            manager.startAutomaticRefresh(token)
            manager.stopAutomaticRefresh()

            // Advancing time well past the scheduled delay should NOT trigger refresh after stopping
            advanceTimeBy(600.seconds)

            collectJob.cancel()

            assertFalse(refreshTriggered, "No refresh should occur after stopAutomaticRefresh()")
        }

    @Test
    fun testStartAutomaticRefreshReplacesExistingSchedule() =
        runTest {
            val manager = createManager(testScope = this)

            var refreshCount = 0
            val collectJob =
                launch {
                    manager.onRefreshSuccess.collect { refreshCount++ }
                }

            val token1 = createTokenPair(600)
            manager.startAutomaticRefresh(token1)
            // token1 schedule: delay = 600-300 = 300s

            val token2 = createTokenPair(1200)
            manager.startAutomaticRefresh(token2)
            // Calling startAutomaticRefresh again must cancel token1's job
            // token2 schedule: delay = 1200-300 = 900s

            // Advance past token1's would-be time (300s) but before token2's (900s)
            advanceTimeBy(400.seconds)

            manager.stopAutomaticRefresh()
            collectJob.cancel()

            assertEquals(0, refreshCount, "First schedule should be cancelled when startAutomaticRefresh() is called again")
        }

    @Test
    fun testOnRefreshSuccessEmitsOnAutoRefresh() =
        runTest {
            val config =
                TokenRefreshConfig(
                    refreshThresholdSeconds = 5,
                    maxRetryAttempts = 0,
                    retryDelayMs = 0,
                )
            val manager = createManager(testScope = this, config = config)

            val token = createTokenPair(10)

            var successEmitted = false
            val collectJob =
                launch {
                    manager.onRefreshSuccess.collect {
                        successEmitted = true
                    }
                }

            manager.startAutomaticRefresh(token)

            // threshold=5s, token expires in 10s => delay=5s, min 5s
            advanceTimeBy(6.seconds)

            manager.stopAutomaticRefresh()
            collectJob.cancel()

            assertTrue(successEmitted, "onRefreshSuccess should have been emitted")
        }

    @Test
    fun testAutoRefreshUsesMinDelayForNearExpiry() =
        runTest {
            val config =
                TokenRefreshConfig(
                    refreshThresholdSeconds = 300,
                    maxRetryAttempts = 0,
                    retryDelayMs = 0,
                )
            val manager = createManager(testScope = this, config = config)

            // Token expires in 10s with threshold 300s => computed delay is -290s, clamped to MIN_SCHEDULE_DELAY_MS (5s)
            val token = createTokenPair(10)

            var refreshTriggered = false
            val collectJob =
                launch {
                    manager.onRefreshSuccess.collect { refreshTriggered = true }
                }

            manager.startAutomaticRefresh(token)

            // After 6s (> MIN_SCHEDULE_DELAY_MS of 5s), the refresh should have triggered
            advanceTimeBy(6.seconds)

            manager.stopAutomaticRefresh()
            collectJob.cancel()

            assertTrue(refreshTriggered, "Near-expiry token should trigger refresh using minimum delay (5s)")
        }
}
