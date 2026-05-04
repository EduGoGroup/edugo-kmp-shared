package com.edugo.kmp.network

import com.edugo.kmp.foundation.error.AppError
import com.edugo.kmp.foundation.error.ErrorCode
import com.edugo.kmp.foundation.result.Result
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Convierte HttpResponse a Result<T> con deserialización automática.
 * Mapea errores HTTP a AppError apropiados.
 *
 * @return Result.Success con datos deserializados o Result.Failure con error
 */
public suspend inline fun <reified T> HttpResponse.toResult(): Result<T> {
    return when {
        status.isSuccess() -> {
            try {
                Result.Success(body<T>())
            } catch (e: Exception) {
                Result.Failure(
                    error = "Failed to deserialize response: ${e.message}",
                    isRetryable = false,
                )
            }
        }
        else -> {
            val errorCode = ErrorCode.fromHttpStatus(status.value)
            val errorBody = tryReadErrorBody()
            val userMessage = extractUserMessage(errorBody) ?: status.description
            Result.Failure(
                error = userMessage,
                isRetryable = errorCode.retryable,
                errorCode = errorCode,
            )
        }
    }
}

/**
 * Convierte HttpResponse a Result<T> con AppError estructurado.
 */
public suspend inline fun <reified T> HttpResponse.toResultWithAppError(): Result<T> {
    return when {
        status.isSuccess() -> {
            try {
                Result.Success(body<T>())
            } catch (e: Exception) {
                val appError = AppError.fromException(
                    exception = e,
                    code = ErrorCode.SYSTEM_SERIALIZATION_ERROR
                )
                Result.Failure(appError.toString(), errorCode = appError.code)
            }
        }
        else -> {
            val errorCode = ErrorCode.fromHttpStatus(status.value)
            val errorBody = tryReadErrorBody()
            val appError = AppError.fromCode(
                code = errorCode,
                customMessage = errorBody ?: status.description
            )
            Result.Failure(appError.toString(), errorCode = appError.code)
        }
    }
}

/**
 * Intenta leer el body de error de forma segura.
 */
public suspend fun HttpResponse.tryReadErrorBody(): String? {
    return try {
        bodyAsText().take(500) // Limitar tamaño
    } catch (e: Exception) {
        null
    }
}

/**
 * Extracts a human-readable message from a JSON error body.
 * Tries common API error fields: "message", "error", "detail".
 * Returns null if the body is not JSON or none of the fields are found.
 */
public fun extractUserMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        val obj = Json.parseToJsonElement(body).jsonObject
        (obj["message"] ?: obj["error"] ?: obj["detail"])
            ?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

/**
 * Verifica si el status code indica éxito (2xx).
 */
public fun HttpStatusCode.isSuccess(): Boolean = value in 200..299

/**
 * Verifica si el status code indica error del cliente (4xx).
 */
public fun HttpStatusCode.isClientError(): Boolean = value in 400..499

/**
 * Verifica si el status code indica error del servidor (5xx).
 */
public fun HttpStatusCode.isServerError(): Boolean = value in 500..599
