package com.edugo.kmp.telemetry

public interface MetricsRecorder {
    public fun counter(name: String, value: Double = 1.0, labels: Map<String, String> = emptyMap())
    public fun histogram(name: String, value: Double, labels: Map<String, String> = emptyMap())
    public fun gauge(name: String, value: Double, labels: Map<String, String> = emptyMap())
}
