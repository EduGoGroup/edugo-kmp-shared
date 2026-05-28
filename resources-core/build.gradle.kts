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
            namespace = "com.edugo.kmp.resources"
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.edugo.kmp.resources.generated"
}
