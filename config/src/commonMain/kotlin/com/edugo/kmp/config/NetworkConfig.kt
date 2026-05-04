package com.edugo.kmp.config

import kotlinx.serialization.Serializable

/**
 * Configuración de red y comunicaciones HTTP.
 *
 * Agrupa propiedades relacionadas con timeouts, puertos y debugging de red.
 */
interface NetworkConfig {
    /** Timeout en milisegundos para peticiones HTTP. */
    val timeout: Long

    /** Puerto para la aplicación web (Wasm). */
    val webPort: Int

    /**
     * Indica si el modo debug está activo.
     * Afecta logging de red, reintentos agresivos, etc.
     */
    val debugMode: Boolean
}

/**
 * Implementación serializable de [NetworkConfig].
 */
@Serializable
data class NetworkConfigImpl(
    override val timeout: Long,
    override val webPort: Int,
    override val debugMode: Boolean
) : NetworkConfig
