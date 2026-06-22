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
                // commonMain (jvm/android/iosX64/iosArm64/iosSimulatorArm64). Misma libsodium C → el
                // sello es byte-compatible con el backend Go (nacl/box + box.SealAnonymous).
                api(libs.ionspin.libsodium.bindings)
            }
        }
    }
}
