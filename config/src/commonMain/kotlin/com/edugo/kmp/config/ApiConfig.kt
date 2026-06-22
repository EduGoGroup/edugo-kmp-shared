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

    /**
     * URL base del API Messaging (mensajería efímera cifrada, plan 025).
     *
     * Getter por defecto vacío para no obligar a los implementadores anónimos de
     * ApiConfig (fakes de test) a overridearlo. La fuente real por ambiente sale de
     * los JSON de resources/config horneados en ApiConfigImpl.
     */
    val messagingBaseUrl: String
        get() = ""
}

/**
 * Implementación serializable de [ApiConfig].
 */
@Serializable
data class ApiConfigImpl(
    override val identityBaseUrl: String,
    override val academicBaseUrl: String,
    override val learningBaseUrl: String,
    override val platformBaseUrl: String,
    // Default vacío por compatibilidad con los call sites posicionales existentes
    // (tests de config). El generador generateAppConfigs lo emite explícito desde
    // los JSON, y el modo mixto lo propaga en LocalApiOverride.
    override val messagingBaseUrl: String = ""
) : ApiConfig
