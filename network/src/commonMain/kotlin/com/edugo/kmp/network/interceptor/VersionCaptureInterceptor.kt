package com.edugo.kmp.network.interceptor

import com.edugo.kmp.network.version.ApiVersionHolder
import com.edugo.kmp.network.version.ApiVersionInfo
import io.ktor.client.statement.*

/**
 * Interceptor que captura la versión de cada API backend desde los headers de
 * respuesta y la publica en [ApiVersionHolder].
 *
 * Contrato con el backend: cada respuesta HTTP trae
 * - `X-Edugo-Api-Version` (semver de la API), y
 * - `X-Edugo-Api-Build` (sha corto del build de la API).
 *
 * El nombre lógico de la API (p. ej. "identity"/"academic") se resuelve a partir
 * del host:puerto de la respuesta vía [resolveApiName], que aporta el consumidor
 * al registrar el interceptor. Así este tipo se mantiene neutral: no conoce las
 * APIs concretas de ninguna app.
 *
 * Solo observa (no muta el request), por eso usa un [order] alto, como
 * [TelemetryInterceptor]. Si faltan los headers o no se resuelve la API, no hace
 * nada — la captura es best-effort y nunca rompe el request.
 *
 * @param resolveApiName Mapea (host, puerto) → nombre lógico de API, o `null` si
 *                       el host/puerto no corresponde a una API conocida.
 */
class VersionCaptureInterceptor(
    private val resolveApiName: (host: String, port: Int) -> String?,
    private val holder: ApiVersionHolder = ApiVersionHolder,
) : Interceptor {

    override val order: Int = 98

    override suspend fun interceptResponse(response: HttpResponse) {
        val version = response.headers[VERSION_HEADER] ?: return
        val build = response.headers[BUILD_HEADER].orEmpty()

        val url = response.request.url
        val apiName = resolveApiName(url.host, url.port) ?: return

        holder.record(apiName, ApiVersionInfo(version = version, build = build))
    }

    companion object {
        const val VERSION_HEADER: String = "X-Edugo-Api-Version"
        const val BUILD_HEADER: String = "X-Edugo-Api-Build"
    }
}
