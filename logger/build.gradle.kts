plugins {
    id("kmp.android")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
    // Fase 7 añade `maven-publish` + `publishing { ... }` cuando el módulo se libere.
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
