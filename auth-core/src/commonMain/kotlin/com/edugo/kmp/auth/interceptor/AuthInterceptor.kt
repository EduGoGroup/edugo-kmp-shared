package com.edugo.kmp.auth.interceptor

import com.edugo.kmp.network.interceptor.Interceptor
import com.edugo.kmp.network.interceptor.TokenProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

/**
 * Interceptor que agrega header Authorization con Bearer token.
 *
 * Vive en kmp-auth, usa interfaces de kmp-network.
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val headerName: String = HttpHeaders.Authorization,
    private val tokenPrefix: String = "Bearer ",
) : Interceptor {
    override val order: Int = 20

    override suspend fun interceptRequest(request: HttpRequestBuilder) {
        if (request.headers.contains(headerName)) {
            return
        }

        // getToken() already handles expiry check + refresh internally
        val token = tokenProvider.getToken()

        token?.let {
            request.header(headerName, "$tokenPrefix$it")
        }
    }

    companion object {
        fun withStaticToken(token: String): AuthInterceptor =
            AuthInterceptor(
                tokenProvider =
                    object : TokenProvider {
                        override suspend fun getToken(): String = token

                        override suspend fun refreshToken(): String = token

                        override suspend fun isTokenExpired(): Boolean = false
                    },
            )
    }
}
