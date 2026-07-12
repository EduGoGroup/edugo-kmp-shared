# edugo-kmp-shared

Repositorio hermano de [`edugo-ui-kmp`](../edugo-ui-kmp/) que aloja los **módulos KMP genéricos /
neutrales** (sin marca EduGo) extraídos del monorepo principal. Publica artefactos Maven con
coordenadas `com.edugo.kmp:<modulo>:<version>` más un BOM (`com.edugo.kmp:edugo-kmp-bom`) que pinea
la matriz de versiones compatibles. Es **Compose / Kotlin Multiplatform**: targets Android, iOS,
Desktop (JVM) y Web (WasmJS).

Regla rectora: **neutralidad**. Aquí va lo reutilizable por cualquier app KMP; lo branded EduGo
(paleta concreta, login, dashboard, seeds, contratos SDUI) se queda en `edugo-ui-kmp`.

> Guía completa para asistentes: `AGENTS.md`. Este README es el resumen de estado y comandos.

## Estado

Repo **maduro**: 15 módulos con código publicable + BOM. Las extracciones planificadas
(`kmp-shared-extraction-v2`, Fases 0-7) ya ocurrieron; los módulos neutrales viven aquí y la app
principal los consume.

### Módulos (`settings.gradle.kts`)

Por capas (Fase de extracción entre paréntesis):

- **Foundation (F1):** `:foundation` (Result<T>, EntityBase/Validatable/Auditable/SoftDeletable,
  AppError/ErrorCode, JsonConfig, PagedResult, DomainMapper — módulo hoja), `:core` (AppDispatchers,
  Platform, sincronización, merge/patch), `:validation` (motor de validación + `AccumulativeValidation`).
- **Infraestructura (F3):** `:logger` (fachada sobre Kermit), `:config` (entornos DEV/STAGING/PROD),
  `:storage` (wrapper type-safe sobre multiplatform-settings), `:settings` (preferencias / estado de
  UI reactivo), `:telemetry-core` (fachada OTel; los sinks por plataforma viven en la app).
- **Datos & red (F4):** `:network` (Ktor Client), `:database-core` (base SQLDelight neutra).
- **Auth (F5, DA-12):** `:auth-core` (orquestación de tokens; define puertos `TokenProvider`,
  `RefreshTokenSource`, `TokenVerifier` que el producto implementa).
- **UI (F6):** `:design-core` (design system neutro sobre Material 3), `:resources-core` (strings
  genéricos vía Compose Resources).
- **Mensajería (móvil-only, ADR 0029):** `:crypto` (crypto_box_seal/open + keygen X25519 sobre
  libsodium) y `:secure-storage` (custodia de llaves en Android Keystore / iOS Keychain). Renuncian
  al target Web (`kmp.webSupported = false`).
- **BOM:** carpeta `bom/` → artefacto `com.edugo.kmp:edugo-kmp-bom`.

## Consumidor principal: `edugo-ui-kmp`

- `edugo-ui-kmp/gradle.properties` declara `includeSharedLocally` (default `true`), que activa
  `includeBuild("../edugo-kmp-shared")` en `edugo-ui-kmp/settings.gradle.kts`.
- La app consume cada módulo vía aliases `libs.edugo.kmp.*` (ver `gradle/libs.versions.toml` de la app).
- Cada módulo neutral suele tener una **contraparte producto** en la app:
  `design-core`↔`:kmp-design`, `resources-core`↔`:kmp-resources`, `auth-core`↔`:modules:auth-edugo`,
  `telemetry-core`↔`:modules:telemetry-{android,ios,web}-edugo`, `database-core`↔`:modules:database-edugo`.

## Release atómico (BOM)

El repo se publica en **un solo Release atómico** que sube todos los módulos + el BOM con la misma
matriz de versiones. Ver **ADR 0014 (`EduGo/docs/adr/0014-release-atomico-edugo-kmp-shared.md`)**.

> Nota operativa: el publish atómico tarda ~30 min. No cancelarlo antes de completar o el BOM queda
> incompleto (le faltaría el módulo raíz). Verificar completitud por módulos raíz, no por conteo de
> packages (paginan).

Modos de consumo:

- **Composite-build local** (default): `includeBuild` desde la app; no requiere publicación.
- **Publicado** (`includeSharedLocally=false`): consume `com.edugo.kmp:*` desde GitHub Packages,
  pineado por el BOM.

## Comandos clave

```bash
./gradlew :bom:tasks          # verifica el stub/artefacto del BOM
./gradlew detekt              # calidad estática (DA-17)
./gradlew ktlintCheck
./gradlew ktlintFormat        # autocorrige donde puede
```

Requisito base del ecosistema: **JDK 21+**. Convenciones Gradle en `build-logic/`.

## Política de versionamiento

Ver decisiones DA-2, DA-5 y DA-6 del plan `kmp-shared-extraction-v2` y el ADR 0014.
