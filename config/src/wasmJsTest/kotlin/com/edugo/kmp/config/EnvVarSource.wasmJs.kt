package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(key, value) => {
        try { window[key] = value; }
        catch (e) { console.error('[EnvVarSource] window[' + key + '] write failed:', e); }
    }"""
)
private external fun setWindowGlobal(key: String, value: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(key) => {
        try { delete window[key]; }
        catch (e) { console.error('[EnvVarSource] delete window[' + key + '] failed:', e); }
    }"""
)
private external fun clearWindowGlobal(key: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(key) => {
        try { var v = window[key]; return (v == null) ? '' : String(v); }
        catch (e) { return ''; }
    }"""
)
private external fun readWindowGlobalRaw(key: String): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(name, content) => {
        try {
            var existing = document.querySelector('meta[name="' + name + '"]');
            if (existing) { existing.remove(); }
            var m = document.createElement('meta');
            m.setAttribute('name', name);
            m.setAttribute('content', content);
            document.head.appendChild(m);
        } catch (e) { console.error('[EnvVarSource] setMetaTag failed:', e); }
    }"""
)
private external fun setMetaTag(name: String, content: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(name) => {
        try {
            var existing = document.querySelector('meta[name="' + name + '"]');
            if (existing) { existing.remove(); }
        } catch (e) { console.error('[EnvVarSource] clearMetaTag failed:', e); }
    }"""
)
private external fun clearMetaTag(name: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """(name) => {
        try {
            var meta = document.querySelector('meta[name="' + name + '"]');
            return meta ? (meta.getAttribute('content') || '') : '';
        } catch (e) { return ''; }
    }"""
)
private external fun readMetaTagRaw(name: String): String

/**
 * Implementación WasmJS de [EnvVarSource]. Usa interop JS para mutar el
 * `window` global y los `<meta>` tags del DOM (ambos read/write en runtime
 * Karma + Chrome headless).
 *
 * Karma boota con un `index.html` mínimo, así que las pruebas pueden
 * insertar/eliminar el `<meta name="app-environment">` libremente sin
 * arrastrar contaminación del `index.html` productivo (que es el que se
 * sirve para `:run`, no para `:test`).
 */
internal actual class EnvVarSource actual constructor() {

    private data class Snapshot(
        val windowValues: Map<String, String?>,
        val metaValues: Map<String, String?>
    )
    private var snapshot: Snapshot? = null

    actual fun set(variable: AppEnvVar, value: String) {
        val nativeKey = variable.primaryKeys[TargetPlatform.WEB]
            ?: error("AppEnvVar.${variable.name} no declara primary key para WEB")
        when (nativeKey) {
            is NativeKey.WindowGlobal -> setWindowGlobal(nativeKey.key, value)
            else -> error("Plataforma WEB espera WindowGlobal para primary, pero recibió $nativeKey")
        }
    }

    actual fun setFallback(variable: AppEnvVar, value: String) {
        val nativeKey = variable.fallbackKeys[TargetPlatform.WEB]
            ?: error("AppEnvVar.${variable.name} no declara fallback key para WEB")
        when (nativeKey) {
            is NativeKey.MetaTag -> setMetaTag(nativeKey.name, value)
            else -> error("Plataforma WEB espera MetaTag para fallback, pero recibió $nativeKey")
        }
    }

    actual fun clear(variable: AppEnvVar) {
        (variable.primaryKeys[TargetPlatform.WEB] as? NativeKey.WindowGlobal)?.let {
            clearWindowGlobal(it.key)
        }
        (variable.fallbackKeys[TargetPlatform.WEB] as? NativeKey.MetaTag)?.let {
            clearMetaTag(it.name)
        }
    }

    actual fun installSnapshot() {
        val windows = AppEnvVar.entries.mapNotNull {
            (it.primaryKeys[TargetPlatform.WEB] as? NativeKey.WindowGlobal)?.let { wg ->
                wg.key to readWindowGlobalRaw(wg.key).takeIf { v -> v.isNotEmpty() }
            }
        }.toMap()
        val metas = AppEnvVar.entries.mapNotNull {
            (it.fallbackKeys[TargetPlatform.WEB] as? NativeKey.MetaTag)?.let { mt ->
                mt.name to readMetaTagRaw(mt.name).takeIf { v -> v.isNotEmpty() }
            }
        }.toMap()
        snapshot = Snapshot(windows, metas)
        // Limpieza inicial para garantizar partida limpia
        windows.keys.forEach(::clearWindowGlobal)
        metas.keys.forEach(::clearMetaTag)
    }

    actual fun restoreSnapshot() {
        val s = snapshot ?: return
        s.windowValues.forEach { (k, v) ->
            if (v == null) clearWindowGlobal(k) else setWindowGlobal(k, v)
        }
        s.metaValues.forEach { (k, v) ->
            if (v == null) clearMetaTag(k) else setMetaTag(k, v)
        }
        snapshot = null
    }

    actual fun supportsFallback(): Boolean = true
}
