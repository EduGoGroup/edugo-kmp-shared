package com.edugo.kmp.telemetry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TelemetryTest {

    // ==================== Noop Never Fails ====================

    @Test
    fun noopNeverFails() {
        val telemetry = Telemetry.Noop

        // MetricsRecorder
        telemetry.metrics.counter("test", 1.0, mapOf("k" to "v"))
        telemetry.metrics.histogram("test", 42.0, mapOf("k" to "v"))
        telemetry.metrics.gauge("test", 99.0, mapOf("k" to "v"))

        // AnalyticsRecorder
        telemetry.analytics.trackEvent("event", mapOf("key" to "value"))
        telemetry.analytics.trackScreen("screen", mapOf("key" to "value"))
        telemetry.analytics.setUserProperty("prop", "val")
        telemetry.analytics.setUserProperty("prop", null)
        telemetry.analytics.setUserId("user123")
        telemetry.analytics.setUserId(null)

        // CrashRecorder
        telemetry.crash.recordException(RuntimeException("test"), mapOf("ctx" to "val"))
        telemetry.crash.log("log message")
        telemetry.crash.setUserId("user123")
        telemetry.crash.setUserId(null)
    }

    // ==================== Companion Noop Is Noop ====================

    @Test
    fun companionNoopIsNoop() {
        val telemetry = Telemetry.Noop

        assertNotNull(telemetry)
        assertIs<NoopMetricsRecorder>(telemetry.metrics)
        assertIs<NoopAnalyticsRecorder>(telemetry.analytics)
        assertIs<NoopCrashRecorder>(telemetry.crash)
    }

    // ==================== Custom Recorder Is Used ====================

    @Test
    fun customRecorderIsUsed() {
        val spyMetrics = SpyMetricsRecorder()
        val spyAnalytics = SpyAnalyticsRecorder()
        val spyCrash = SpyCrashRecorder()
        val telemetry = Telemetry(spyMetrics, spyAnalytics, spyCrash)

        telemetry.metrics.counter("my_counter", 5.0, mapOf("env" to "test"))
        telemetry.analytics.trackEvent("my_event", mapOf("prop" to 42))
        telemetry.crash.log("something happened")

        assertEquals(1, spyMetrics.calls.size)
        assertEquals("my_counter", spyMetrics.calls[0].name)
        assertEquals(5.0, spyMetrics.calls[0].value)
        assertEquals("test", spyMetrics.calls[0].labels["env"])

        assertEquals(1, spyAnalytics.events.size)
        assertEquals("my_event", spyAnalytics.events[0].name)
        assertEquals(42, spyAnalytics.events[0].properties["prop"])

        assertEquals(1, spyCrash.logs.size)
        assertEquals("something happened", spyCrash.logs[0])
    }

    @Test
    fun customRecorderHistogramAndGauge() {
        val spyMetrics = SpyMetricsRecorder()
        val telemetry = Telemetry(metrics = spyMetrics)

        telemetry.metrics.histogram("latency", 123.0, mapOf("route" to "/api"))
        telemetry.metrics.gauge("connections", 7.0)

        assertEquals(1, spyMetrics.histograms().size)
        assertEquals("latency", spyMetrics.histograms()[0].name)
        assertEquals(123.0, spyMetrics.histograms()[0].value)

        assertEquals(1, spyMetrics.gauges().size)
        assertEquals("connections", spyMetrics.gauges()[0].name)
        assertEquals(7.0, spyMetrics.gauges()[0].value)
    }

    @Test
    fun customRecorderScreenAndUserProperty() {
        val spyAnalytics = SpyAnalyticsRecorder()
        val telemetry = Telemetry(analytics = spyAnalytics)

        telemetry.analytics.trackScreen("home", mapOf("tab" to "main"))
        telemetry.analytics.setUserProperty("plan", "premium")
        telemetry.analytics.setUserId("u-42")

        assertEquals(1, spyAnalytics.screens.size)
        assertEquals("home", spyAnalytics.screens[0].name)

        assertEquals(1, spyAnalytics.userProperties.size)
        assertEquals("plan", spyAnalytics.userProperties[0].name)
        assertEquals("premium", spyAnalytics.userProperties[0].value)

        assertEquals("u-42", spyAnalytics.lastUserId)
    }

    @Test
    fun customRecorderExceptionWithContext() {
        val spyCrash = SpyCrashRecorder()
        val telemetry = Telemetry(crash = spyCrash)
        val error = IllegalStateException("bad state")

        telemetry.crash.recordException(error, mapOf("screen" to "login"))
        telemetry.crash.setUserId("u-99")

        assertEquals(1, spyCrash.exceptions.size)
        assertEquals(error, spyCrash.exceptions[0].throwable)
        assertEquals("login", spyCrash.exceptions[0].context["screen"])
        assertEquals("u-99", spyCrash.lastUserId)
    }

    @Test
    fun defaultConstructorUsesNoops() {
        val telemetry = Telemetry()

        assertIs<NoopMetricsRecorder>(telemetry.metrics)
        assertIs<NoopAnalyticsRecorder>(telemetry.analytics)
        assertIs<NoopCrashRecorder>(telemetry.crash)
    }
}
