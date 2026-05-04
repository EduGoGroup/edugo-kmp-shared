package com.edugo.kmp.telemetry

public interface AnalyticsRecorder {
    public fun trackEvent(name: String, properties: Map<String, Any?> = emptyMap())
    public fun trackScreen(screenName: String, properties: Map<String, Any?> = emptyMap())
    public fun setUserProperty(name: String, value: String?)
    public fun setUserId(userId: String?)
}
