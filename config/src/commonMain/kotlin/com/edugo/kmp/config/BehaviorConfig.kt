package com.edugo.kmp.config

import kotlinx.serialization.Serializable

/**
 * Configuración de comportamiento de la aplicación.
 *
 * Agrupa flags que controlan features, modos especiales, etc.
 */
interface BehaviorConfig {
    /**
     * Indica si el modo mock está activo.
     *
     * Cuando es true, los repositorios de red se reemplazan por mocks
     * que devuelven datos coherentes sin necesidad de backend.
     * Siempre es false en PROD por seguridad.
     */
    val mockMode: Boolean
}

/**
 * Implementación serializable de [BehaviorConfig].
 */
@Serializable
data class BehaviorConfigImpl(
    override val mockMode: Boolean = false
) : BehaviorConfig
