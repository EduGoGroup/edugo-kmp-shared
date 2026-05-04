package com.edugo.kmp.telemetry.tracing

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.withContext

/**
 * Elemento del CoroutineContext que transporta el [Span] actual a traves de
 * fronteras `suspend`.
 *
 * En Kotlin/Native (NMM) no hay `ThreadLocal` portable, asi que el "current span"
 * se propaga via CoroutineContext. JvmCommonMain hace lo mismo por consistencia
 * cross-platform — el span context de la JVM nativo NO se reusa, se envuelve.
 *
 * Uso tipico (interceptor Ktor):
 * ```
 * val span = telemetry.tracer.startSpan(...)
 * withSpan(span) {
 *     // dentro de este bloque, currentSpan() == span
 *     callApi()
 * }
 * ```
 */
public class SpanContextElement(public val span: Span) :
    AbstractCoroutineContextElement(Key) {

    public companion object Key : CoroutineContext.Key<SpanContextElement>
}

/** Devuelve el [Span] activo del coroutine context, o `null` si no hay. */
public suspend fun currentSpan(): Span? =
    coroutineContext[SpanContextElement.Key]?.span

/**
 * Ejecuta [block] con [span] como "current". Atajo idiomatico para el patron
 * de propagacion via CoroutineContext.
 */
public suspend inline fun <T> withSpan(span: Span, crossinline block: suspend () -> T): T =
    withContext(SpanContextElement(span)) { block() }
