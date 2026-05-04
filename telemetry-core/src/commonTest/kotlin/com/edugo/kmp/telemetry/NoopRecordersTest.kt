package com.edugo.kmp.telemetry

import kotlin.test.Test

class NoopRecordersTest {

    // ==================== NoopMetricsRecorder ====================

    @Test
    fun noopMetricsCounterNeverFails() {
        val recorder = NoopMetricsRecorder()
        recorder.counter("name")
        recorder.counter("name", 0.0)
        recorder.counter("name", -1.0)
        recorder.counter("name", Double.MAX_VALUE)
        recorder.counter("name", Double.NaN)
        recorder.counter("name", 1.0, mapOf("a" to "b", "c" to "d"))
        recorder.counter("", 1.0, emptyMap())
    }

    @Test
    fun noopMetricsHistogramNeverFails() {
        val recorder = NoopMetricsRecorder()
        recorder.histogram("name", 0.0)
        recorder.histogram("name", -100.0)
        recorder.histogram("name", Double.MAX_VALUE)
        recorder.histogram("name", Double.NaN)
        recorder.histogram("name", 1.0, mapOf("x" to "y"))
        recorder.histogram("", 0.0, emptyMap())
    }

    @Test
    fun noopMetricsGaugeNeverFails() {
        val recorder = NoopMetricsRecorder()
        recorder.gauge("name", 0.0)
        recorder.gauge("name", -50.0)
        recorder.gauge("name", Double.MAX_VALUE)
        recorder.gauge("name", Double.NaN)
        recorder.gauge("name", 1.0, mapOf("key" to "val"))
        recorder.gauge("", 0.0, emptyMap())
    }

    // ==================== NoopAnalyticsRecorder ====================

    @Test
    fun noopAnalyticsTrackEventNeverFails() {
        val recorder = NoopAnalyticsRecorder()
        recorder.trackEvent("event")
        recorder.trackEvent("event", mapOf("key" to "value"))
        recorder.trackEvent("event", mapOf("key" to null))
        recorder.trackEvent("event", mapOf("key" to 42, "other" to listOf(1, 2, 3)))
        recorder.trackEvent("", emptyMap())
    }

    @Test
    fun noopAnalyticsTrackScreenNeverFails() {
        val recorder = NoopAnalyticsRecorder()
        recorder.trackScreen("screen")
        recorder.trackScreen("screen", mapOf("tab" to "home"))
        recorder.trackScreen("screen", mapOf("key" to null))
        recorder.trackScreen("", emptyMap())
    }

    @Test
    fun noopAnalyticsSetUserPropertyNeverFails() {
        val recorder = NoopAnalyticsRecorder()
        recorder.setUserProperty("name", "value")
        recorder.setUserProperty("name", null)
        recorder.setUserProperty("", "")
        recorder.setUserProperty("", null)
    }

    @Test
    fun noopAnalyticsSetUserIdNeverFails() {
        val recorder = NoopAnalyticsRecorder()
        recorder.setUserId("user-123")
        recorder.setUserId(null)
        recorder.setUserId("")
    }

    // ==================== NoopCrashRecorder ====================

    @Test
    fun noopCrashRecordExceptionNeverFails() {
        val recorder = NoopCrashRecorder()
        recorder.recordException(RuntimeException("test"))
        recorder.recordException(RuntimeException("test"), mapOf("ctx" to "val"))
        recorder.recordException(IllegalStateException("state"), emptyMap())
        recorder.recordException(Error("error"), mapOf("a" to "b", "c" to "d"))
    }

    @Test
    fun noopCrashLogNeverFails() {
        val recorder = NoopCrashRecorder()
        recorder.log("message")
        recorder.log("")
        recorder.log("A".repeat(10000))
    }

    @Test
    fun noopCrashSetUserIdNeverFails() {
        val recorder = NoopCrashRecorder()
        recorder.setUserId("user-123")
        recorder.setUserId(null)
        recorder.setUserId("")
    }
}
