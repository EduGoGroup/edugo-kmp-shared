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
        api("com.edugo.kmp:foundation:0.1.0-DEV")
        api("com.edugo.kmp:core:0.1.0-DEV")
        api("com.edugo.kmp:validation:0.1.0-DEV")
    }
}
