package com.edugo.kmp.auth.token

import com.edugo.kmp.foundation.result.Result

/**
 * Puerto para refrescar el access token contra el backend.
 *
 * Implementaciones típicas:
 * - `RepositoryRefreshTokenSource` en `:modules:auth-edugo` (delega en
 *   `AuthRepository.refresh(...)` y mapea `RefreshResponse` → `TokenPair`).
 *
 * Contrato:
 * - `refresh(refreshToken)` ejecuta UNA sola petición al back; el manager
 *   se encarga del single-flight y del retry.
 * - `Result.Success(TokenPair)` cuando el back acepta y devuelve un nuevo
 *   par de tokens.
 * - `Result.Failure(error)` cuando el back rechaza (token revocado/expirado)
 *   o hay error de red. El error se mapea posteriormente a
 *   `RefreshFailureReason` por el manager.
 */
interface RefreshTokenSource {
    suspend fun refresh(refreshToken: String): Result<TokenPair>
}
