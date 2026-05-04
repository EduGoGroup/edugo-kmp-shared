package com.edugo.kmp.auth.token

import com.edugo.kmp.foundation.result.Result

/**
 * Puerto que abstrae el almacenamiento local del par access/refresh token.
 *
 * Implementaciones típicas:
 * - `StorageTokenProvider` en `:modules:auth-edugo` (lee/escribe `AuthToken` JSON
 *   en `SafeEduGoStorage`, key `auth_token`).
 * - Cualquier consumidor externo de `auth-core` puede proveer su propia impl
 *   (ej. una app iOS nativa con Keychain).
 *
 * Contrato:
 * - `currentAccessToken()` retorna el access token vigente o `Result.Failure`
 *   con mensaje "no_token" si no hay sesión.
 * - `currentRefreshToken()` análogo para el refresh token.
 * - `clearTokens()` borra ambos tokens del almacenamiento subyacente y nunca
 *   lanza excepción.
 */
interface TokenProvider {
    suspend fun currentAccessToken(): Result<String>

    suspend fun currentRefreshToken(): Result<String>

    suspend fun clearTokens()
}
