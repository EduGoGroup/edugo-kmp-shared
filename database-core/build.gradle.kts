plugins {
    id("kmp.android")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
    // IMPORTANTE: NO se aplica `app.cash.sqldelight` aquí. database-core
    // no contiene archivos .sq; sólo expone facilities + runtime + drivers.
    // El plugin SQLDelight vive en database-edugo.
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false
val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.database"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                implementation(project(":storage"))
                implementation(project(":telemetry-core"))
                api(libs.sqldelight.runtime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings.test)
            }
        }
        val desktopMain by getting {
            dependencies {
                api(libs.sqldelight.sqlite.driver)
            }
        }

        if (enableAndroid) {
            val androidMain by getting {
                dependencies {
                    api(libs.sqldelight.android.driver)
                }
            }
        }

        if (enableIos) {
            val iosMain by getting {
                dependencies {
                    api(libs.sqldelight.native.driver)
                }
            }
        }
    }
}
