package com.edugo.kmp.telemetry

public interface CrashRecorder {
    public fun recordException(throwable: Throwable, context: Map<String, String> = emptyMap())
    public fun log(message: String)
    public fun setUserId(userId: String?)
}
