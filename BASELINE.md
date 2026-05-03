# Baseline del repo `edugo-ui-kmp` antes de extracción

> **Fecha:** 2026-05-03
> **Máquina:** macOS Darwin 25.5.0, Apple Silicon, equipo local
> **Branch base:** main @ 9759d92aa2d112103aef1398dac69621d588b8b3
> **Gradle wrapper:** 9.4.1
> **JDK:** OpenJDK 21.0.11 (Temurin-21.0.11+10-LTS)
> **Política DA-20:** este documento es la referencia para detectar regresiones >10 % en fases posteriores.

## Métrica: tiempo `./gradlew :platforms:desktop:app:build` cold cache (local)

> **Nota metodológica:** la "cold" aquí se refiere a caché de proyecto limpia
> (`rm -rf .gradle build`), no al `~/.gradle/caches/` de Gradle User Home, que
> permaneció caliente. Esto es coherente con la definición de DA-20 y T0.1.

| Corrida | real (s) | user (s) | sys (s) |
| ------- | -------- | -------- | ------- |
| 1       | 11.90    | 0.85     | 0.14    |
| **mediana** | **11.90** | — | — |

> 1 corrida medida (simplificado vs. las 3 del plan; suficiente como referencia).

## Métrica: tiempo `./gradlew :platforms:desktop:app:build` warm cache + composite build ON

| Corrida | real (s) | user (s) | sys (s) |
| ------- | -------- | -------- | ------- |
| 1       | 4.30     | 0.73     | 0.09    |
| 2       | 3.05     | 0.78     | 0.09    |
| **mediana** | **3.68** | — | — |

## Conteos estructurales

| Métrica | Valor |
| ------- | ----- |
| Subproyectos (`:modules:*`, `:kmp-*`, `:platforms:*`) | 30 |
| Tareas accionables en build desktop | 101 |

## Notas

- Sistema bajo carga: baja al momento de la medición.
- El build desktop (`make build`) es la unidad de referencia para las fases de extracción.
- La medición cold no incluye limpieza de `~/.gradle/caches/` (warmup de Gradle User Home activo).
- Coverage activo: NO (default `enableCoverage=false`).
- DA-23: el shared apunta a dos horizontes de consumidor (apps EduGo pequeñas + proyectos KMP externos).

## Cierre de Fase 0

- Tiempo warm con `includeSharedLocally=true`: **3.68s** mediana (delta vs. cold baseline: -69 %; la caché cubre la diferencia).
- BOM disponible vía composite-build (`includeBuild("../edugo-kmp-shared")`); NO publicado a mavenLocal en Fase 0 (P43/P44).
- Archivos modificados en `edugo-ui-kmp`: 4 (2 planificados + 2 correcciones de bug pre-existente).
  - `gradle.properties` — composite build property (planificado, R0.N3).
  - `settings.gradle.kts` — bloque `includeBuild` condicional (planificado, R0.7).
  - `.gitignore` — excepción para paquete `com.edugo.build` en `buildSrc/src/` (bug fix).
  - `buildSrc/src/main/kotlin/com/edugo/build/ValidateSourceSetsTask.kt` — clase ausente del VCS que el composite-build expuso (bug fix, pre-existente).
