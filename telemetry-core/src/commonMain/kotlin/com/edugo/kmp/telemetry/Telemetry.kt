package com.edugo.kmp.telemetry

import com.edugo.kmp.telemetry.tracing.NoopTracer
import com.edugo.kmp.telemetry.tracing.Tracer

public class Telemetry(
    public val metrics: MetricsRecorder = NoopMetricsRecorder(),
    public val analytics: AnalyticsRecorder = NoopAnalyticsRecorder(),
    public val crash: CrashRecorder = NoopCrashRecorder(),
    public val tracer: Tracer = NoopTracer.Instance,
) {
    public companion object {
        public val Noop: Telemetry = Telemetry()
    }
}
