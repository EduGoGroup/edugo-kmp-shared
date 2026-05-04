package com.edugo.kmp.auth.jwt

import com.edugo.kmp.auth.test.FakeTokenVerifier
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class JwtValidatorImplTest {
    private lateinit var validator: JwtValidator
    private lateinit var verifier: FakeTokenVerifier

    // Token válido con exp en el futuro (2030)
    // Payload: {"sub":"user-123","exp":1893456000}
    private val validToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyIsImV4cCI6MTg5MzQ1NjAwMH0.sig"

    // Token expirado (exp en el pasado - 2021)
    // Payload: {"sub":"user-123","exp":1604000000}
    private val expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyIsImV4cCI6MTYwNDAwMDAwMH0.sig"

    @BeforeTest
    fun setup() {
        verifier = FakeTokenVerifier()
        validator = JwtValidatorImpl(verifier)
    }

    private fun setVerifierResult(result: Result<TokenVerificationResult>) {
        verifier.nextResult = result
    }

    @Test
    fun `validate returns Valid when backend confirms`() =
        runTest {
            setVerifierResult(
                success(
                    TokenVerificationResult(
                        valid = true,
                        subject = "user-123",
                        expiresAt = Instant.parse("2030-01-01T00:00:00Z"),
                        claims = mapOf("email" to "test@edugo.com", "role" to "admin", "school_id" to "school-456"),
                        errorCode = null,
                    ),
                ),
            )

            val result = validator.validate(validToken)

            assertTrue(result is JwtValidationResult.Valid)
            val valid = result as JwtValidationResult.Valid
            assertEquals("user-123", valid.subject)
            assertEquals("admin", valid.claims["role"])
        }

    @Test
    fun `validate returns Invalid Expired for expired token without calling backend`() =
        runTest {
            val result = validator.validate(expiredToken)

            assertTrue(result is JwtValidationResult.Invalid)
            assertEquals(
                JwtValidationResult.InvalidReason.Expired,
                (result as JwtValidationResult.Invalid).reason,
            )
            assertEquals(0, verifier.callCount)
        }

    @Test
    fun `validate returns Invalid Revoked when backend says revoked`() =
        runTest {
            setVerifierResult(
                success(
                    TokenVerificationResult(
                        valid = false,
                        subject = null,
                        expiresAt = null,
                        claims = emptyMap(),
                        errorCode = "revoked",
                    ),
                ),
            )

            val result = validator.validate(validToken)

            assertTrue(result is JwtValidationResult.Invalid)
            assertEquals(
                JwtValidationResult.InvalidReason.Revoked,
                (result as JwtValidationResult.Invalid).reason,
            )
        }

    @Test
    fun `validate returns NetworkError when backend unavailable`() =
        runTest {
            setVerifierResult(failure("Network unreachable"))

            val result = validator.validate(validToken)

            assertTrue(result is JwtValidationResult.NetworkError)
        }

    @Test
    fun `validate returns Invalid Malformed for invalid token structure`() =
        runTest {
            val result = validator.validate("not-a-jwt")

            assertTrue(result is JwtValidationResult.Invalid)
            assertEquals(
                JwtValidationResult.InvalidReason.Malformed,
                (result as JwtValidationResult.Invalid).reason,
            )
        }

    @Test
    fun `validate returns Invalid Malformed for empty token`() =
        runTest {
            val result = validator.validate("")

            assertTrue(result is JwtValidationResult.Invalid)
            assertEquals(
                JwtValidationResult.InvalidReason.Malformed,
                (result as JwtValidationResult.Invalid).reason,
            )
        }

    @Test
    fun `validate returns Invalid UserInactive when backend says inactive`() =
        runTest {
            setVerifierResult(
                success(
                    TokenVerificationResult(
                        valid = false,
                        subject = null,
                        expiresAt = null,
                        claims = emptyMap(),
                        errorCode = "user_inactive",
                    ),
                ),
            )

            val result = validator.validate(validToken)

            assertTrue(result is JwtValidationResult.Invalid)
            assertEquals(
                JwtValidationResult.InvalidReason.UserInactive,
                (result as JwtValidationResult.Invalid).reason,
            )
        }

    @Test
    fun `quickValidate works offline and returns parsed claims`() {
        val result = validator.quickValidate(validToken)

        assertTrue(result is JwtParseResult.Success)
        assertEquals("user-123", (result as JwtParseResult.Success).claims.subject)
    }

    @Test
    fun `quickValidate returns error for invalid token`() {
        val result = validator.quickValidate("invalid")

        assertTrue(result is JwtParseResult.InvalidFormat)
    }

    @Test
    fun `extension isValid returns true for Valid result`() =
        runTest {
            setVerifierResult(
                success(
                    TokenVerificationResult(
                        valid = true,
                        subject = "user-123",
                        expiresAt = Instant.parse("2030-01-01T00:00:00Z"),
                        claims = mapOf("email" to "test@edugo.com", "role" to "user"),
                        errorCode = null,
                    ),
                ),
            )

            val result = validator.validate(validToken)

            assertTrue(result.isValid)
            assertFalse(result.isInvalid)
            assertFalse(result.isNetworkError)
        }

    @Test
    fun `extension isInvalid returns true for Invalid result`() =
        runTest {
            val result = validator.validate("not-a-jwt")

            assertFalse(result.isValid)
            assertTrue(result.isInvalid)
            assertFalse(result.isNetworkError)
        }

    @Test
    fun `extension getValidOrNull returns Valid when successful`() =
        runTest {
            setVerifierResult(
                success(
                    TokenVerificationResult(
                        valid = true,
                        subject = "user-123",
                        expiresAt = Instant.parse("2030-01-01T00:00:00Z"),
                        claims = mapOf("email" to "test@edugo.com", "role" to "user"),
                        errorCode = null,
                    ),
                ),
            )

            val result = validator.validate(validToken)
            val validResult = result.getValidOrNull()

            assertNotNull(validResult)
            assertEquals("user-123", validResult.subject)
        }

    @Test
    fun `extension getValidOrNull returns null when invalid`() =
        runTest {
            val result = validator.validate("invalid")
            val validResult = result.getValidOrNull()

            assertNull(validResult)
        }
}
