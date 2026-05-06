package com.edugo.kmp.config

/**
 * WasmJS implementation: lee el cache que pobla [ConfigPrefetcher.prefetch]
 * (ejecutado en el bootstrap del callsite, antes de Compose / Koin) con el
 * `config/{env}.json` real servido por webpack. Si la pre-carga falló (offline
 * o el bundle no incluyó el archivo), cae a [DefaultConfigs] como red de
 * seguridad, manteniendo el comportamiento histórico.
 *
 * STANDARD.md §6 — el JSON empaquetado es la fuente de verdad; los defaults
 * sólo aplican cuando la red no pudo servir el recurso.
 */
internal actual fun loadResourceAsString(path: String): String? {
    val cached = ConfigPrefetcher.cache[path]
    if (cached != null) return cached
    return DefaultConfigs.get(path)
}
