plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false
val enableWeb = findProperty("enableWeb")?.toString()?.toBoolean() ?: false
val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.network"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                api(project(":logger"))
                implementation(project(":core"))
                implementation(project(":telemetry-core"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.encoding)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.client.mock)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        if (enableAndroid) {
            val androidMain by getting {
                dependencies {
                    implementation(libs.ktor.client.okhttp)
                }
            }
        }

        if (enableWeb) {
            val wasmJsMain by getting {
                dependencies {
                    implementation(libs.ktor.client.js)
                }
            }
        }

        if (enableIos) {
            val iosMain by getting {
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}
