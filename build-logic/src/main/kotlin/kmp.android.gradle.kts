import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kover")
}

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

PlatformFlagsValidator.validateAndReportFlags(project)

val enableAndroid = PlatformFlags.android(project)
// El target Web (wasmJs) requiere DOS condiciones: que el corte global lo pida (enableWeb) y que el
// módulo declare soportar web (kmp.webSupported, default true). Un módulo móvil-only (p.ej. mensajería,
// ADR 0029) pone kmp.webSupported=false y queda fuera de wasmJs aunque el corte sea -PenableWeb=true.
val enableWeb = PlatformFlags.web(project) && PlatformFlags.webSupportedByModule(project)
val enableIos = PlatformFlags.ios(project)
PlatformFlags.registerIosCompatibilityTasks(project, enableIos)

if (enableAndroid) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>(
            "androidLibrary"
        ) {
            compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
            withHostTest {}
        }
    }

    if (enableWeb) {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser {
                testTask { enabled = true }
            }
            binaries.library()
        }
    }

    if (enableIos) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.all {
                linkerOpts("-lsqlite3")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-serialization-json").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }

        if (enableIos) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }

            val iosX64Test by getting
            val iosArm64Test by getting
            val iosSimulatorArm64Test by getting
            val iosTest by creating {
                dependsOn(commonTest)
                iosX64Test.dependsOn(this)
                iosArm64Test.dependsOn(this)
                iosSimulatorArm64Test.dependsOn(this)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
