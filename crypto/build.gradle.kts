plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.crypto"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))
                // libsodium multiplataforma (Ionspin): crypto_box_seal/sealOpen + keygen X25519 en
                // commonMain (jvm/android/iosArm64/iosSimulatorArm64). Misma libsodium C → el
                // sello es byte-compatible con el backend Go (nacl/box + box.SealAnonymous).
                // NO hay target Web: libsodium 0.9.5 no publica variante wasm y la mensajería es
                // móvil-only (ADR 0029); el módulo declara `kmp.webSupported = false` en settings.gradle.kts.
                api(libs.ionspin.libsodium.bindings)
            }
        }

        // Las pruebas de SealedBoxCrypto necesitan cargar la libsodium nativa del binding. En
        // testAndroidHostTest se ejecuta el `actual` Android de Ionspin sobre la JVM del host: el AAR solo
        // trae los .so de Android (jni/<abi>/libsodium.so), no la librería del SO del host, y la carga
        // revienta. Por eso viven en este conjunto, que comparten desktop (JVM, con la nativa del host)
        // e iOS (libsodium estática), y no en commonTest.
        val commonTest by getting
        val nonAndroidTest by creating {
            dependsOn(commonTest)
        }
        val desktopTest by getting {
            dependsOn(nonAndroidTest)
        }
        findByName("iosTest")?.dependsOn(nonAndroidTest)
    }
}
