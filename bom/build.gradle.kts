plugins {
    `java-platform`
    id("kmp.quality") // DA-17: detekt + ktlint aplicados al BOM (caso degenerado)
    // NOTA P43/P44: `maven-publish` NO se aplica en Fases 0-6 (composite-build local).
    // Fase 7 añade `maven-publish` + `publishing { ... }` cuando el BOM se libere como 0.1.0.
}

group = "com.edugo.kmp"
// En Fases 0-6 el BOM no tiene versión publicable. Se omite `version` durante el
// composite-build local; opcionalmente puede usarse `version = "0.1.0-DEV"` para
// debug. La versión real (0.1.0) la fija Fase 7.

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        // VACÍO en Fase 0. Las constraints se poblarán cuando los módulos vivan en
        // shared (Fase 2+) y serán consumidas vía composite-build, no vía mavenLocal.
    }
}
