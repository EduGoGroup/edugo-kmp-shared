package com.edugo.kmp.network.version

/**
 * Versión y build de una API backend, capturados desde los headers de respuesta.
 *
 * Contrato con el backend: cada respuesta HTTP trae los headers
 * `X-Edugo-Api-Version` (semver) y `X-Edugo-Api-Build` (sha corto). El
 * interceptor [com.edugo.kmp.network.interceptor.VersionCaptureInterceptor] lee
 * ambos y los publica en [ApiVersionHolder] indexados por nombre lógico de API.
 *
 * Tipo neutral: no conoce las APIs concretas de ninguna app; el nombre lógico lo
 * decide quien registra el interceptor (mapeo host/puerto → nombre).
 *
 * @property version Semver reportado por la API (header `X-Edugo-Api-Version`).
 * @property build SHA corto del build de la API (header `X-Edugo-Api-Build`).
 */
data class ApiVersionInfo(
    val version: String,
    val build: String,
)
