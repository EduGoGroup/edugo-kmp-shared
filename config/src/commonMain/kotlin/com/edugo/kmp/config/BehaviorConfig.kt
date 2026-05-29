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

    /**
     * Habilita el selector de usuarios de prueba en la pantalla de login.
     *
     * Herramienta SOLO para pruebas alpha/beta: cuando es true, sobre el campo
     * de email aparece un combo que lista usuarios de prueba traídos del backend
     * y autocompleta el email al seleccionar uno (el password lo escribe el tester).
     * Debe estar en false en PROD; el combo no existe ahí.
     */
    val testUserSelectorEnabled: Boolean
}

/**
 * Implementación serializable de [BehaviorConfig].
 */
@Serializable
data class BehaviorConfigImpl(
    override val mockMode: Boolean = false,
    override val testUserSelectorEnabled: Boolean = false
) : BehaviorConfig
