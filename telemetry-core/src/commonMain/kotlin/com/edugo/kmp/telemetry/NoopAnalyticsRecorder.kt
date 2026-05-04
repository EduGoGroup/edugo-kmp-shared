package com.edugo.kmp.telemetry

public class NoopAnalyticsRecorder : AnalyticsRecorder {
    override fun trackEvent(name: String, properties: Map<String, Any?>) {}
    override fun trackScreen(screenName: String, properties: Map<String, Any?>) {}
    override fun setUserProperty(name: String, value: String?) {}
    override fun setUserId(userId: String?) {}
}
