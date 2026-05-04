package com.edugo.kmp.telemetry.tracing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpanContextTest {

    @Test
    fun validContextPasses() {
        val ctx = SpanContext(
            traceIdHex = "0af7651916cd43dd8448eb211c80319c",
            spanIdHex = "b7ad6b7169203331",
        )
        assertTrue(ctx.isValid)
        assertTrue(ctx.isSampled)
    }

    @Test
    fun invalidConstantIsInvalid() {
        assertFalse(SpanContext.Invalid.isValid)
        assertFalse(SpanContext.Invalid.isSampled)
    }

    @Test
    fun shortIdsAreInvalid() {
        val ctx = SpanContext(traceIdHex = "abc", spanIdHex = "def")
        assertFalse(ctx.isValid)
    }

    @Test
    fun nonHexCharsAreInvalid() {
        val ctx = SpanContext(
            traceIdHex = "0af7651916cd43dd8448eb211c80319G",
            spanIdHex = "b7ad6b7169203331",
        )
        assertFalse(ctx.isValid)
    }

    @Test
    fun unsampledFlag() {
        val ctx = SpanContext(
            traceIdHex = "0af7651916cd43dd8448eb211c80319c",
            spanIdHex = "b7ad6b7169203331",
            flags = 0x00,
        )
        assertTrue(ctx.isValid)
        assertFalse(ctx.isSampled)
    }

    @Test
    fun lengthsAreCanonical() {
        assertEquals(32, SpanContext.TRACE_ID_HEX_LEN)
        assertEquals(16, SpanContext.SPAN_ID_HEX_LEN)
    }
}
