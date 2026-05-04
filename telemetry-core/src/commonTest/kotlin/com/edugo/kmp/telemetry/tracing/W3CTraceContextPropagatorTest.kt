package com.edugo.kmp.telemetry.tracing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class W3CTraceContextPropagatorTest {

    private val validTraceId = "0af7651916cd43dd8448eb211c80319c"
    private val validSpanId = "b7ad6b7169203331"

    @Test
    fun injectWritesCanonicalTraceparent() {
        val ctx = SpanContext(validTraceId, validSpanId, flags = 0x01)
        val headers = mutableMapOf<String, String>()

        W3CTraceContextPropagator.inject(ctx, headers)

        assertEquals(
            "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01",
            headers["traceparent"],
        )
        assertNull(headers["tracestate"])
    }

    @Test
    fun injectIncludesTracestateWhenPresent() {
        val ctx = SpanContext(validTraceId, validSpanId, flags = 0x01, traceState = "vendor=abc")
        val headers = mutableMapOf<String, String>()

        W3CTraceContextPropagator.inject(ctx, headers)

        assertEquals("vendor=abc", headers["tracestate"])
    }

    @Test
    fun injectSkipsInvalidContext() {
        val headers = mutableMapOf<String, String>()
        W3CTraceContextPropagator.inject(SpanContext.Invalid, headers)
        assertTrue(headers.isEmpty())
    }

    @Test
    fun injectSkipsAllZerosTraceId() {
        val ctx = SpanContext(
            traceIdHex = "00000000000000000000000000000000",
            spanIdHex = validSpanId,
        )
        val headers = mutableMapOf<String, String>()
        W3CTraceContextPropagator.inject(ctx, headers)
        assertTrue(headers.isEmpty())
    }

    @Test
    fun injectFlagsZeroProducesDoubleZero() {
        val ctx = SpanContext(validTraceId, validSpanId, flags = 0x00)
        val headers = mutableMapOf<String, String>()
        W3CTraceContextPropagator.inject(ctx, headers)
        assertEquals(
            "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-00",
            headers["traceparent"],
        )
    }

    @Test
    fun extractRoundTrip() {
        val original = SpanContext(validTraceId, validSpanId, flags = 0x01, traceState = "k=v")
        val headers = mutableMapOf<String, String>()
        W3CTraceContextPropagator.inject(original, headers)

        val extracted = W3CTraceContextPropagator.extract(headers)

        assertNotNull(extracted)
        assertEquals(original.traceIdHex, extracted.traceIdHex)
        assertEquals(original.spanIdHex, extracted.spanIdHex)
        assertEquals(original.flags, extracted.flags)
        assertEquals(original.traceState, extracted.traceState)
    }

    @Test
    fun extractMissingHeaderReturnsNull() {
        assertNull(W3CTraceContextPropagator.extract(emptyMap()))
    }

    @Test
    fun extractWrongVersionReturnsNull() {
        val headers = mapOf(
            "traceparent" to "01-$validTraceId-$validSpanId-01",
        )
        assertNull(W3CTraceContextPropagator.extract(headers))
    }

    @Test
    fun extractMalformedReturnsNull() {
        val cases = listOf(
            "00-tooshort-$validSpanId-01",
            "00-$validTraceId-tooshort-01",
            "00-$validTraceId-$validSpanId-zz",
            "garbage",
            "00-$validTraceId-$validSpanId",
        )
        for (raw in cases) {
            assertNull(
                W3CTraceContextPropagator.extract(mapOf("traceparent" to raw)),
                "expected null for $raw",
            )
        }
    }

    @Test
    fun extractAllZerosReturnsNull() {
        val headers = mapOf(
            "traceparent" to "00-00000000000000000000000000000000-0000000000000000-01",
        )
        assertNull(W3CTraceContextPropagator.extract(headers))
    }

    @Test
    fun extractIsCaseInsensitive() {
        val headers = mapOf(
            "traceparent" to "00-${validTraceId.uppercase()}-${validSpanId.uppercase()}-01",
        )
        val ctx = W3CTraceContextPropagator.extract(headers)
        assertNotNull(ctx)
        assertEquals(validTraceId, ctx.traceIdHex)
        assertEquals(validSpanId, ctx.spanIdHex)
    }
}
