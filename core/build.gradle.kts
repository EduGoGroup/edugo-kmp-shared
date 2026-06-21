plugins {
    id("kmp.android")
    id("kmp.publish")
}

// :core declara el target Android (vía kmp.android) porque su `androidMain` aporta `actual` con
// comportamiento divergente — p.ej. `Platform.name = "Android"` (Platform.android.kt). Antes usaba
// `kmp.logic.core` (que NO declara androidTarget), así que la app Android resolvía la variante
// desktop/JVM de :core y `Platform.name` devolvía "JVM" en runtime (gate de plataforma roto). Mismo
// molde que :network/:auth-core. foundation/validation siguen en kmp.logic.core: son lógica pura
// sin `actual` de plataforma, su variante JVM en Android es inocua.
val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

kotlin {
    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.core"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))   // composite-build local resuelve el subproyecto
            }
        }
    }
}
