package com.edugo.kmp.telemetry

data class AnalyticsCall(
    val type: String,
    val name: String,
    val properties: Map<String, Any?> = emptyMap()
)

data class UserPropertyCall(
    val name: String,
    val value: String?
)

class SpyAnalyticsRecorder : AnalyticsRecorder {

    val events: MutableList<AnalyticsCall> = mutableListOf()
    val screens: MutableList<AnalyticsCall> = mutableListOf()
    val userProperties: MutableList<UserPropertyCall> = mutableListOf()
    var lastUserId: String? = null
    var setUserIdCallCount: Int = 0

    override fun trackEvent(name: String, properties: Map<String, Any?>) {
        events.add(AnalyticsCall("event", name, properties))
    }

    override fun trackScreen(screenName: String, properties: Map<String, Any?>) {
        screens.add(AnalyticsCall("screen", screenName, properties))
    }

    override fun setUserProperty(name: String, value: String?) {
        userProperties.add(UserPropertyCall(name, value))
    }

    override fun setUserId(userId: String?) {
        lastUserId = userId
        setUserIdCallCount++
    }
}
