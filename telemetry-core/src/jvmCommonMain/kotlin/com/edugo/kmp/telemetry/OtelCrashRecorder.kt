package com.edugo.kmp.telemetry

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.Severity
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicReference

/**
 * Implementación de [CrashRecorder] basada en OpenTelemetry Logs API.
 *
 * - recordException -> LogRecord severity=ERROR con exception.* attrs y stacktrace serializado
 *   (semantic conventions OTel: exception.type, exception.message, exception.stacktrace).
 * - log             -> LogRecord severity=INFO con el mensaje como body.
 * - setUserId       -> stored y añadido como enduser.id en cada emit.
 */
internal class OtelCrashRecorder(
    private val otelLogger: Logger,
) : CrashRecorder {

    private val userId = AtomicReference<String?>(null)

    override fun recordException(throwable: Throwable, context: Map<String, String>) {
        val attrs = Attributes.builder().apply {
            put(AttributeKey.stringKey("exception.type"), throwable::class.qualifiedName ?: "unknown")
            throwable.message?.let { put(AttributeKey.stringKey("exception.message"), it) }
            put(AttributeKey.stringKey("exception.stacktrace"), throwable.stackTraceAsString())
            userId.get()?.let { put(AttributeKey.stringKey("enduser.id"), it) }
            for ((k, v) in context) {
                put(AttributeKey.stringKey(k), v)
            }
        }.build()

        otelLogger.logRecordBuilder()
            .setSeverity(Severity.ERROR)
            .setSeverityText("ERROR")
            .setBody(throwable.message ?: throwable::class.simpleName ?: "exception")
            .setAllAttributes(attrs)
            .emit()
    }

    override fun log(message: String) {
        val attrs = Attributes.builder().apply {
            userId.get()?.let { put(AttributeKey.stringKey("enduser.id"), it) }
        }.build()

        otelLogger.logRecordBuilder()
            .setSeverity(Severity.INFO)
            .setSeverityText("INFO")
            .setBody(message)
            .setAllAttributes(attrs)
            .emit()
    }

    override fun setUserId(userId: String?) {
        this.userId.set(userId)
    }

    private fun Throwable.stackTraceAsString(): String {
        val sw = StringWriter()
        printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
