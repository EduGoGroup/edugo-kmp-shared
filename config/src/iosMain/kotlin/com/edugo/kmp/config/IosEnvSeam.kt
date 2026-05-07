package com.edugo.kmp.config

import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

/**
 * Indirección interna entre [detectPlatformEnvironment] (iOS) y las APIs
 * inmutables de Foundation (`NSProcessInfo`, `NSBundle.mainBundle`).
 *
 * En producción los providers apuntan directamente a esas APIs. En tests
 * (`iosTest`), la implementación de `EnvVarSource` swappea los providers
 * por backings mutables para simular distintos valores de `APP_ENVIRONMENT`
 * y `Info.plist` sin tocar el bundle real (que es read-only en runtime).
 *
 * Mantenerlos como `@Volatile` permite que sean mutados desde el thread de
 * test antes de que el detector los lea — el detector se invoca desde el
 * mismo thread en los tests, pero la marca de volatilidad documenta la
 * intención y protege contra reordering.
 */
internal object IosEnvSeam {
    private val PRODUCTION_ENV: (String) -> String? =
        { key -> NSProcessInfo.processInfo.environment[key] as? String }

    private val PRODUCTION_PLIST: (String) -> String? =
        { key -> NSBundle.mainBundle.infoDictionary?.get(key) as? String }

    @kotlin.concurrent.Volatile
    internal var envProvider: (String) -> String? = PRODUCTION_ENV

    @kotlin.concurrent.Volatile
    internal var plistProvider: (String) -> String? = PRODUCTION_PLIST

    internal fun resetToProduction() {
        envProvider = PRODUCTION_ENV
        plistProvider = PRODUCTION_PLIST
    }
}
