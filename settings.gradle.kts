rootProject.name = "edugo-kmp-shared"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":bom")
// El BOM se publica como artefacto `edugo-kmp-bom` (design.md §5.4). Como la
// carpeta del subproyecto se llama `bom/`, renombramos `project.name` para
// que el composite-build resuelva la coordenada `com.edugo.kmp:edugo-kmp-bom`
// usada por los aliases del catalog en la app.
project(":bom").name = "edugo-kmp-bom"
include(":foundation")
include(":core")
include(":validation")

// Fase 3 (Infrastructure Extraction)
include(":logger")
include(":config")
include(":storage")
include(":settings")
include(":telemetry-core")
