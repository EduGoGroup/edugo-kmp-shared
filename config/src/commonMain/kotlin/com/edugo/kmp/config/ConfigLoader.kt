package com.edugo.kmp.config

import kotlinx.serialization.json.Json

/**
 * Cargador de configuración por ambiente.
 *
 * Sirve las constantes generadas en build-time desde [GeneratedConfigs] (que se
 * compila a partir de los JSON en `src/commonMain/resources/config/` vía la
 * task Gradle `generateAppConfigs`). No hay I/O en runtime ni fallback silencioso:
 * agregar un `Environment` nuevo sin actualizar `GeneratedConfigs` provoca un
 * `when` no exhaustivo y el compilador falla.
 *
 * Uso:
 * ```kotlin
 * val config = ConfigLoader.load(Environment.DEV)
 * println(config.api.academicBaseUrl) // "http://localhost:8060"
 * ```
 */
object ConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Carga la configuración para el ambiente especificado.
     *
     * @param environment Ambiente a cargar (DEV, DEV_LAN, STAGING, PROD)
     * @return AppConfig generada en build-time desde el JSON correspondiente
     */
    fun load(environment: Environment): AppConfig = when (environment) {
        Environment.DEV -> GeneratedConfigs.DEV
        Environment.DEV_LAN -> GeneratedConfigs.DEV_LAN
        Environment.STAGING -> GeneratedConfigs.STAGING
        Environment.PROD -> GeneratedConfigs.PROD
    }

    /**
     * Carga la configuración desde un string (útil para testing).
     *
     * @param jsonString JSON string con la configuración
     * @return AppConfig con la configuración parseada
     */
    fun loadFromString(jsonString: String): AppConfig {
        return json.decodeFromString<AppConfigImpl>(jsonString)
    }
}
