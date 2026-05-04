plugins {
    id("kmp.android")
    id("kmp.quality")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
    // Fase 7 cableará `maven-publish` y publicará la versión `0.1.0`.
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>(
            "androidLibrary",
        ) {
            namespace = "com.edugo.kmp.auth"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                api(project(":logger"))
                implementation(project(":config"))
                implementation(project(":core"))
                implementation(project(":validation"))
                implementation(project(":network"))
                implementation(project(":telemetry-core"))
                implementation(project(":storage"))
                implementation(libs.ktor.client.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.client.mock)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.test)
                implementation(libs.turbine)
            }
        }
    }
}
