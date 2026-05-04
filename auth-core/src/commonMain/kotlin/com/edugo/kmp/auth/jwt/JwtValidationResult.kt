package com.edugo.kmp.auth.jwt

import kotlin.time.Instant

/**
 * Resultado de la validación de un token JWT.
 * Representa validación REAL (verificada por backend), no solo parsing.
 */
sealed class JwtValidationResult {
    /**
     * Token válido - verificado por el backend.
     */
    data class Valid(
        val subject: String,
        val expiresAt: Instant,
        val claims: Map<String, String> = emptyMap(),
    ) : JwtValidationResult()

    /**
     * Token inválido con razón específica.
     */
    data class Invalid(
        val reason: InvalidReason,
    ) : JwtValidationResult()

    /**
     * Error de red - no se pudo validar (usar validación local como fallback).
     */
    data class NetworkError(
        val message: String,
    ) : JwtValidationResult()

    /**
     * Razones por las que un token puede ser inválido.
     */
    sealed class InvalidReason {
        /** Token ha expirado */
        data object Expired : InvalidReason()

        /** Token fue revocado (logout, cambio de password, etc.) */
        data object Revoked : InvalidReason()

        /** Token tiene formato inválido o firma incorrecta */
        data object Malformed : InvalidReason()

        /** Usuario asociado está inactivo */
        data object UserInactive : InvalidReason()

        /** Otra razón */
        data class Other(
            val message: String,
        ) : InvalidReason()
    }
}

// Extension functions
val JwtValidationResult.isValid: Boolean
    get() = this is JwtValidationResult.Valid

val JwtValidationResult.isInvalid: Boolean
    get() = this is JwtValidationResult.Invalid

val JwtValidationResult.isNetworkError: Boolean
    get() = this is JwtValidationResult.NetworkError

fun JwtValidationResult.getValidOrNull(): JwtValidationResult.Valid? = this as? JwtValidationResult.Valid
