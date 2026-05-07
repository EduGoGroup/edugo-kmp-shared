package com.edugo.kmp.config

/**
 * Implementación iOS de [EnvVarSource].
 *
 * `NSProcessInfo.processInfo.environment` y `NSBundle.mainBundle.infoDictionary`
 * son **read-only** en runtime — no hay API pública en iOS para mutarlos
 * desde el proceso. Por eso el detector iOS productivo (`EnvironmentDetector.ios.kt`)
 * delega su lectura a [IosEnvSeam], que en producción apunta a las APIs
 * reales y en tests apunta a los backings mutables que poblamos aquí.
 */
internal actual class EnvVarSource actual constructor() {

    private val envBacking: MutableMap<String, String> = mutableMapOf()
    private val plistBacking: MutableMap<String, String> = mutableMapOf()
    private var snapshotInstalled: Boolean = false

    actual fun set(variable: AppEnvVar, value: String) {
        val nativeKey = variable.primaryKeys[TargetPlatform.IOS]
            ?: error("AppEnvVar.${variable.name} no declara primary key para IOS")
        when (nativeKey) {
            is NativeKey.ProcessEnv -> envBacking[nativeKey.key] = value
            else -> error("Plataforma iOS espera ProcessEnv para primary, pero recibió $nativeKey")
        }
    }

    actual fun setFallback(variable: AppEnvVar, value: String) {
        val nativeKey = variable.fallbackKeys[TargetPlatform.IOS]
            ?: error("AppEnvVar.${variable.name} no declara fallback key para IOS")
        when (nativeKey) {
            is NativeKey.PlistKey -> plistBacking[nativeKey.key] = value
            else -> error("Plataforma iOS espera PlistKey para fallback, pero recibió $nativeKey")
        }
    }

    actual fun clear(variable: AppEnvVar) {
        (variable.primaryKeys[TargetPlatform.IOS] as? NativeKey.ProcessEnv)?.let {
            envBacking.remove(it.key)
        }
        (variable.fallbackKeys[TargetPlatform.IOS] as? NativeKey.PlistKey)?.let {
            plistBacking.remove(it.key)
        }
    }

    actual fun installSnapshot() {
        envBacking.clear()
        plistBacking.clear()
        // Activamos el seam recién aquí para que la mera construcción del
        // EnvVarSource no contamine el estado global de IosEnvSeam.
        IosEnvSeam.envProvider = { key -> envBacking[key] }
        IosEnvSeam.plistProvider = { key -> plistBacking[key] }
        snapshotInstalled = true
    }

    actual fun restoreSnapshot() {
        envBacking.clear()
        plistBacking.clear()
        IosEnvSeam.resetToProduction()
        snapshotInstalled = false
    }

    actual fun supportsFallback(): Boolean = true
}
