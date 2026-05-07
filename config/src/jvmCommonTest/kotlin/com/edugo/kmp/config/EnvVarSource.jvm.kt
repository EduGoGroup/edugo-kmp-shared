package com.edugo.kmp.config

/**
 * Implementación JVM de [EnvVarSource] compartida por Desktop y Android.
 *
 * Cubre la ruta primaria (`System.setProperty`/`clearProperty` sobre las keys
 * declaradas como [NativeKey.SystemProperty] en [AppEnvVar]). La ruta fallback
 * vía env var (`System.getenv`) **no es mutable** desde código JVM en runtime
 * sin reflexión a `ProcessEnvironment`, que es frágil entre JDKs y bypassea
 * el contrato de la plataforma — por eso se documenta en `STANDARD.md §10`
 * y se valida por integración manual en lugar de aquí.
 *
 * El detector productivo Desktop y el detector productivo Android leen la
 * misma system property en idéntico orden para `APP_ENVIRONMENT`, así que un
 * mismo `actual` cubre ambos targets — la única diferencia entre ellos es el
 * label en el mensaje de error ("Desktop" vs "Android"), que el contract test
 * verifica usando su propio `expectedPlatformLabel`.
 *
 * Para variables que difieren entre Desktop y Android (ej.
 * `OTEL_EXPORTER_OTLP_ENDPOINT`: Desktop primario `EnvVar`, Android primario
 * `SystemProperty`), [systemPropertyKeyOrNull] busca la mejor SystemProperty
 * mutable disponible (primary primero, luego fallback) para esta variable. Si
 * no hay ninguna SystemProperty en ningún slot, el seam hace skip silencioso
 * (no es testeable en JVM por las razones de runtime expuestas arriba).
 */
internal actual class EnvVarSource actual constructor() {

    private var snapshot: Map<String, String?>? = null

    actual fun set(variable: AppEnvVar, value: String) {
        val key = systemPropertyKeyOrNull(variable) ?: return
        System.setProperty(key, value)
    }

    actual fun setFallback(variable: AppEnvVar, value: String) {
        // JVM env vars son read-only en runtime: documentado, no soportado.
        // Llamar a este método en JVM es un error del test (no debería ocurrir
        // porque [supportsFallback] retorna `false`).
        error("EnvVarSource (JVM) no soporta setFallback: env vars son read-only en runtime")
    }

    actual fun clear(variable: AppEnvVar) {
        val key = systemPropertyKeyOrNull(variable) ?: return
        System.clearProperty(key)
    }

    actual fun installSnapshot() {
        snapshot = AppEnvVar.entries
            .mapNotNull { systemPropertyKeyOrNull(it) }
            .associateWith { System.getProperty(it) }
    }

    actual fun restoreSnapshot() {
        snapshot?.forEach { (key, value) ->
            if (value == null) System.clearProperty(key) else System.setProperty(key, value)
        }
        snapshot = null
    }

    actual fun supportsFallback(): Boolean = false

    /**
     * Devuelve la SystemProperty mutable disponible (primary o fallback) para
     * `variable`, prefiriendo primary; null si ningún slot del par
     * Desktop/Android es una SystemProperty.
     *
     * Para variables donde Desktop y Android difieren en sus claves nativas,
     * el seam mutará SOLO la que corresponde al target. Como Desktop/Android
     * comparten `actual`, asumimos que el detector productivo del target
     * equivocado (ej. Desktop leyendo "otel.exporter.otlp.endpoint" cuando
     * su primaria real es la env var) o no entra en este flujo, o lee
     * coincidentemente lo seteado por el seam.
     */
    private fun systemPropertyKeyOrNull(variable: AppEnvVar): String? {
        TargetPlatform.entries.forEach { platform ->
            val primary = variable.primaryKeys[platform]
            if (primary is NativeKey.SystemProperty) return primary.key
        }
        TargetPlatform.entries.forEach { platform ->
            val fallback = variable.fallbackKeys[platform]
            if (fallback is NativeKey.SystemProperty) return fallback.key
        }
        return null
    }
}
