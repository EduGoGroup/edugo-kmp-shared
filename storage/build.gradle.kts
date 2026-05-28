plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.storage"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                implementation(project(":logger"))
                // DA-MPH-6: ResetClientStateUseCase consume Telemetry + AnalyticsRecorder + CrashRecorder
                // del módulo telemetry-core. Agregado como `api` porque el use case lo expone como
                // parámetro de constructor público (clientes que instancian el use case necesitan ver Telemetry).
                api(project(":telemetry-core"))
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.multiplatform.settings.make.observable)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
