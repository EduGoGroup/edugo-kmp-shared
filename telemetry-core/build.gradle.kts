plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.telemetry"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":foundation"))
                implementation(project(":core"))
                // SIN sqldelight.runtime aquí — el outbox SQLDelight migró a
                // telemetry-android-edugo. La API common de telemetry-core no
                // requiere SQLDelight (DA-16, R3.15).
            }
        }

        // SourceSet compartido entre Desktop (JVM) y Android (también JVM)
        // — preserva el patrón actual del módulo. En este shared, solo
        // desktopMain consume jvmCommonMain; los recorders Android viven
        // en telemetry-android-edugo y reusan la misma API common.
        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.opentelemetry.api)
                implementation(libs.opentelemetry.sdk)
                implementation(libs.opentelemetry.sdk.trace)
                implementation(libs.opentelemetry.sdk.metrics)
                implementation(libs.opentelemetry.sdk.logs)
                implementation(libs.opentelemetry.exporter.otlp)
                implementation(libs.opentelemetry.exporter.common)
                implementation(libs.opentelemetry.exporter.otlp.common)
            }
        }

        val desktopMain by getting { dependsOn(jvmCommonMain) }

        // El sourceSet androidMain de `telemetry-core` enlaza con
        // `jvmCommonMain` para que los consumidores Android (en particular
        // `:modules:telemetry-android-edugo` que vive en la app) puedan
        // resolver `OtelTelemetryFactory`, `OtelMetricsRecorder`, etc. al
        // construir sus recorders especializados (DA-16, exposición de
        // API JVM compartida sin replicar clases). NO contiene código
        // Android-específico — los `Buffered*Processor`, `OtelOutboxWorker`
        // y `AndroidTelemetryConfigBuilder` viven en
        // `:modules:telemetry-android-edugo` (R3.2 / DA-16).
        if (enableAndroid) {
            val androidMain by getting { dependsOn(jvmCommonMain) }
        }

        // iosMain / wasmJsMain NO se enlazan aquí — los recorders nativos
        // viven en `telemetry-{ios,web}-edugo` (locales, DA-16).
    }
}
