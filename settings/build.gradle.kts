plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.settings"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":storage"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
