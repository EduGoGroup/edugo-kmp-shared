plugins {
    id("kmp.logic.core")
    // P43/P44: NO se aplica `maven-publish` en Fases 0-6 (composite-build local).
    // Fase 7 añade `maven-publish` + `publishing { ... }` cuando el módulo se libere.
}

// Versión nominal opcional durante composite-build. Puede dejarse en
// "0.1.0-DEV" para debug o omitirse. La versión publicable real (0.1.0)
// se fija en Fase 7.
version = "0.1.0-DEV"
