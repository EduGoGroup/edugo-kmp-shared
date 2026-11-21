import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension

plugins {
    id("kmp.ui.full")
    id("kmp.quality")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.design"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Los tokens sin Compose (Breakpoint, SemanticTone, PlatformDetector…) viven en
                // :design-tokens con los mismos paquetes; `api` para que quien consume
                // design-core los siga viendo sin declarar nada (deuda 131).
                api(project(":design-tokens"))
            }
        }
        val desktopTest by getting {
            dependencies {
                // Runtime nativo de Skiko resuelto por el OS del build —
                // requerido por runComposeUiTest en desktop. Agnóstico a CI/macOS/Linux/Windows.
                runtimeOnly(compose.desktop.currentOs)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    if (name.contains("AndroidHostTest", ignoreCase = true)) {
        exclude("**/components/buttons/ButtonContentTest*")
        exclude("**/components/lists/DSListRowTest*")
    }
}
