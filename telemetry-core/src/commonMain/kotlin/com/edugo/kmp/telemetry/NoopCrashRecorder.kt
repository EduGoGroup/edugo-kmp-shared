package com.edugo.kmp.telemetry

public class NoopCrashRecorder : CrashRecorder {
    override fun recordException(throwable: Throwable, context: Map<String, String>) {}
    override fun log(message: String) {}
    override fun setUserId(userId: String?) {}
}
