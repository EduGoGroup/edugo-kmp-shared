package com.edugo.kmp.config

/**
 * Seam de tests: cada plataforma provee un `actual class EnvVarSource` que
 * sabe cómo mutar/limpiar las fuentes nativas que consume su
 * `detectPlatformEnvironment()` correspondiente.
 *
 *  - JVM (Desktop/Android): `System.setProperty` / `clearProperty` para la
 *    ruta primaria (`app.environment`). La ruta de env var fallback no es
 *    mutable desde código (`System.getenv` es read-only) y queda fuera de
 *    cobertura — ver `STANDARD.md §10`.
 *  - iOS: backings mutables que `IosEnvSeam` consulta en lugar de
 *    `NSProcessInfo` / `NSBundle` reales.
 *  - WasmJS: `window[key]` y `<meta name=…>` mutados por interop JS.
 *
 * El método [installSnapshot] toma una fotografía de las fuentes nativas
 * relevantes antes de un test, y [restoreSnapshot] las repone tras el test
 * (incluso si `set`/`clear` no se llamaron). Es defensivo: garantiza que un
 * test no contamine al siguiente, incluso con fallos a mitad de ejecución.
 *
 * Cada implementación `actual` sabe internamente qué `TargetPlatform` lookup
 * debe usar contra [AppEnvVar.primaryKeys] / [AppEnvVar.fallbackKeys]; el
 * contract test que la consume declara su propio `expectedPlatformLabel`.
 */
internal expect class EnvVarSource() {
    /** Setea la fuente primaria de la variable a `value`. */
    fun set(variable: AppEnvVar, value: String)

    /** Setea la fuente fallback (Plist en iOS, meta tag en Web). No-op si no aplica. */
    fun setFallback(variable: AppEnvVar, value: String)

    /** Borra TODAS las fuentes nativas (primaria + fallback) de `variable`. */
    fun clear(variable: AppEnvVar)

    /** Captura el estado actual de las fuentes para restaurarlo en `restoreSnapshot`. */
    fun installSnapshot()

    /** Restaura las fuentes al estado de `installSnapshot` y limpia los fakes. */
    fun restoreSnapshot()

    /** Indica si esta plataforma soporta una fuente fallback distinta de la primaria. */
    fun supportsFallback(): Boolean
}
