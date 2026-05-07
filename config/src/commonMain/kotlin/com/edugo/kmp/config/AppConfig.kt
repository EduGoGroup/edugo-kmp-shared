package com.edugo.kmp.config

import kotlinx.serialization.Serializable

/**
 * Configuración global de la aplicación.
 *
 * Contiene todas las propiedades necesarias para configurar
 * el comportamiento de la app en diferentes ambientes.
 *
 * Se carga desde archivos JSON en resources/config/ según el ambiente.
 * Agrupa propiedades en submodules por cohesión: network, behavior, api.
 */
interface AppConfig {
    /** Ambiente actual de ejecución. */
    val environment: Environment

    /** Configuración de red y comunicaciones HTTP. */
    val network: NetworkConfig

    /** Configuración de comportamiento de la aplicación. */
    val behavior: BehaviorConfig

    /** Configuración de endpoints de APIs. */
    val api: ApiConfig

    /** Configuración de telemetría (OpenTelemetry). */
    val telemetry: TelemetryConfig
}

/**
 * Implementación serializable de [AppConfig].
 *
 * Usada internamente por [ConfigLoader] para
 * deserializar los archivos JSON de configuración.
 */
@Serializable
data class AppConfigImpl(
    private val environmentName: String,
    override val network: NetworkConfigImpl,
    override val behavior: BehaviorConfigImpl,
    override val api: ApiConfigImpl,
    override val telemetry: TelemetryConfigImpl = TelemetryConfigImpl()
) : AppConfig {
    override val environment: Environment =
        Environment.fromString(environmentName)
            ?: error(
                "AppConfigImpl.environmentName=\"$environmentName\" no es un Environment válido. " +
                    "Valores aceptados: ${Environment.entries.joinToString(", ") { it.name }}."
            )

    init {
        if (environment == Environment.PROD && behavior.mockMode) {
            throw IllegalStateException("mockMode cannot be enabled in PROD environment")
        }
    }
}
