package com.edugo.kmp.auth.test

import com.edugo.kmp.auth.token.TokenProvider
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success

class FakeTokenProvider(
    var accessToken: Result<String> = success("fake_access_token"),
    var refreshToken: Result<String> = success("fake_refresh_token"),
) : TokenProvider {
    var clearCallCount: Int = 0
        private set

    override suspend fun currentAccessToken(): Result<String> = accessToken

    override suspend fun currentRefreshToken(): Result<String> = refreshToken

    override suspend fun clearTokens() {
        clearCallCount++
    }
}
