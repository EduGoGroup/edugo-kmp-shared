@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.edugo.kmp.config

import kotlin.js.Promise
import kotlinx.coroutines.await

/**
 * STANDARD.md §6 / §3.4 — pre-carga el `config/{env}.json` real empaquetado en
 * el bundle web antes de levantar Compose. El callsite (`Main.main`) debe
 * invocar [prefetch] dentro de un coroutine antes de inicializar la UI o
 * Koin: `MainScope().launch { ConfigPrefetcher.prefetch(env); startApp() }`.
 *
 * Si la red falla (offline, archivo no servido por webpack), el cache queda
 * vacío y [loadResourceAsString] cae a [DefaultConfigs] como red de seguridad.
 * No se inventan defaults silenciosos del enum `Environment` — el ambiente ya
 * fue forzado por el bridge `BUILD_ENVIRONMENT → forceEnvironment` antes de
 * llegar acá.
 */
public object ConfigPrefetcher {
    internal val cache: MutableMap<String, String> = mutableMapOf()

    public suspend fun prefetch(environment: Environment) {
        val path = "config/${environment.fileName}.json"
        val text = runCatching {
            val promise: Promise<JsString> = fetchTextOrEmpty(path)
            promise.await<JsString>().toString()
        }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
        if (text != null) {
            cache[path] = text
        }
    }
}

@JsFun(
    """(path) => fetch(path).then((r) => r.ok ? r.text() : '').catch(() => '')"""
)
private external fun fetchTextOrEmpty(path: String): Promise<JsString>
