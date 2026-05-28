plugins {
    id("kmp.android")
    id("kmp.publish")
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings.test)
            }
        }

        val sqlPlatformsMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.sqldelight.runtime)
            }
        }

        val desktopMain by getting {
            dependsOn(sqlPlatformsMain)
            dependencies {
                api(libs.sqldelight.sqlite.driver)
            }
        }

        if (enableAndroid) {
            val androidMain by getting {
                dependsOn(sqlPlatformsMain)
                dependencies {
                    api(libs.sqldelight.android.driver)
                }
            }
        }

        if (enableIos) {
            val iosMain by getting {
                dependsOn(sqlPlatformsMain)
                dependencies {
                    api(libs.sqldelight.native.driver)
                }
            }
        }
    }
}
