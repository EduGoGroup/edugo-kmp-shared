package com.edugo.kmp.telemetry.tracing

/**
 * Estado final del span — alineado con OTel StatusCode (proto values 0..2).
 *
 * Convencion OTel:
 * - [UNSET]: default, deja que el backend infiera (mayoria de spans HTTP 2xx/3xx).
 * - [OK]: marcar exito explicito (raro — solo cuando `unset` confunde el grafico).
 * - [ERROR]: cualquier 4xx/5xx, network error, exception en el path del span.
 */
public enum class SpanStatusCode(public val protoValue: Int) {
    UNSET(0),
    OK(1),
    ERROR(2),
}
