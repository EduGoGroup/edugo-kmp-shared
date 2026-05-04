package com.edugo.kmp.auth.token

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Manager que orquestra el refresh de tokens con sincronización, retry y manejo de errores robusto.
 */
interface TokenRefreshManager {
    suspend fun refreshIfNeeded(): Result<TokenPair>

    suspend fun forceRefresh(): Result<TokenPair>

    fun shouldRefresh(token: TokenPair): Boolean

    val onRefreshFailed: Flow<RefreshFailureReason>

    val onRefreshSuccess: Flow<TokenPair>

    fun startAutomaticRefresh(token: TokenPair)

    fun stopAutomaticRefresh()

    suspend fun cancelPendingRefresh()
}
