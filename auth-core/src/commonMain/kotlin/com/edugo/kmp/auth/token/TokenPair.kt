package com.edugo.kmp.auth.token

import kotlin.time.Instant

/**
 * Par de tokens (access + refresh) neutro: NO depende de `AuthToken` (modelo wire EduGo).
 *
 * El campo `refreshToken` es nullable porque algunos backends (Keycloak rotativo)
 * no lo rotan en cada refresh — el cliente reutiliza el anterior si llega null.
 */
data class TokenPair(
    val accessToken: String,
    val expiresAt: Instant,
    val refreshToken: String?,
)
