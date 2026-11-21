package com.edugo.kmp.auth.token

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.telemetry.Telemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Implementación de [TokenRefreshManager] con sincronización thread-safe y retry inteligente.
 *
 * Tras la inversión de dependencias (DA-12), el manager NO conoce ni `AuthRepository` ni
 * `SafeEduGoStorage`: opera contra los puertos [TokenProvider] y [RefreshTokenSource].
 * La persistencia del nuevo token tras un refresh exitoso es responsabilidad de los
 * consumidores que escuchan [onRefreshSuccess] (ver `AuthSessionHandler`/`StorageTokenProvider`
 * en `:modules:auth-edugo`).
 */
class TokenRefreshManagerImpl(
    private val tokenProvider: TokenProvider,
    private val refreshTokenSource: RefreshTokenSource,
    private val config: TokenRefreshConfig = TokenRefreshConfig.DEFAULT,
    private val scope: CoroutineScope,
    private val telemetry: Telemetry = Telemetry.Noop,
) : TokenRefreshManager {
    private val refreshMutex = Mutex()

    /**
     * Single-flight handle for the in-flight refresh, if any. Multiple callers (reactive
     * 401 retries, proactive scheduler, manual force) coalesce on this same Deferred so
     * the server only sees one POST /auth/refresh per refresh cycle, regardless of how
     * many UI events trigger one. `@Volatile` lets the fast-path read outside the mutex
     * see the latest value on JVM/Native; on wasmJs/JS it's a no-op (single-threaded).
     */
    @Volatile
    private var refreshJob: Deferred<Result<TokenPair>>? = null

    private val _onRefreshFailed = MutableSharedFlow<RefreshFailureReason>(replay = 0)
    override val onRefreshFailed: Flow<RefreshFailureReason> = _onRefreshFailed.asSharedFlow()

    private val _onRefreshSuccess = MutableSharedFlow<TokenPair>(replay = 0)
    override val onRefreshSuccess: Flow<TokenPair> = _onRefreshSuccess.asSharedFlow()

    private var refreshScheduleJob: Job? = null

    private companion object {
        private const val MIN_SCHEDULE_DELAY_MS = 5_000L
    }

    override suspend fun refreshIfNeeded(): Result<TokenPair> {
        // Fast path: if a refresh is already in flight, piggyback on it. This is the
        // single-flight guarantee — N concurrent callers see exactly 1 POST /auth/refresh.
        refreshJob?.let { return it.await() }

        return refreshMutex.withLock {
            // Re-check inside the lock: a previous holder may have started (and finished)
            // a refresh while we were waiting on the mutex. If so, the token is fresh now
            // and shouldRefresh will be false; we don't want to fire a redundant refresh.
            refreshJob?.let { return@withLock it.await() }

            // refreshIfNeeded is called by reactive 401 retries and the proactive auto-refresh
            // scheduler — both happen during normal app usage, so they map to ApiCall.
            executeRefresh(RefreshReason.ApiCall)
        }
    }

    override suspend fun forceRefresh(reason: RefreshReason): Result<TokenPair> {
        refreshJob?.let { return it.await() }

        return refreshMutex.withLock {
            refreshJob?.let { return@withLock it.await() }

            executeRefresh(reason)
        }
    }

    /**
     * Starts a refresh job and publishes it to the single-flight slot so concurrent callers
     * can await it. Must be called while holding [refreshMutex].
     *
     * Bug 0132: el hueco lo vacía la propia renovación al terminar, no quien la espera. Si a
     * quien espera lo cancelan (el programador, al reiniciar la renovación automática en un
     * cambio de escuela), la renovación sigue en vuelo —identity-core ya puede haber rotado el
     * refresh— y quien llegue después se une a ella en vez de mandar otra vez el refresh sin
     * rotar. Solo se pone una renovación en el hueco cuando está vacío, así que nadie más que
     * su propia terminación lo vacía.
     */
    private suspend fun executeRefresh(reason: RefreshReason): Result<TokenPair> {
        val job = scope.async(start = CoroutineStart.LAZY) { performRefresh(reason) }
        refreshJob = job
        job.invokeOnCompletion { if (refreshJob === job) refreshJob = null }
        job.start()
        return job.await()
    }

    override fun shouldRefresh(token: TokenPair): Boolean {
        val now = Clock.System.now()
        val timeUntilExpiration = token.expiresAt - now
        if (timeUntilExpiration <= kotlin.time.Duration.ZERO) {
            return true
        }
        val threshold = config.refreshThresholdSeconds.seconds
        return timeUntilExpiration <= threshold
    }

    private suspend fun performRefresh(reason: RefreshReason): Result<TokenPair> {
        val refreshToken =
            when (val r = tokenProvider.currentRefreshToken()) {
                is Result.Success -> r.data
                is Result.Failure -> return handleRefreshFailure(RefreshFailureReason.NoRefreshToken)
                is Result.Loading -> return handleRefreshFailure(RefreshFailureReason.NoRefreshToken)
            }

        var lastError: Throwable? = null

        repeat(config.maxRetryAttempts + 1) { attempt ->
            if (attempt > 0) {
                val delayMs = config.calculateRetryDelay(attempt)
                delay(delayMs)
            }

            when (val result = refreshTokenSource.refresh(refreshToken, reason)) {
                is Result.Success -> {
                    val pair = result.data
                    _onRefreshSuccess.emit(pair)
                    return success(pair)
                }

                is Result.Failure -> {
                    lastError = Exception(result.error)

                    if (!isRetryableError(result.error)) {
                        val reason = mapErrorToFailureReason(result.error)
                        return handleRefreshFailure(reason)
                    }
                }

                is Result.Loading -> {
                    // No debería ocurrir en suspend function
                }
            }
        }

        val reason =
            RefreshFailureReason.NetworkError(
                lastError?.message ?: "Network error after ${config.maxRetryAttempts} retries",
            )
        return handleRefreshFailure(reason)
    }

    private suspend fun handleRefreshFailure(reason: RefreshFailureReason): Result<TokenPair> {
        _onRefreshFailed.emit(reason)
        telemetry.crash.recordException(
            Exception(reason.toLogString()),
            mapOf(
                "operation" to "performRefresh",
                "module" to "auth",
                "context" to "refresh_failure",
                "edugo.feature" to "auth",
                "error.kind" to reason.errorCode,
            ),
        )
        return failure(reason.errorCode)
    }

    private fun mapErrorToFailureReason(errorMessage: String): RefreshFailureReason =
        when {
            errorMessage.contains("expired", ignoreCase = true) ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("revoked", ignoreCase = true) ->
                RefreshFailureReason.TokenRevoked

            errorMessage.contains("invalid", ignoreCase = true) ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("401") ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("network", ignoreCase = true) ||
                errorMessage.contains("timeout", ignoreCase = true) ||
                errorMessage.contains("connection", ignoreCase = true) ->
                RefreshFailureReason.NetworkError(errorMessage)

            errorMessage.matches(Regex(".*5\\d{2}.*")) -> {
                val code = Regex("(5\\d{2})").find(errorMessage)?.value?.toIntOrNull() ?: 500
                RefreshFailureReason.ServerError(code, errorMessage)
            }

            else ->
                RefreshFailureReason.ServerError(0, errorMessage)
        }

    private fun isRetryableError(errorMessage: String): Boolean {
        // Connection refused / host unreachable = server not listening, no point retrying
        if (errorMessage.contains("refused", ignoreCase = true) ||
            errorMessage.contains("unreachable", ignoreCase = true) ||
            errorMessage.contains("-1004", ignoreCase = true)
        ) {
            return false
        }

        if (errorMessage.contains("timeout", ignoreCase = true) ||
            errorMessage.contains("network", ignoreCase = true) ||
            errorMessage.contains("connection", ignoreCase = true)
        ) {
            return true
        }

        if (errorMessage.matches(Regex(".*5\\d{2}.*"))) {
            return true
        }

        if (errorMessage.contains("expired", ignoreCase = true) ||
            errorMessage.contains("invalid", ignoreCase = true) ||
            errorMessage.contains("revoked", ignoreCase = true) ||
            errorMessage.contains("401")
        ) {
            return false
        }

        return false
    }

    override fun startAutomaticRefresh(token: TokenPair) {
        stopAutomaticRefresh()
        refreshScheduleJob =
            scope.launch {
                scheduleNextRefresh(token)
            }
    }

    override fun stopAutomaticRefresh() {
        refreshScheduleJob?.cancel()
        refreshScheduleJob = null
    }

    private suspend fun scheduleNextRefresh(token: TokenPair) {
        val now = Clock.System.now()
        val timeUntilExpiration = token.expiresAt - now
        val threshold = config.refreshThresholdSeconds.seconds
        val delayDuration = timeUntilExpiration - threshold

        val delayMs =
            delayDuration.inWholeMilliseconds
                .coerceAtLeast(MIN_SCHEDULE_DELAY_MS)

        delay(delayMs)

        // Route the proactive refresh through the same single-flight path as reactive
        // refreshes. If a 401-driven refresh is already in flight when our timer fires,
        // we await its result instead of firing a parallel POST /auth/refresh.
        // The proactive scheduler runs during normal app usage, so it maps to ApiCall
        // (Bootstrap is reserved for restoreSession on app start).
        val result = forceRefresh(RefreshReason.ApiCall)
        when (result) {
            is Result.Success -> {
                scheduleNextRefresh(result.data)
            }

            is Result.Failure -> {
                val reason = mapErrorToFailureReason(result.error)
                _onRefreshFailed.emit(reason)
            }

            is Result.Loading -> {
                // No debería ocurrir
            }
        }
    }

    /**
     * Vuelve cuando no queda ninguna renovación en vuelo (bug 0132). No la cancela: con
     * rotación, cortarla después de que identity-core rotara perdería el refresh nuevo. Es lo
     * que el logout ya obtenía al esperar el mutex, y ahora vale también para la renovación
     * cuyo programador se paró a medias.
     */
    override suspend fun cancelPendingRefresh() {
        refreshMutex.withLock {
            refreshJob?.join()
        }
    }
}
