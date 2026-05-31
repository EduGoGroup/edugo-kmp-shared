package com.edugo.kmp.config

/**
 * Override por-API para el "modo mixto" del frontend: permite apuntar 1+ APIs a
 * `localhost` dejando el resto en el entorno base (típicamente STAGING/GCP).
 *
 * La fuente de verdad del mapeo nombre→puerto y de la regla de reescritura vive
 * aquí. La task Gradle `generateAppConfigs` (config/build.gradle.kts) aplica esta
 * misma regla en build-time para hornear las URLs en [GeneratedConfigs] — por eso
 * no hay I/O ni branching en runtime. Este objeto existe además para poder
 * verificar la regla con tests unitarios puros, sin disparar Gradle.
 *
 * Contrato:
 * - Sin APIs listadas → el [ApiConfig] se devuelve intacto.
 * - Para cada API listada → su baseURL se reescribe a `http://localhost:<puerto>`.
 * - Las APIs no listadas conservan la URL del entorno base.
 */
object LocalApiOverride {
    /**
     * Mapeo canónico nombre-de-API → puerto local de la API Go correspondiente.
     *
     * Debe permanecer alineado con:
     * - Los puertos de las APIs Go (identity 8070, academic 8060, learning 8065,
     *   platform 8075).
     * - El `LOCAL_APIS`/`android-reverse` del Makefile de `edugo-ui-kmp`.
     * - La tabla de puertos replicada en la task `generateAppConfigs`.
     */
    val ports: Map<String, Int> = mapOf(
        "identity" to 8070,
        "academic" to 8060,
        "learning" to 8065,
        "platform" to 8075,
    )

    /**
     * Parsea el CSV recibido vía `-PlocalApis` a un conjunto de nombres de API
     * válidos (lowercase, sin espacios, sin vacíos). Nombres desconocidos
     * provocan error: fail-loud, igual que el resto del contrato de config.
     *
     * @param csv Lista separada por comas, p.ej. "academic,learning". Null o
     *            vacío → conjunto vacío (sin override).
     * @return Conjunto de nombres de API normalizados.
     * @throws IllegalArgumentException si algún nombre no está en [ports].
     */
    fun parse(csv: String?): Set<String> {
        if (csv.isNullOrBlank()) return emptySet()
        val names = csv.split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        val unknown = names.filterNot { ports.containsKey(it) }
        require(unknown.isEmpty()) {
            "localApis contiene APIs desconocidas: ${unknown.joinToString(", ")}. " +
                "Valores aceptados: ${ports.keys.sorted().joinToString(", ")}."
        }
        return names.toSet()
    }

    /**
     * Reescribe a `http://localhost:<puerto>` solo las baseURLs de las APIs
     * listadas, conservando el resto del [base] tal cual.
     *
     * @param base ApiConfig del entorno activo (p.ej. STAGING).
     * @param localApis Nombres de API a redirigir a localhost (ver [parse]).
     * @return Un nuevo [ApiConfig] con las URLs reescritas; si [localApis] está
     *         vacío, devuelve [base] sin cambios.
     */
    fun applyTo(base: ApiConfig, localApis: Set<String>): ApiConfig {
        if (localApis.isEmpty()) return base
        return ApiConfigImpl(
            identityBaseUrl = if ("identity" in localApis) localUrl("identity") else base.identityBaseUrl,
            academicBaseUrl = if ("academic" in localApis) localUrl("academic") else base.academicBaseUrl,
            learningBaseUrl = if ("learning" in localApis) localUrl("learning") else base.learningBaseUrl,
            platformBaseUrl = if ("platform" in localApis) localUrl("platform") else base.platformBaseUrl,
        )
    }

    /** URL local de una API conocida. */
    fun localUrl(api: String): String {
        val port = ports[api]
            ?: throw IllegalArgumentException(
                "API desconocida '$api'. Valores aceptados: ${ports.keys.sorted().joinToString(", ")}.",
            )
        return "http://localhost:$port"
    }
}
