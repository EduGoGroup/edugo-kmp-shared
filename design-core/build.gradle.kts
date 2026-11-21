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
        // Las pruebas de UI con runComposeUiTest viven aquí y no en commonTest: en testAndroidHostTest la
        // variante Android de compose-ui-test necesita el runtime de Android (android.os.Build.FINGERPRINT
        // es null en la JVM del host sin Robolectric) y revienta antes de componer. Las comparten
        // desktop, wasmJs e iOS, que sí las ejecutan.
        val commonTest by getting
        val nonAndroidTest by creating {
            dependsOn(commonTest)
        }
        val desktopTest by getting {
            dependsOn(nonAndroidTest)
            dependencies {
                // Runtime nativo de Skiko resuelto por el OS del build —
                // requerido por runComposeUiTest en desktop. Agnóstico a CI/macOS/Linux/Windows.
                runtimeOnly(compose.desktop.currentOs)
            }
        }
        findByName("wasmJsTest")?.dependsOn(nonAndroidTest)
        findByName("iosTest")?.dependsOn(nonAndroidTest)
    }
}
