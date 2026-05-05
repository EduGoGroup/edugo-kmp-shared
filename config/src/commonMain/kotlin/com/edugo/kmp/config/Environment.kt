package com.edugo.kmp.config

/**
 * Representa los ambientes disponibles para la aplicación.
 *
 * Cada ambiente tiene su propia configuración en un archivo JSON separado:
 * - DEV: config/dev.json
 * - STAGING: config/staging.json
 * - PROD: config/prod.json
 */
enum class Environment {
    /**
     * Ambiente de desarrollo local.
     * Típicamente, usa localhost y tiene debug habilitado.
     */
    DEV,

    /**
     * Ambiente de desarrollo apuntando a APIs locales accesibles vía LAN.
     * Apunta a la IP LAN de la Mac dev (configurada en `dev-lan.json`) para
     * que un dispositivo físico (iPad / Android device) en la misma WiFi
     * pueda consumir el backend sin necesidad de cloud o tunneling.
     */
    DEV_LAN,

    /**
     * Ambiente de staging/pruebas.
     * Usa servidores de staging para validación antes de producción.
     */
    STAGING,

    /**
     * Ambiente de producción.
     * Configuración optimizada para usuarios finales.
     */
    PROD;

    /**
     * Convierte el enum a kebab-case string para nombrar archivos.
     * Ejemplo: Environment.DEV -> "dev", Environment.DEV_LAN -> "dev-lan"
     */
    val fileName: String
        get() = name.lowercase().replace('_', '-')

    companion object {
        /**
         * Obtiene el ambiente desde un string.
         * Retorna null si el string no corresponde a ningún ambiente válido.
         *
         * @param value String del ambiente (case-insensitive)
         * @return Environment correspondiente o null si no se encuentra
         */
        fun fromString(value: String?): Environment? {
            if (value == null) return null
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

        /**
         * Obtiene el ambiente desde un string con un fallback.
         *
         * @param value String del ambiente (case-insensitive)
         * @param default Ambiente a retornar si value no es válido (por defecto DEV)
         * @return Environment correspondiente o el default
         */
        fun fromStringOrDefault(value: String?, default: Environment = DEV): Environment {
            return fromString(value) ?: default
        }
    }
}
