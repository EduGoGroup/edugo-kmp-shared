package com.edugo.kmp.config

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TelemetryConfigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun default_otelEndpoint_is_empty_when_constructed_without_args() {
        val config = TelemetryConfigImpl()
        assertEquals("", config.otelEndpoint)
    }

    @Test
    fun validates_http_url_accepts_http_scheme() {
        val config = TelemetryConfigImpl(otelEndpoint = "http://localhost:4318")
        assertEquals("http://localhost:4318", config.otelEndpoint)
    }

    @Test
    fun validates_http_url_accepts_https_scheme() {
        val config = TelemetryConfigImpl(otelEndpoint = "https://otel.example.com:4318")
        assertEquals("https://otel.example.com:4318", config.otelEndpoint)
    }

    @Test
    fun rejects_invalid_scheme_throws_IllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            TelemetryConfigImpl(otelEndpoint = "ftp://foo")
        }
        assertFailsWith<IllegalArgumentException> {
            TelemetryConfigImpl(otelEndpoint = "localhost:4318")
        }
    }

    @Test
    fun accepts_empty_string_without_throwing() {
        val config = TelemetryConfigImpl(otelEndpoint = "")
        assertEquals("", config.otelEndpoint)
    }

    @Test
    fun serializes_to_json_round_trip() {
        val original = TelemetryConfigImpl(otelEndpoint = "http://localhost:4318")
        val encoded = json.encodeToString(TelemetryConfigImpl.serializer(), original)
        val decoded = json.decodeFromString(TelemetryConfigImpl.serializer(), encoded)
        assertEquals(original, decoded)
    }
}
