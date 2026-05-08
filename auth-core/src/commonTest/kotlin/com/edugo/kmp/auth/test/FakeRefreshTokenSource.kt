package com.edugo.kmp.auth.test

import com.edugo.kmp.auth.token.RefreshReason
import com.edugo.kmp.auth.token.RefreshTokenSource
import com.edugo.kmp.auth.token.TokenPair
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class FakeRefreshTokenSource(
    var nextResult: Result<TokenPair> =
        success(
            TokenPair(
                accessToken = "fake_new_access",
                expiresAt = Clock.System.now() + 1.hours,
                refreshToken = "fake_new_refresh",
            ),
        ),
) : RefreshTokenSource {
    var callCount: Int = 0
        private set
    var lastRefreshToken: String? = null
        private set
    var lastReason: RefreshReason? = null
        private set

    override suspend fun refresh(refreshToken: String, reason: RefreshReason): Result<TokenPair> {
        callCount++
        lastRefreshToken = refreshToken
        lastReason = reason
        return nextResult
    }
}
