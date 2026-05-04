package com.edugo.kmp.auth.jwt

import kotlin.time.Instant

/**
 * Subset estable de los datos que devuelve un endpoint de verificación JWT.
 * Es el contrato neutro entre `auth-core` y cualquier impl concreta del
 * verificador (que puede ser HTTP, gRPC, intra-proceso, etc.).
 */
data class TokenVerificationResult(
    val valid: Boolean,
    val subject: String?,
    val expiresAt: Instant?,
    val claims: Map<String, String>,
    val errorCode: String?,
)
