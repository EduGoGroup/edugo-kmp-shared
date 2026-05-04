package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.SpyAnalyticsRecorder
import com.edugo.kmp.telemetry.SpyCrashRecorder
import com.edugo.kmp.telemetry.SpyMetricsRecorder
import com.edugo.kmp.telemetry.Telemetry
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTelemetryTest {

    // ==================== recordLogin ====================

    @Test
    fun recordLoginSuccessProducesCounterAndHistogramAndEvent() {
        val spyMetrics = SpyMetricsRecorder()
        val spyAnalytics = SpyAnalyticsRecorder()
        val telemetry = Telemetry(metrics = spyMetrics, analytics = spyAnalytics)

        telemetry.recordLogin(success = true, durationMs = 1500)

        // Counter
        val counters = spyMetrics.counters()
        assertEquals(1, counters.size)
        assertEquals(MetricNames.AUTH_LOGINS_TOTAL, counters[0].name)
        assertEquals("true", counters[0].labels["success"])

        // Histogram
        val histograms = spyMetrics.histograms()
        assertEquals(1, histograms.size)
        assertEquals(MetricNames.AUTH_LOGIN_DURATION_MS, histograms[0].name)
        assertEquals(1500.0, histograms[0].value)
        assertEquals("true", histograms[0].labels["success"])

        // Analytics event
        assertEquals(1, spyAnalytics.events.size)
        assertEquals("login", spyAnalytics.events[0].name)
        assertEquals(true, spyAnalytics.events[0].properties["success"])
        assertEquals(1500L, spyAnalytics.events[0].properties["duration_ms"])
    }

    @Test
    fun recordLoginFailureProducesCorrectLabels() {
        val spyMetrics = SpyMetricsRecorder()
        val spyAnalytics = SpyAnalyticsRecorder()
        val telemetry = Telemetry(metrics = spyMetrics, analytics = spyAnalytics)

        telemetry.recordLogin(success = false, durationMs = 300)

        val counters = spyMetrics.counters()
        assertEquals("false", counters[0].labels["success"])

        assertEquals(false, spyAnalytics.events[0].properties["success"])
        assertEquals(300L, spyAnalytics.events[0].properties["duration_ms"])
    }

    // ==================== recordTokenRefresh ====================

    @Test
    fun recordTokenRefreshProducesCounterAndEvent() {
        val spyMetrics = SpyMetricsRecorder()
        val spyAnalytics = SpyAnalyticsRecorder()
        val telemetry = Telemetry(metrics = spyMetrics, analytics = spyAnalytics)

        telemetry.recordTokenRefresh(success = true)

        assertEquals(1, spyMetrics.counters().size)
        assertEquals(MetricNames.AUTH_TOKEN_REFRESH_TOTAL, spyMetrics.counters()[0].name)
        assertEquals("true", spyMetrics.counters()[0].labels["success"])

        assertEquals(1, spyAnalytics.events.size)
        assertEquals("token_refresh", spyAnalytics.events[0].name)
        assertEquals(true, spyAnalytics.events[0].properties["success"])
    }

    // ==================== setUserId ====================

    @Test
    fun setUserIdPropagatesToAnalyticsAndCrash() {
        val spyAnalytics = SpyAnalyticsRecorder()
        val spyCrash = SpyCrashRecorder()
        val telemetry = Telemetry(analytics = spyAnalytics, crash = spyCrash)

        telemetry.setUserId("user-42")

        assertEquals("user-42", spyAnalytics.lastUserId)
        assertEquals(1, spyAnalytics.setUserIdCallCount)
        assertEquals("user-42", spyCrash.lastUserId)
        assertEquals(1, spyCrash.setUserIdCallCount)
    }

    @Test
    fun setUserIdNullPropagates() {
        val spyAnalytics = SpyAnalyticsRecorder()
        val spyCrash = SpyCrashRecorder()
        val telemetry = Telemetry(analytics = spyAnalytics, crash = spyCrash)

        telemetry.setUserId(null)

        assertEquals(null, spyAnalytics.lastUserId)
        assertEquals(null, spyCrash.lastUserId)
    }

    // ==================== trackLoginScreen ====================

    @Test
    fun trackLoginScreenRecordsScreen() {
        val spyAnalytics = SpyAnalyticsRecorder()
        val telemetry = Telemetry(analytics = spyAnalytics)

        telemetry.trackLoginScreen()

        assertEquals(1, spyAnalytics.screens.size)
        assertEquals("login", spyAnalytics.screens[0].name)
    }
}
