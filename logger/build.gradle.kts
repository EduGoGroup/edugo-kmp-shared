plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.logger"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))                  // composite-build resuelve a com.edugo.kmp:core
                implementation(libs.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kermit)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.slf4j.nop)
            }
        }
    }
}
