package com.edugo.kmp.telemetry

data class MetricsCall(
    val type: String,
    val name: String,
    val value: Double,
    val labels: Map<String, String>
)

class SpyMetricsRecorder : MetricsRecorder {

    val calls: MutableList<MetricsCall> = mutableListOf()

    override fun counter(name: String, value: Double, labels: Map<String, String>) {
        calls.add(MetricsCall("counter", name, value, labels))
    }

    override fun histogram(name: String, value: Double, labels: Map<String, String>) {
        calls.add(MetricsCall("histogram", name, value, labels))
    }

    override fun gauge(name: String, value: Double, labels: Map<String, String>) {
        calls.add(MetricsCall("gauge", name, value, labels))
    }

    fun counters(): List<MetricsCall> = calls.filter { it.type == "counter" }
    fun histograms(): List<MetricsCall> = calls.filter { it.type == "histogram" }
    fun gauges(): List<MetricsCall> = calls.filter { it.type == "gauge" }
}
