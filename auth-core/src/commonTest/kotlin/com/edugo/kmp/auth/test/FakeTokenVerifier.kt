package com.edugo.kmp.auth.test

import com.edugo.kmp.auth.jwt.TokenVerificationResult
import com.edugo.kmp.auth.jwt.TokenVerifier
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success

class FakeTokenVerifier(
    var nextResult: Result<TokenVerificationResult> =
        success(
            TokenVerificationResult(
                valid = true,
                subject = "fake_user",
                expiresAt = null,
                claims = emptyMap(),
                errorCode = null,
            ),
        ),
) : TokenVerifier {
    var callCount: Int = 0
        private set
    var lastToken: String? = null
        private set

    override suspend fun verify(token: String): Result<TokenVerificationResult> {
        callCount++
        lastToken = token
        return nextResult
    }
}
