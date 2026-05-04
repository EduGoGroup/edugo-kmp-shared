package com.edugo.kmp.network.e2e

import com.edugo.kmp.network.interceptor.TelemetryInterceptor
import com.edugo.kmp.telemetry.OtelTelemetryFactory
import com.edugo.kmp.telemetry.TelemetryConfig
import com.edugo.kmp.telemetry.tracing.SpanKind
import com.edugo.kmp.telemetry.tracing.withSpan
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

/**
 * Smoke E2E: lanza una request real desde Desktop al `edugo-api-identity`
 * y valida que `traceparent` viaja correctamente hasta Tempo.
 *
 * Skip por default — solo corre cuando la env var `EDUGO_E2E_TRACE=1` esta
 * presente. Necesita:
 *  - `edugo-observability` corriendo (`make up` desde `edugo-observability/`)
 *  - 4 APIs Go corriendo (`make up` desde `EduBack/`)
 *
 * Imprime en stdout el `trace_id` generado para que el caller pueda hacer
 * `curl http://localhost:3000/api/datasources/proxy/uid/tempo/api/traces/<id>`
 * y verificar la traza completa Desktop CLIENT -> Go SERVER -> usecase/repo.
 */
class TraceE2EProbe {

    @Test
    fun postLoginAndPrintTraceId() {
        if (System.getenv("EDUGO_E2E_TRACE") != "1") {
            println("[E2E] Skipped (set EDUGO_E2E_TRACE=1 to run)")
            return
        }

        val telemetry = OtelTelemetryFactory.create(
            TelemetryConfig(
                serviceName = "edugo-ui-kmp-desktop",
                serviceVersion = "0.0.0-e2e",
                deploymentEnvironment = "local",
                endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: "http://localhost:4318",
            ),
        )
        val interceptor = TelemetryInterceptor(telemetry)
        val httpClient = HttpClient(CIO)

        runBlocking {
            val parent = telemetry.tracer.startSpan(
                name = "e2e.login_probe",
                kind = SpanKind.INTERNAL,
                attributes = mapOf("edugo.feature" to "auth"),
            )
            println("[E2E] parent trace_id=${parent.context.traceIdHex}")
            try {
                withSpan(parent) {
                    val builder = HttpRequestBuilder().apply {
                        method = HttpMethod.Post
                        url(urlString = "http://localhost:8070/api/v1/auth/login")
                        contentType(ContentType.Application.Json)
                        setBody("""{"email":"e2e-trace@example.com","password":"wrong"}""")
                    }
                    interceptor.interceptRequest(builder)
                    val tp = builder.headers.build()["traceparent"]
                    println("[E2E] injected traceparent=$tp")
                    runCatching {
                        val response = httpClient.post(builder)
                        interceptor.interceptResponse(response)
                        println("[E2E] http_status=${response.status.value}")
                    }.onFailure { exc ->
                        interceptor.onError(builder, exc)
                        println("[E2E] request threw: ${exc::class.simpleName}: ${exc.message}")
                    }
                }
            } finally {
                parent.end()
            }
            // Esperar al BatchSpanProcessor flush (default 5s) + un margen.
            delay(8_000)
            httpClient.close()
            OtelTelemetryFactory.shutdown()
        }
    }
}
