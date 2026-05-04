plugins {
    id("kmp.android")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
    // Fase 7 añade `maven-publish` + `publishing { ... }` cuando el módulo se libere.
}

// Versión nominal opcional durante composite-build. Puede dejarse en
// "0.1.0-DEV" para debug o omitirse. La versión publicable real (0.1.0)
// se fija en Fase 7.
version = "0.1.0-DEV"

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
