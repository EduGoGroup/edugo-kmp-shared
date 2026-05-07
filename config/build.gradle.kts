plugins {
    id("kmp.android")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    // Fase 4 — `expect class EnvVarSource` (beta en Kotlin 2.x). El flag
    // está estabilizado en cuanto a contrato de uso; suprimimos el warning
    // para mantener limpio el output de compilación.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.config"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                implementation(project(":core"))
            }
        }

        // Fase 4 — source set intermedio que comparte el `actual EnvVarSource`
        // entre Desktop y Android (ambos JVM, ambos detectores leen
        // `System.getProperty("app.environment")`).
        val commonTest by getting
        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }
        val desktopTest by getting {
            dependsOn(jvmCommonTest)
        }
        if (enableAndroid) {
            val androidHostTest by getting {
                dependsOn(jvmCommonTest)
            }
        }
    }
}
