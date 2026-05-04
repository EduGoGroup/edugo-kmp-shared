import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension

plugins {
    id("kmp.ui.full")
    id("kmp.quality")
    // mavenPublishing / publishing { ... } se cablea en Fase 7.
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.design"
        }
    }
}
