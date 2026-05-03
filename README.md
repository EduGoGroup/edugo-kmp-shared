# edugo-kmp-shared

Repositorio hermano de [`edugo-ui-kmp`](../edugo-ui-kmp/) que aloja los
módulos KMP genéricos extraídos del monorepo principal. Publica artefactos
Maven con coordenadas `com.edugo.kmp:<modulo>:<version>` y un BOM
(`com.edugo.kmp:edugo-kmp-bom`) que pinea la matriz de versiones
compatibles.

## Estado

- **Fase 0 (Bootstrap):** repo creado con esqueleto Gradle y BOM vacío. Sin
  módulos extraídos todavía. Composite-build cableado desde
  `edugo-ui-kmp/settings.gradle.kts`.
- **Fase 1 (Domain Isolation):** sucede en `edugo-ui-kmp`, NO en este repo.
- **Fase 2+:** comienzan las extracciones de módulos.

Plan vivo: [`edugo-ui-kmp/docs/architecture/kmp-shared-extraction-v2/README.md`](../edugo-ui-kmp/docs/architecture/kmp-shared-extraction-v2/README.md).

## Comandos clave

### Verificar que el BOM stub está disponible (P43/P44 — composite-build only)

```bash
./gradlew :bom:tasks
```

> En Fases 0-6 el BOM NO se publica. Se consume vía `includeBuild("../edugo-kmp-shared")` desde `edugo-ui-kmp`. La publicación real (`0.1.0`) llega en Fase 7.

### Calidad estática (DA-17)

```bash
./gradlew detekt
./gradlew ktlintCheck
./gradlew ktlintFormat   # autocorrige donde puede
```

## Cableado con `edugo-ui-kmp`

`edugo-ui-kmp/gradle.properties` declara la propiedad
`includeSharedLocally`. Por defecto es `true`, lo que activa
`includeBuild("../edugo-kmp-shared")` en `edugo-ui-kmp/settings.gradle.kts`.

> **P43/P44.** En Fases 0-6 el composite-build local es el **único** modo
> soportado. El flag `includeSharedLocally=false` queda reservado para Fase 7+
> cuando el shared se publique como `com.edugo.kmp:*:0.1.0`.

## Estructura

Ver `bom/README.md` para detalles del BOM. Las métricas de referencia
(`BASELINE.md`) viven en el repo principal en
`edugo-ui-kmp/docs/architecture/kmp-shared-extraction-v2/BASELINE.md` (P22).

## Política de versionamiento

Ver decisiones DA-2, DA-5 y DA-6 del plan v2.
