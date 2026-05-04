package com.edugo.kmp.auth.jwt

/**
 * Validador de tokens JWT.
 *
 * Combina validación local (rápida, offline) con validación remota (completa).
 *
 * ## Estrategia de Validación
 *
 * ### Para operaciones críticas (pagos, cambios de cuenta):
 * Usar `validate()` - consulta backend, verifica revocación.
 *
 * ### Para operaciones normales (mostrar UI, cache):
 * Usar `quickValidate()` - solo verifica estructura y expiración local.
 *
 * ## Ejemplo
 * ```kotlin
 * when (val result = validator.validate(token)) {
 *     is JwtValidationResult.Valid -> {
 *         println("Subject: ${result.subject}")
 *     }
 *     is JwtValidationResult.Invalid -> {
 *         when (result.reason) {
 *             is InvalidReason.Expired -> forceLogout()
 *             is InvalidReason.Revoked -> forceLogout()
 *             else -> showError()
 *         }
 *     }
 *     is JwtValidationResult.NetworkError -> {
 *         val local = validator.quickValidate(token)
 *     }
 * }
 * ```
 */
interface JwtValidator {
    /**
     * Válido token contra el backend.
     * Requiere conexión de red. Verifica firma, expiración y revocación.
     *
     * @param token Token JWT a validar
     * @return Resultado de validación (Valid, Invalid, NetworkError)
     */
    suspend fun validate(token: String): JwtValidationResult

    /**
     * Validación rápida local.
     * NO verifica firma ni revocación. Solo estructura y expiración.
     * Útil para decisiones rápidas o modo offline.
     *
     * @param token Token JWT a validar
     * @return Resultado de parsing (Success con claims, o error)
     */
    fun quickValidate(token: String): JwtParseResult
}
