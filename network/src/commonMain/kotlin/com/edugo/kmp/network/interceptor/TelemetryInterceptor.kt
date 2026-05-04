package com.edugo.kmp.network.interceptor

import com.edugo.kmp.telemetry.Telemetry
import com.edugo.kmp.telemetry.helpers.recordHttpRequest
import com.edugo.kmp.telemetry.helpers.recordNetworkError
import com.edugo.kmp.telemetry.helpers.sanitizePath
import com.edugo.kmp.telemetry.tracing.Span
import com.edugo.kmp.telemetry.tracing.SpanKind
import com.edugo.kmp.telemetry.tracing.SpanStatusCode
import com.edugo.kmp.telemetry.tracing.W3CTraceContextPropagator
import com.edugo.kmp.telemetry.tracing.currentSpan
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.AttributeKey
import kotlin.time.Clock

/**
 * Interceptor que registra metricas + emite span CLIENT por cada request HTTP.
 *
 * Mide la duracion de cada request y registra:
 * - Metodo HTTP, path, status code y duracion en [interceptResponse]
 * - Errores de red en [onError]
 *
 * Ademas (D11):
 * - Arranca un span CLIENT en [interceptRequest], hijo del span actual del
 *   coroutine context (ver [currentSpan]) si lo hay.
 * - Inyecta `traceparent` (W3C) en los headers — las APIs Go con `otelgin`
 *   crean un span SERVER hijo, dejando una traza completa en Tempo.
 * - Cierra el span en [interceptResponse]/[onError] con `http.status_code` y
 *   `setStatus(ERROR)` cuando aplica.
 *
 * Si [telemetry.tracer] es Noop (Web wasmJs, o boot temprano antes del
 * factory), [Span.context] es invalido → el propagador omite la inyeccion y
 * no se contamina el header con un trace-id "00..00".
 *
 * Usa request attributes (Ktor 3.x) para almacenar el span y el timestamp,
 * evitando un mapa interno con mutex.
 *
 * @param telemetry Instancia de Telemetry (puede ser Noop).
 */
class TelemetryInterceptor(
    private val telemetry: Telemetry
) : Interceptor {

    override val order: Int = 99

    private val startTimeKey = AttributeKey<Long>("telemetry_start_time")
    private val spanKey = AttributeKey<Span>("telemetry_span")

    override suspend fun interceptRequest(request: HttpRequestBuilder) {
        request.attributes.put(startTimeKey, Clock.System.now().toEpochMilliseconds())

        val rawPath = extractPath(request.url.buildString())
        val sanitizedPath = sanitizePath(rawPath)
        val method = request.method.value

        val span = telemetry.tracer.startSpan(
            name = "$method $sanitizedPath",
            kind = SpanKind.CLIENT,
            attributes = mapOf(
                "http.method" to method,
                "http.url" to request.url.buildString(),
                "http.route" to sanitizedPath,
                "http.scheme" to request.url.protocol.name,
                "net.peer.name" to request.url.host,
            ),
            parent = currentSpan()?.context,
        )
        request.attributes.put(spanKey, span)

        val headers = mutableMapOf<String, String>()
        W3CTraceContextPropagator.inject(span.context, headers)
        for ((k, v) in headers) request.headers.append(k, v)
    }

    override suspend fun interceptResponse(response: HttpResponse) {
        val attrs = response.call.attributes
        val startTime = attrs.getOrNull(startTimeKey)
        if (startTime != null) {
            val durationMs = Clock.System.now().toEpochMilliseconds() - startTime
            val method = response.request.method.value
            val path = extractPath(response.request.url.toString())
            val statusCode = response.status.value
            telemetry.recordHttpRequest(method, path, statusCode, durationMs)
        }

        val span = attrs.getOrNull(spanKey)
        if (span != null) {
            val statusCode = response.status.value
            span.setAttribute("http.status_code", statusCode.toLong())
            if (statusCode >= 400) span.setStatus(SpanStatusCode.ERROR)
            span.end()
        }
    }

    override suspend fun onError(request: HttpRequestBuilder, exception: Throwable) {
        val path = extractPath(request.url.buildString())
        val errorType = exception::class.simpleName ?: "Unknown"
        val sanitizedPath = sanitizePath(path)
        telemetry.crash.recordException(
            exception,
            mapOf(
                "operation" to request.method.value,
                "module" to "network",
                "context" to "http_request",
                "edugo.feature" to "http_client",
                "http.path" to sanitizedPath,
                "error.kind" to errorType,
            ),
        )
        telemetry.recordNetworkError(path, errorType)

        val span = request.attributes.getOrNull(spanKey)
        if (span != null) {
            span.recordException(exception, mapOf("http.path" to sanitizedPath))
            span.setStatus(SpanStatusCode.ERROR, errorType)
            span.end()
        }
    }

    /**
     * Extracts the path component from a full URL string.
     * e.g. "https://api.example.com/v1/users?page=1" -> "/v1/users"
     */
    private fun extractPath(url: String): String {
        val afterScheme = url.substringAfter("://", "")
        val pathAndQuery = afterScheme.substringAfter("/", "")
        val path = pathAndQuery.substringBefore("?")
        return "/$path"
    }
}
