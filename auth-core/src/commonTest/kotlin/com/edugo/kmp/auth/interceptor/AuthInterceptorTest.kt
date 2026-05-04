package com.edugo.kmp.auth.interceptor

import com.edugo.kmp.network.interceptor.TokenProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthInterceptorTest {
    private class StubTokenProvider(
        private val token: String? = "test-token-123",
        private val expired: Boolean = false,
    ) : TokenProvider {
        var getTokenCallCount = 0
        var refreshTokenCallCount = 0

        override suspend fun getToken(): String? {
            getTokenCallCount++
            return token
        }

        override suspend fun refreshToken(): String? {
            refreshTokenCallCount++
            return token
        }

        override suspend fun isTokenExpired(): Boolean = expired
    }

    @Test
    fun interceptRequest_adds_bearer_token() =
        runTest {
            val provider = StubTokenProvider(token = "my-token")
            val interceptor = AuthInterceptor(provider)
            val request = HttpRequestBuilder()

            interceptor.interceptRequest(request)

            assertEquals("Bearer my-token", request.headers[HttpHeaders.Authorization])
        }

    @Test
    fun interceptRequest_skips_when_header_exists() =
        runTest {
            val provider = StubTokenProvider(token = "new-token")
            val interceptor = AuthInterceptor(provider)
            val request =
                HttpRequestBuilder().apply {
                    header(HttpHeaders.Authorization, "Bearer existing-token")
                }

            interceptor.interceptRequest(request)

            assertEquals("Bearer existing-token", request.headers[HttpHeaders.Authorization])
            assertEquals(0, provider.getTokenCallCount)
        }

    @Test
    fun interceptRequest_uses_getToken_not_refresh() =
        runTest {
            val provider = StubTokenProvider(token = "some-token")
            val interceptor = AuthInterceptor(provider)
            val request = HttpRequestBuilder()

            interceptor.interceptRequest(request)

            assertEquals(1, provider.getTokenCallCount)
            assertEquals(0, provider.refreshTokenCallCount)
        }

    @Test
    fun interceptRequest_handles_null_token() =
        runTest {
            val provider = StubTokenProvider(token = null)
            val interceptor = AuthInterceptor(provider)
            val request = HttpRequestBuilder()

            interceptor.interceptRequest(request)

            assertNull(request.headers[HttpHeaders.Authorization])
        }

    @Test
    fun withStaticToken_creates_interceptor_with_fixed_token() =
        runTest {
            val interceptor = AuthInterceptor.withStaticToken("static-abc")
            val request = HttpRequestBuilder()

            interceptor.interceptRequest(request)

            assertEquals("Bearer static-abc", request.headers[HttpHeaders.Authorization])
        }

    @Test
    fun order_is_20() {
        val provider = StubTokenProvider()
        val interceptor = AuthInterceptor(provider)

        assertEquals(20, interceptor.order)
    }
}
