package com.edugo.kmp.config

import kotlinx.serialization.Serializable

/**
 * Configuración de endpoints de APIs.
 *
 * Agrupa URLs base de todos los microservicios.
 */
interface ApiConfig {
    /** URL base del API Identity (auth, usuarios, roles, permisos, auditoría). */
    val identityBaseUrl: String

    /** URL base del API Academic (escuelas, unidades, membresías). */
    val academicBaseUrl: String

    /** URL base del API Learning (materiales, evaluaciones, progreso). */
    val learningBaseUrl: String

    /** URL base del API Platform (pantallas, menús, sync, configuración). */
    val platformBaseUrl: String
}

/**
 * Implementación serializable de [ApiConfig].
 */
@Serializable
data class ApiConfigImpl(
    override val identityBaseUrl: String,
    override val academicBaseUrl: String,
    override val learningBaseUrl: String,
    override val platformBaseUrl: String
) : ApiConfig
