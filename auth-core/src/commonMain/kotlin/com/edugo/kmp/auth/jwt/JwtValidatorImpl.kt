package com.edugo.kmp.auth.jwt

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.telemetry.Telemetry
import kotlin.time.Instant

/**
 * Implementación de JwtValidator que combina parsing local con validación remota.
 *
 * Tras la inversión de dependencias (DA-12), el validador opera contra el puerto
 * [TokenVerifier] en lugar de `AuthRepository`. La rama de validación local rápida
 * (`JwtParser.parse`) se preserva.
 */
class JwtValidatorImpl(
    private val tokenVerifier: TokenVerifier,
    private val telemetry: Telemetry = Telemetry.Noop,
) : JwtValidator {
    override suspend fun validate(token: String): JwtValidationResult {
        // 1. Validación local rápida primero (evita llamada de red innecesaria)
        val parseResult = JwtParser.parse(token)
        if (parseResult is JwtParseResult.EmptyToken) {
            return JwtValidationResult.Invalid(JwtValidationResult.InvalidReason.Malformed)
        }
        if (parseResult is JwtParseResult.InvalidFormat) {
            return JwtValidationResult.Invalid(JwtValidationResult.InvalidReason.Malformed)
        }

        // 2. Si ya expiró localmente, no gastar llamada de red
        val claims = (parseResult as JwtParseResult.Success).claims
        if (claims.isExpired) {
            return JwtValidationResult.Invalid(JwtValidationResult.InvalidReason.Expired)
        }

        // 3. Validación completa contra backend (vía puerto)
        return try {
            val result = tokenVerifier.verify(token)

            when {
                result is Result.Failure -> {
                    JwtValidationResult.NetworkError(result.error)
                }

                result is Result.Success && result.data.valid -> {
                    mapToValid(result.data)
                }

                result is Result.Success && !result.data.valid -> {
                    mapToInvalid(result.data)
                }

                else -> {
                    JwtValidationResult.Invalid(
                        JwtValidationResult.InvalidReason.Other("Unexpected response"),
                    )
                }
            }
        } catch (e: Exception) {
            telemetry.crash.recordException(
                e,
                mapOf(
                    "operation" to "validate",
                    "module" to "auth",
                    "context" to "verify_token_remote",
                    "edugo.feature" to "auth",
                ),
            )
            JwtValidationResult.NetworkError(e.message ?: "Unknown error")
        }
    }

    override fun quickValidate(token: String): JwtParseResult = JwtParser.parse(token)

    // --- Private helpers ---

    private fun mapToValid(response: TokenVerificationResult): JwtValidationResult.Valid {
        val subject = response.subject
        if (subject.isNullOrBlank()) {
            throw IllegalStateException("Missing subject in valid token response")
        }
        return JwtValidationResult.Valid(
            subject = subject,
            expiresAt = response.expiresAt ?: Instant.DISTANT_FUTURE,
            claims = response.claims,
        )
    }

    private fun mapToInvalid(response: TokenVerificationResult): JwtValidationResult.Invalid {
        val reason =
            when (response.errorCode) {
                "expired" -> JwtValidationResult.InvalidReason.Expired
                "revoked" -> JwtValidationResult.InvalidReason.Revoked
                "user_inactive" -> JwtValidationResult.InvalidReason.UserInactive
                "malformed" -> JwtValidationResult.InvalidReason.Malformed
                else -> JwtValidationResult.InvalidReason.Other(response.errorCode ?: "Unknown error")
            }
        return JwtValidationResult.Invalid(reason)
    }
}
