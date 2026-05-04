plugins {
    `java-platform`
    id("kmp.quality") // DA-17: detekt + ktlint aplicados al BOM (caso degenerado)
    // NOTA P43/P44: `maven-publish` NO se aplica en Fases 0-6 (composite-build local).
    // Fase 7 añade `maven-publish` + `publishing { ... }` cuando el BOM se libere como 0.1.0.
}

group = "com.edugo.kmp"
// Versión nominal opcional. La publicación real (0.1.0) ocurre en Fase 7.
version = "0.1.0-DEV"

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        // Heredados de Fase 2 (3 artefactos)
        api("com.edugo.kmp:foundation:0.1.0-DEV")
        api("com.edugo.kmp:core:0.1.0-DEV")
        api("com.edugo.kmp:validation:0.1.0-DEV")
        // Nuevos en Fase 3 (5 artefactos) — DA-23/P43: composite-build local
        api("com.edugo.kmp:logger:0.1.0-DEV")
        api("com.edugo.kmp:config:0.1.0-DEV")
        api("com.edugo.kmp:storage:0.1.0-DEV")
        api("com.edugo.kmp:settings:0.1.0-DEV")
        api("com.edugo.kmp:telemetry-core:0.1.0-DEV")
        // Nuevos en Fase 4 (2 artefactos) — DA-23/P43: composite-build local
        api("com.edugo.kmp:network:0.1.0-DEV")
        api("com.edugo.kmp:database-core:0.1.0-DEV")
        // Nuevo en Fase 5 (1 artefacto) — DA-12: composite-build local; publicación 0.1.0 en Fase 7
        api("com.edugo.kmp:auth-core:0.1.0-DEV")
    }
}
