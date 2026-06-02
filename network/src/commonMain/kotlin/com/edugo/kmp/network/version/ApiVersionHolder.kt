package com.edugo.kmp.network.version

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holder observable singleton de las versiones de API capturadas en runtime.
 *
 * Indexa [ApiVersionInfo] por nombre lógico de API (p. ej. "identity",
 * "academic"…). El mapa se actualiza de forma incremental: cada respuesta HTTP
 * con headers de versión refresca solo la entrada de su API, conservando el
 * resto.
 *
 * Tipo neutral: no conoce APIs concretas; los nombres lógicos los aporta quien
 * registra el interceptor de captura. La UI observa [versions] para mostrar las
 * versiones (o "—" si una API aún no respondió).
 */
object ApiVersionHolder {
    private val _versions = MutableStateFlow<Map<String, ApiVersionInfo>>(emptyMap())

    /** Mapa observable nombre-de-API → versión capturada. */
    val versions: StateFlow<Map<String, ApiVersionInfo>> = _versions.asStateFlow()

    /**
     * Registra/actualiza la versión de una API. Solo reemplaza la entrada de
     * [apiName]; las demás permanecen.
     */
    fun record(apiName: String, info: ApiVersionInfo) {
        _versions.update { current -> current + (apiName to info) }
    }

    /** Limpia todas las versiones capturadas (útil en logout/reset). */
    fun clear() {
        _versions.value = emptyMap()
    }
}
