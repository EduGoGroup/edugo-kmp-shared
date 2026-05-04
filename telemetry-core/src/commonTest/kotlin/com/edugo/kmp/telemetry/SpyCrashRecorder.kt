package com.edugo.kmp.telemetry

data class ExceptionCall(
    val throwable: Throwable,
    val context: Map<String, String>
)

class SpyCrashRecorder : CrashRecorder {

    val exceptions: MutableList<ExceptionCall> = mutableListOf()
    val logs: MutableList<String> = mutableListOf()
    var lastUserId: String? = null
    var setUserIdCallCount: Int = 0

    override fun recordException(throwable: Throwable, context: Map<String, String>) {
        exceptions.add(ExceptionCall(throwable, context))
    }

    override fun log(message: String) {
        logs.add(message)
    }

    override fun setUserId(userId: String?) {
        lastUserId = userId
        setUserIdCallCount++
    }
}
