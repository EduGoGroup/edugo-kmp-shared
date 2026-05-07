package com.edugo.kmp.config

/**
 * Implementación JVM de [EnvVarSource] compartida por Desktop y Android.
 *
 * Cubre la ruta primaria (`System.setProperty`/`clearProperty` sobre
 * `app.environment`). La ruta fallback (`APP_ENVIRONMENT` env var) **no es
 * mutable** desde código JVM en runtime sin reflexión a `ProcessEnvironment`,
 * que es frágil entre JDKs y bypassea el contrato de la plataforma — por
 * eso se documenta en `STANDARD.md §10` y se valida por integración manual
 * en lugar de aquí.
 *
 * El detector productivo Desktop y el detector productivo Android leen la
 * misma system property (`app.environment`) en idéntico orden, así que un
 * mismo `actual` cubre ambos targets — la única diferencia entre ellos es
 * el label en el mensaje de error ("Desktop" vs "Android"), que el contract
 * test verifica usando su propio `expectedPlatformLabel`.
 */
internal actual class EnvVarSource actual constructor() {

    private var snapshot: Map<String, String?>? = null

    actual fun set(variable: AppEnvVar, value: String) {
        System.setProperty(primarySystemPropertyKey(variable), value)
    }

    actual fun setFallback(variable: AppEnvVar, value: String) {
        // JVM env vars son read-only en runtime: documentado, no soportado.
        // Llamar a este método en JVM es un error del test (no debería ocurrir
        // porque [supportsFallback] retorna `false`).
        error("EnvVarSource (JVM) no soporta setFallback: env vars son read-only en runtime")
    }

    actual fun clear(variable: AppEnvVar) {
        System.clearProperty(primarySystemPropertyKey(variable))
    }

    actual fun installSnapshot() {
        // Defensivo: el actual JVM cubre Desktop+Android asumiendo que ambas
        // plataformas declaran la misma SystemProperty primaria. Si en el
        // futuro divergen, el lookup `primarySystemPropertyKey(... DESKTOP)`
        // elegiría Desktop silenciosamente y los tests Android pasarían sin
        // ejercitar la key real de Android — fallar ruidosamente.
        AppEnvVar.entries.forEach { variable ->
            val desktopKey = variable.primaryKeys[TargetPlatform.DESKTOP]
            val androidKey = variable.primaryKeys[TargetPlatform.ANDROID]
            check(desktopKey == androidKey) {
                "AppEnvVar.${variable.name} divergencia entre primaryKeys: " +
                    "DESKTOP=$desktopKey vs ANDROID=$androidKey. " +
                    "EnvVarSource.jvm.kt asume que comparten clave; " +
                    "introducir un actual dedicado por target si divergen."
            }
        }
        snapshot = AppEnvVar.entries
            .map { primarySystemPropertyKey(it) }
            .associateWith { System.getProperty(it) }
    }

    actual fun restoreSnapshot() {
        snapshot?.forEach { (key, value) ->
            if (value == null) System.clearProperty(key) else System.setProperty(key, value)
        }
        snapshot = null
    }

    actual fun supportsFallback(): Boolean = false

    private fun primarySystemPropertyKey(variable: AppEnvVar): String {
        // Desktop y Android comparten la misma SystemProperty key.
        val nativeKey = variable.primaryKeys[TargetPlatform.DESKTOP]
            ?: error("AppEnvVar.${variable.name} no declara primary key para DESKTOP/ANDROID")
        return when (nativeKey) {
            is NativeKey.SystemProperty -> nativeKey.key
            else -> error(
                "Plataforma JVM espera SystemProperty para ${variable.name}, pero recibió $nativeKey"
            )
        }
    }
}
