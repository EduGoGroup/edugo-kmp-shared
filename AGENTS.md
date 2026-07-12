# AGENTS.md — edugo-kmp-shared

> Fuente única de instrucciones para este proyecto (Claude, Gemini, Cursor, Codex…).
> `CLAUDE.md` y `GEMINI.md` solo apuntan aquí. Reglas del ecosistema: `../../AGENTS.md`.
> Convención de docs (estructura fractal): `../../docs/CONVENCIONES.md`.
> El plan vivo de extracción que gobierna este repo vive en el repo principal:
> `../edugo-ui-kmp/docs/architecture/kmp-shared-extraction-v2/README.md`.

## Qué es este repo

Repositorio **hermano** de `edugo-ui-kmp` que aloja los **módulos KMP genéricos / neutrales**
(sin marca EduGo) extraídos del monorepo principal. Publica artefactos Maven con coordenadas
`com.edugo.kmp:<modulo>:<version>` más un BOM (`com.edugo.kmp:edugo-kmp-bom`) que pinea la matriz de
versiones compatibles. Es **Compose/Kotlin Multiplatform**: targets Android, iOS, Desktop (JVM) y
Web (WasmJS).

La regla rectora es la **neutralidad**: aquí va lo reutilizable por cualquier app KMP; lo branded
EduGo (paleta concreta, login, dashboard, seeds, contratos SDUI) se queda en `edugo-ui-kmp`.

## Cómo lo consume `edugo-ui-kmp` (composite-build, NO publicación aún)

- `edugo-ui-kmp/gradle.properties` declara `includeSharedLocally` (default `true`), lo que activa
  `includeBuild("../edugo-kmp-shared")` en `edugo-ui-kmp/settings.gradle.kts`.
- La app consume cada módulo vía aliases `libs.edugo.kmp.*` (ver `gradle/libs.versions.toml` de la app).
- **P43/P44:** en Fases 0-6 el composite-build local es el **único** modo soportado; el BOM **no se
  publica**. `includeSharedLocally=false` y la publicación real (`0.1.0`) quedan para Fase 7+.
- Cada módulo neutral suele tener una **contraparte producto** en la app
  (`design-core`↔`:kmp-design`, `resources-core`↔`:kmp-resources`, `auth-core`↔`:modules:auth-edugo`,
  `telemetry-core`↔`:modules:telemetry-{android,ios,web}-edugo`).

## Módulos que expone (`settings.gradle.kts`)

Por capas (Fase de extracción entre paréntesis):

- **Foundation (F1):** `:foundation` (Result<T>, EntityBase/Validatable/Auditable/SoftDeletable,
  AppError/ErrorCode 1000-6999, JsonConfig, PagedResult, DomainMapper — módulo hoja, sin deps internas),
  `:core` (AppDispatchers, Platform, sincronización, merge/patch helpers; deps: `:foundation`),
  `:validation` (motor de validación + `AccumulativeValidation`; integra con `Result`).
- **Infraestructura (F3):** `:logger` (fachada sobre Kermit), `:config` (entornos DEV/STAGING/PROD,
  URLs/timeouts), `:storage` (wrapper type-safe sobre multiplatform-settings), `:settings`
  (preferencias de usuario / estado de UI reactivo), `:telemetry-core` (fachada OTel/telemetría
  desacoplada de proveedor; los sinks por plataforma viven en la app).
- **Datos & red (F4):** `:network` (Ktor Client: serialización, interceptores, reintentos,
  conectividad), `:database-core` (base SQLDelight neutra; los `.sq` EduGo viven en `:database-edugo`).
- **Auth (F5, DA-12):** `:auth-core` — orquestación de tokens (refresh, single-flight, circuit
  breaker, retry, JWT parser, rate limiter, `AuthInterceptor`). Define **puertos** que el producto
  implementa: `TokenProvider`, `RefreshTokenSource`, `TokenVerifier`.
- **UI (F6):** `:design-core` (design system neutro: tokens, tipografía, `DSTheme` parametrizable,
  familia `DS*` sobre Material 3 — sin marca), `:resources-core` (strings genéricos vía Compose
  Resources — sin marca).
- **Mensajería (móvil-only, ADR 0029):** `:crypto` (crypto_box_seal/open + keygen X25519 sobre
  libsodium/Ionspin) y `:secure-storage` (custodia de Kd_priv + DEK en Android Keystore / iOS
  Keychain). Renuncian al target Web (`kmp.webSupported = false`, fijado en `settings.gradle.kts`).
- **BOM:** carpeta `bom/` → `project.name = "edugo-kmp-bom"` (artefacto `com.edugo.kmp:edugo-kmp-bom`).

## Convenciones (mismas que la familia EduGo KMP)

- **Idioma:** código en inglés; documentación y logs en español.
- **Errores:** nada de excepciones para negocio/red — retornar `Result<T>` de `:foundation`.
- **Pureza `commonMain`:** lógica al 100% en `commonMain`; plataforma vía `expect/actual` o DI. No
  `android.*`/`platform.*` ni `Dispatchers.IO/Main` directos (usa `AppDispatchers` de `:core`).
- **Neutralidad:** ningún módulo de este repo puede importar símbolos branded EduGo. Si necesitas algo
  EduGo-específico, va en la app como contraparte producto y se inyecta vía puerto/DI.
- **DI:** se inyectan interfaces; las impls concretas las aporta el consumidor.
- **Versionamiento:** decisiones DA-2/DA-5/DA-6 del plan v2. BOM vacío hasta Fase 2.

## Build & calidad

- Composite-build: `./gradlew :bom:tasks` (verifica el stub del BOM, modo composite-only).
- Calidad estática (DA-17): `./gradlew detekt`, `./gradlew ktlintCheck`, `./gradlew ktlintFormat`.
- Convenciones Gradle en `build-logic/` (`kmp.logic.core`, `kmp.ui.full`, `kmp.quality`, `kover`,
  `PlatformFlags`). Requisito base del ecosistema: JDK 21+.

## Antes de editar

- Lee el `README.md` (y `gemini.md` si existe) del módulo afectado — son guías locales por área.
- No tocar `build.gradle.kts`/`settings.gradle.kts` ni `.kt` sin confirmar el alcance.
- Si extraes algo nuevo desde la app, sigue el plan `kmp-shared-extraction-v2` (Fases 0-7) y verifica
  que el símbolo sea **neutral** antes de moverlo; lo branded no entra aquí.
