package com.edugo.kmp.network

import io.ktor.client.plugins.*

/**
 * Mapea excepciones de Ktor y sistema a NetworkException tipadas.
 */
public object ExceptionMapper {

    /**
     * Convierte cualquier excepción a NetworkException apropiada.
     */
    public fun map(exception: Throwable): NetworkException {
        return when (exception) {
            // Ya es NetworkException
            is NetworkException -> exception

            // Timeout de Ktor
            is HttpRequestTimeoutException ->
                NetworkException.Timeout("Request timeout: ${exception.message}", exception)

            // SSL/TLS y otros errores - detectar por tipo o mensaje
            else -> mapByMessage(exception)
        }
    }

    /**
     * Mapea excepciones basándose en el mensaje cuando no hay tipo específico.
     */
    private fun mapByMessage(exception: Throwable): NetworkException {
        val message = exception.message?.lowercase() ?: ""
        val className = exception::class.simpleName?.lowercase() ?: ""

        return when {
            // SSL/TLS errors
            message.contains("ssl") || message.contains("certificate") ||
            className.contains("ssl") ->
                NetworkException.SslError(exception.message ?: "SSL error", exception)

            // DNS errors
            message.contains("dns") || message.contains("unknown host") ||
            message.contains("unable to resolve") || message.contains("unresolved") ||
            className.contains("unresolvedaddress") ->
                NetworkException.DnsFailure(exception.message ?: "DNS failure", exception)

            // Timeout errors
            message.contains("timeout") || message.contains("timed out") ||
            className.contains("timeout") ->
                NetworkException.Timeout(exception.message ?: "Timeout", exception)

            // Connection reset/closed
            // Incluye conexiones keep-alive muertas reusadas tras reinicio del backend:
            // OkHttp lanza IOException("unexpected end of stream on http://...") y Darwin/iOS
            // reporta "connection was lost". Deben clasificarse como connectivity (retryable + stale fallback).
            (message.contains("connection") &&
                (message.contains("refused") || message.contains("reset") || message.contains("closed"))) ||
            message.contains("unexpected end of stream") || message.contains("end of stream") ||
            message.contains("broken pipe") || message.contains("stream was reset") ||
            message.contains("connection was lost") ->
                NetworkException.ConnectionReset(exception.message ?: "Connection reset", exception)

            // No connection / network unreachable
            // wasmJs/JS browsers throw TypeError("Failed to fetch") when the network is offline,
            // CORS preflight fails, or the server is unreachable. Same semantics as a connect
            // failure on JVM/Android/iOS — classify as NoConnection, not as a 500 ServerError.
            message.contains("no route") || message.contains("network is unreachable") ||
            message.contains("no address") || message.contains("connect") && message.contains("fail") ||
            message.contains("failed to fetch") ||
            className.contains("connectexception") || className.contains("noroute") ||
            className.contains("typeerror") ->
                NetworkException.NoConnection(exception.message ?: "No connection", exception)

            // Socket errors generalmente indican problemas de conexión
            className.contains("socket") && (message.contains("closed") || message.contains("reset")) ->
                NetworkException.ConnectionReset(exception.message ?: "Socket error", exception)

            // Fallback a ServerError para errores desconocidos
            else -> NetworkException.ServerError(
                statusCode = 500,
                message = exception.message ?: "Unknown network error",
                cause = exception
            )
        }
    }

    /**
     * Ejecuta bloque y mapea cualquier excepción a NetworkException.
     */
    public inline fun <T> runCatching(block: () -> T): kotlin.Result<T> {
        return try {
            kotlin.Result.success(block())
        } catch (e: Throwable) {
            kotlin.Result.failure(map(e))
        }
    }
}
