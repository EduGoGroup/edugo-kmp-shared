plugins {
    `java-platform`
    id("kmp.quality")
    id("kmp.publish")
}

javaPlatform {
    allowDependencies()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

// Las versiones de cada módulo se leen desde versions.properties (raíz del repo),
// que mantiene la última versión publicada en GitHub Packages por módulo.
// El workflow publish.yml actualiza ese archivo en cada release puntual.
val publishedVersions = java.util.Properties().apply {
    rootProject.file("versions.properties").inputStream().use { load(it) }
}

fun published(module: String): String =
    publishedVersions.getProperty(module)
        ?: error("Falta la versión de '$module' en versions.properties")

dependencies {
    constraints {
        // Heredados de Fase 2 (3 artefactos)
        api("com.edugo.kmp:foundation:${published("foundation")}")
        api("com.edugo.kmp:core:${published("core")}")
        api("com.edugo.kmp:validation:${published("validation")}")
        // Nuevos en Fase 3 (5 artefactos)
        api("com.edugo.kmp:logger:${published("logger")}")
        api("com.edugo.kmp:config:${published("config")}")
        api("com.edugo.kmp:storage:${published("storage")}")
        api("com.edugo.kmp:settings:${published("settings")}")
        api("com.edugo.kmp:telemetry-core:${published("telemetry-core")}")
        // Nuevos en Fase 4 (2 artefactos)
        api("com.edugo.kmp:network:${published("network")}")
        api("com.edugo.kmp:database-core:${published("database-core")}")
        // Nuevo en Fase 5 (1 artefacto) — DA-12
        api("com.edugo.kmp:auth-core:${published("auth-core")}")
        // Nuevos en Fase 6 (2 artefactos)
        api("com.edugo.kmp:design-core:${published("design-core")}")
        api("com.edugo.kmp:resources-core:${published("resources-core")}")
    }
}
