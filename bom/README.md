# edugo-kmp-bom

Bill of Materials del shared `com.edugo.kmp:*`. En Fase 0 está **vacío**;
las constraints se añaden a partir de Fase 2 cuando aparezcan los
primeros módulos extraídos.

## Política de consumo (P43/P44)

- **Fases 0-6:** el BOM se consume **únicamente vía composite-build local**
  desde la app EduGo (`includeBuild("../edugo-kmp-shared")`). NO se publica a
  `mavenLocal` ni se generan artefactos. Durante esta etapa el BOM no tiene
  versión publicable (puede declararse `0.1.0-DEV` solo si Gradle lo exige).
- **Fase 7:** se aplica `maven-publish`, se fija `version = "0.1.0"` y se
  publica como `com.edugo.kmp:edugo-kmp-bom:0.1.0`.
