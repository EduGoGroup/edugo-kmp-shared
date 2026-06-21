plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false
val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.securestorage"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
            }
        }

        if (enableAndroid) {
            val androidMain by getting {
                dependencies {
                    // EncryptedSharedPreferences + MasterKey: cifra los valores con una clave AES256-GCM
                    // que vive en el Android Keystore (hardware-backed cuando el SoC lo soporta).
                    implementation(libs.androidx.security.crypto)
                }
            }
        }

        // iosMain (cuando enableIos) usa platform.Security (Keychain Services) — sin dependencias externas.
        // desktopMain y wasmJsMain caen al actual de cada plataforma definido en sus carpetas de fuentes.
    }
}
