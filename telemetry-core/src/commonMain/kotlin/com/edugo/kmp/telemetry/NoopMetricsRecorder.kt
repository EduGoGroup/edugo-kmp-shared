package com.edugo.kmp.telemetry

public class NoopMetricsRecorder : MetricsRecorder {
    override fun counter(name: String, value: Double, labels: Map<String, String>) {}
    override fun histogram(name: String, value: Double, labels: Map<String, String>) {}
    override fun gauge(name: String, value: Double, labels: Map<String, String>) {}
}
