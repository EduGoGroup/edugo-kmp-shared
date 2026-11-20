package com.edugo.kmp.config

/**
 * Resuelve la URL base de identity-core (login, refresh y logout, ADR 0038).
 *
 * Orden de lectura:
 *   1. El valor de la plataforma, si existe y no está en blanco. Hoy solo lo da
 *      la web: `<meta name="identity-core-base-url">` en `index.html`, que apunta
 *      al proxy del mismo origen de `nginx` (`/identity-core`), porque
 *      identity-core no responde CORS (HU-DEMO.3).
 *   2. `appConfig.api.identityCoreBaseUrl`, del JSON del ambiente. Es lo que usan
 *      Android, escritorio e iOS, que llaman directo a identity-core (R-015).
 *
 * Calcado de [OtelEndpointResolver]: la orquestación vive en `commonMain` y cada
 * plataforma solo aporta su lector.
 */
object IdentityCoreUrlResolver {
    /**
     * @param appConfig Config del ambiente actual.
     * @param platformValue Valor que aporta la plataforma. Por defecto, el de
     *                      [readPlatformIdentityCoreUrl]; los tests lo inyectan.
     * @return La URL base de identity-core, sin espacios alrededor. Puede ser una
     *         ruta relativa (`/identity-core`) en la web, o vacía si el JSON del
     *         ambiente no la trae.
     */
    fun resolve(
        appConfig: AppConfig,
        platformValue: String? = readPlatformIdentityCoreUrl(),
    ): String {
        platformValue?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return appConfig.api.identityCoreBaseUrl.trim()
    }
}

/**
 * Lector de la plataforma. Devuelve `null` cuando la plataforma no aporta valor:
 * en Android, escritorio e iOS siempre; en la web, si falta la `<meta>`.
 */
internal expect fun readPlatformIdentityCoreUrl(): String?
