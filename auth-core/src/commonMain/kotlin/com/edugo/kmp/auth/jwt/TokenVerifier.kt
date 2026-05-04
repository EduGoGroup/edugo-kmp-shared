package com.edugo.kmp.auth.jwt

import com.edugo.kmp.foundation.result.Result

/**
 * Puerto para verificar JWT contra un endpoint remoto.
 *
 * Implementaciones típicas:
 * - `RepositoryTokenVerifier` en `:modules:auth-edugo` (delega en
 *   `AuthRepository.verifyToken(...)`).
 *
 * Contrato:
 * - `verify(token)` retorna `Result.Success(TokenVerificationResult)` con la
 *   bandera `valid` y los claims que el back haya devuelto.
 * - `Result.Failure(error)` cuando hay error de red o el endpoint no responde.
 *   La validación local rápida (parsing JWT, expiración) se hace en
 *   `JwtValidatorImpl` antes de llamar a este puerto.
 */
interface TokenVerifier {
    suspend fun verify(token: String): Result<TokenVerificationResult>
}
