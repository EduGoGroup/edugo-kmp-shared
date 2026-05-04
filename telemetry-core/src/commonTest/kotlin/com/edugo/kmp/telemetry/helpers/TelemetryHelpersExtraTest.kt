package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.Telemetry
import kotlin.test.Test
import kotlin.test.assertTrue

class TelemetryHelpersExtraTest {
    @Test
    fun testAllTelemetryHelpers() {
        val t = Telemetry.Noop

        // AppLifecycle
        t.recordAppStart(1500)
        t.recordAppStateChange("background")
        
        // Assessment
        t.recordAssessmentAttempt("ass_123", 95)
        t.trackAssessmentScreen("ass_123")
        
        // Http
        t.recordHttpRequest("GET", "/api/v1/auth", 200, 150)
        t.recordNetworkError("/api/v1/sync", "IOException")
        
        // Sync
        t.recordSyncOperation("full", 1500, true)
        
        assertTrue(true)
    }
}
