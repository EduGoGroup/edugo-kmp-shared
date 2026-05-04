package com.edugo.kmp.telemetry.tracing

/**
 * Identidad de un span — formato W3C `traceparent` (versión 00).
 *
 * @property traceIdHex 32 chars hex, lowercase. NUNCA todos ceros (= invalid).
 * @property spanIdHex 16 chars hex, lowercase. NUNCA todos ceros (= invalid).
 * @property flags byte de flags. `0x01` = sampled.
 * @property traceState string opcional `key1=val1,key2=val2` (max 256 bytes per W3C).
 */
public data class SpanContext(
    val traceIdHex: String,
    val spanIdHex: String,
    val flags: Byte = SAMPLED,
    val traceState: String = "",
) {
    public val isValid: Boolean
        get() = traceIdHex.length == TRACE_ID_HEX_LEN &&
            spanIdHex.length == SPAN_ID_HEX_LEN &&
            traceIdHex != INVALID_TRACE_ID &&
            spanIdHex != INVALID_SPAN_ID &&
            traceIdHex.all { it in HEX_CHARS } &&
            spanIdHex.all { it in HEX_CHARS }

    public val isSampled: Boolean
        get() = (flags.toInt() and SAMPLED.toInt()) != 0

    public companion object {
        public const val TRACE_ID_HEX_LEN: Int = 32
        public const val SPAN_ID_HEX_LEN: Int = 16
        public const val SAMPLED: Byte = 0x01

        internal const val INVALID_TRACE_ID: String = "00000000000000000000000000000000"
        internal const val INVALID_SPAN_ID: String = "0000000000000000"
        private const val HEX_CHARS = "0123456789abcdef"

        public val Invalid: SpanContext = SpanContext(
            traceIdHex = INVALID_TRACE_ID,
            spanIdHex = INVALID_SPAN_ID,
            flags = 0,
        )
    }
}
