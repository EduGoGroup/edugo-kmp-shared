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

// Fase 4 (Data & Network Extraction)
include(":network")
include(":database-core")

// Fase 5 (Auth Decoupling) — DA-12
include(":auth-core")

// Fase 6 (UI Design Extraction)
include(":design-core")
include(":resources-core")

// Cripto (mensajería): crypto_box_seal/open + keygen X25519 sobre libsodium (Ionspin)
include(":crypto")

// Secure storage (mensajería): custodia de Kd_priv + DEK en el almacén seguro del SO
// (Android Keystore vía EncryptedSharedPreferences / iOS Keychain Services).
include(":secure-storage")

// Mensajería = móvil-only (ADR 0029): la web pública NO es custodio de llaves. Estos dos módulos
// renuncian al target Web (wasmJs) declarando `kmp.webSupported = false`. Se fija aquí —y no en el
// `build.gradle.kts` del módulo— porque la convención `kmp.android` añade el target wasmJs durante la
// APLICACIÓN del plugin (bloque `plugins {}`), que corre ANTES del cuerpo del script del módulo; un
// `ext[...]` en el script llegaría tarde. `gradle.beforeProject` inyecta la propiedad antes de evaluar
// el build script, de modo que la convención la ve a tiempo. Efecto: su set real de plataformas es
// android/ios/jvm; libsodium (sin variante wasm) deja de exigirse y el publish atómico cierra.
val mobileOnlyModules = setOf(":crypto", ":secure-storage")
gradle.beforeProject {
    if (path in mobileOnlyModules) {
        extensions.extraProperties.set("kmp.webSupported", "false")
    }
}
