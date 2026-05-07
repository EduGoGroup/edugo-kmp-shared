# Estándar de detección de entorno — `edugo-kmp-shared/config`

> Documento contractual. Define cómo cada plataforma del proyecto EduGo UI KMP recibe, valida y consume la variable de entorno que determina qué archivo `config-{env}.json` se carga en el arranque.
>
> **Estado**: Fases 1 + 2 + 3 + 4 cerradas. Las 4 plataformas tienen pipeline `-Penv=` end-to-end; iOS tiene default seguro local; el `ResourceLoader` web pre-carga el JSON real desde el bundle; matriz completa de run configs IntelliJ + schemes Xcode versionados (`<Plataforma>-<AMBIENTE>.run.xml` y `iosApp - <AMBIENTE>.xcscheme`); framework parametrizado de tests (`AppEnvVar` + `EnvVarMatrix` + `EnvVarSource`) corre el mismo contrato sobre las 4 plataformas con mocks de sysprop / `NSBundle` / DOM.

---

## 1. Principio rector

> Cada plataforma puede tener su mecanismo nativo de extracción, pero **todas deben cumplir el mismo contrato**: leer una variable real de entorno (no heurísticas), mapearla a un `Environment`, cargar el `config-{env}.json` correspondiente e inyectarlo vía Koin.

**No se aceptan heurísticas como fuente de verdad**: ni `hostname == "localhost"`, ni `Debug.isDebuggerConnected()`, ni `BuildConfig.DEBUG → DEV`. Si la variable no llega, el sistema **falla con un mensaje accionable**; no adivina.

---

## 2. Variable canónica

| Concepto | Valor |
|---|---|
| **Nombre canónico (interfaz externa)** | `APP_ENVIRONMENT` |
| **Alias Gradle CLI** | `-Penv=<valor>` |
| **Valores válidos** | `DEV` &#124; `DEV_LAN` &#124; `STAGING` &#124; `PROD` |
| **Case sensitivity** | El parser acepta cualquier casing y normaliza vía `Environment.fromString` |
| **Default si falta** | `IllegalStateException` con mensaje accionable. Sin defaults silenciosos. |

`APP_ENVIRONMENT` es el **único nombre que un dev, un script de CI o un run config necesita conocer**. Cómo cada plataforma lo internaliza (system property JVM, `BuildConfig`, `Info.plist`, meta tag HTML) es detalle de implementación.

---

## 3. Mecanismos por plataforma

Cada plataforma respeta la convención de naming de su ecosistema en el identificador interno, pero todas exponen la misma variable externa `APP_ENVIRONMENT`.

### 3.1 Desktop (JVM)

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | constante de build `BUILD_ENVIRONMENT` (generada por Gradle desde `-Penv=`) |
| Identificador alternativo | JVM system property `app.environment` (poblada automáticamente por el `application` block) o env var del SO `APP_ENVIRONMENT` |
| Cómo se pasa por CLI | `./gradlew :platforms:desktop:app:run -Penv=STAGING` |
| Cómo se pasa por IDE | Run config versionado `.run/Desktop-<AMBIENTE>.run.xml` (tipo `GradleRunConfiguration`, `scriptParameters="-Penv=<AMBIENTE>"`, task `:platforms:desktop:app:run`). Una variante por ambiente: `Desktop-DEV`, `Desktop-DEV_LAN`, `Desktop-STAGING`, `Desktop-PROD`. |
| Si falta | `IllegalStateException` con el mensaje accionable de §5 (BUILD_ENVIRONMENT, sysprop o env var). |

**Bridge BUILD_ENVIRONMENT → EnvironmentDetector** (mismo patrón que Android/Web):
`platforms/desktop/app/build.gradle.kts` registra `generateBuildConfig` que escribe `BUILD_ENVIRONMENT: String` desde `-Penv=`. `Main.kt` lo lee, valida con `Environment.fromString(...)` y llama `EnvironmentDetector.forceEnvironment(...)` antes de `buildTelemetry()`. Si `-Penv=` no se pasó, el bridge se salta y el detector cae a la system property `app.environment` (que el `application` block del Compose Desktop popula automáticamente) o a la env var, fallando accionablemente si nada llega.

> **Variables adicionales que siguen este patrón**: `OTEL_EXPORTER_OTLP_ENDPOINT` (v2026-05-06). Primaria: `EnvVar("OTEL_EXPORTER_OTLP_ENDPOINT")` — convención OTel oficial. Fallback: `SystemProperty("otel.exporter.otlp.endpoint")` (testeable en runtime). Ver `AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT`.

### 3.2 Android

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | `BuildConfig.BUILD_ENVIRONMENT` (constante bakeada por Gradle) |
| Identificador alternativo | JVM system property `app.environment` o env var `APP_ENVIRONMENT` (tests instrumentados / forzar override) |
| Cómo se pasa por CLI | `./gradlew :platforms:mobile:androidApp:installDebug -PenableAndroid=true -Penv=STAGING` |
| Cómo se pasa por IDE | Run config versionado `.run/Android-<AMBIENTE>.run.xml` (tipo `GradleRunConfiguration`, `scriptParameters="-PenableAndroid=true -Penv=<AMBIENTE>"`, task `:platforms:mobile:androidApp:installAndStartDebug`). Una variante por ambiente: `Android-DEV`, `Android-DEV_LAN`, `Android-STAGING`, `Android-PROD`. |
| Si falta | `IllegalStateException` durante `MainActivity.onCreate()`. Sin fallback a `BuildConfig.DEBUG`. |

**Bridge BuildConfig → EnvironmentDetector** (patrón estándar):

`BuildConfig` vive en el módulo de la app (`androidApp`), no en `edugo-kmp-shared/config`. El detector compartido no puede importarlo. Por eso **`MainActivity.onCreate()` actúa como puente**: lee `BuildConfig.BUILD_ENVIRONMENT`, valida con `Environment.fromString(...)`, y llama `EnvironmentDetector.forceEnvironment(...)` antes de cualquier consumidor de `AppConfig`. Si el valor llega vacío o inválido, falla con `IllegalStateException` accionable. La system property / env var quedan como rutas alternas para tests instrumentados.

**Heurísticas eliminadas en Fase 1** ✅:
- `Debug.isDebuggerConnected()` en `EnvironmentDetector.android.kt`.
- Shortcut `BuildConfig.DEBUG → DEV` en `EduGoApplication`.
- Default silencioso `?: "PRODUCTION"` en `androidApp/build.gradle.kts` (ahora `?: ""`).

> **Variables adicionales que siguen este patrón**: `OTEL_EXPORTER_OTLP_ENDPOINT` (v2026-05-06). Primaria: `SystemProperty("otel.exporter.otlp.endpoint")` (testeable, mutable runtime); el override real de producción es `BuildConfig.OTEL_EXPORTER_OTLP_ENDPOINT` que se resuelve en el callsite (mismo patrón que `APP_ENVIRONMENT` con `BuildConfig.BUILD_ENVIRONMENT`). Fallback: `EnvVar("OTEL_EXPORTER_OTLP_ENDPOINT")`. Ver `AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT`.
>
> El default Gradle del módulo `androidApp` para `otelEndpoint` ahora es `""` (cambia desde `"http://10.0.2.2:4318"`); el `AndroidEndpointResolver` (callsite) aplica `"http://10.0.2.2:4318"` como default-plataforma cuando todo está vacío. Esto cumple "fuente de verdad en JSON, sin magic values en Gradle".

### 3.3 iOS

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | env var `APP_ENVIRONMENT` del scheme Xcode (resuelta por `NSProcessInfo`) |
| Identificador alternativo | `Info.plist["AppEnvironment"]` (resuelto en build-time por `Config.xcconfig`) |
| Cómo se pasa por CLI | `xcodebuild ... APP_ENVIRONMENT=STAGING` |
| Cómo se pasa por IDE | Scheme versionado `iosApp - <AMBIENTE>.xcscheme` bajo `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/`, con `APP_ENVIRONMENT=<AMBIENTE>` en `LaunchAction → EnvironmentVariables`. Uno por ambiente: `iosApp - DEV`, `iosApp - DEV_LAN`, `iosApp - STAGING`, `iosApp - PROD`. |
| Default en `Config.xcconfig` | `APP_ENVIRONMENT = DEV` (fallback seguro de desarrollo local — sólo aplica si CI/release no sobrescriben). |
| Si falta | `IllegalStateException` durante `MainViewController.bootstrap()` con mensaje accionable. |

**Bridge en iOS**: a diferencia de Android/Web, `MainViewController.bootstrap()` no actúa como puente — invoca `EnvironmentDetector.detect()` directamente. El detector ya cubre las dos fuentes (`NSProcessInfo` → `Info.plist`) y falla por sí mismo si ambas están ausentes.

**Heurísticas eliminadas en Fase 1** ✅:
- Default `"local"` en `MainViewController.readEnvironment()` (función eliminada; el detector centraliza la lectura).

**Schemes versionados (Fase 3)** ✅: cuatro archivos uniformes bajo `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/` (`iosApp - DEV.xcscheme`, `iosApp - DEV_LAN.xcscheme`, `iosApp - STAGING.xcscheme`, `iosApp - PROD.xcscheme`). Todos comparten `BlueprintIdentifier`, ejecutan `LaunchAction` con `buildConfiguration="Debug"` (la semántica del scheme es "apunta a las APIs de ese ambiente", no "cambia el modo de build"; release builds reales se generan vía Archive / `xcodebuild`).

> **Variables adicionales que siguen este patrón**: `OTEL_EXPORTER_OTLP_ENDPOINT` (v2026-05-06). Primaria: `ProcessEnv("OTEL_EXPORTER_OTLP_ENDPOINT")` — env var del scheme, igual que `APP_ENVIRONMENT`. Fallback: `PlistKey("OtelExporterOtlpEndpoint")` interpolada desde `Config.xcconfig`. Ver `AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT`.

### 3.4 Web (WasmJS)

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | constante de build `BUILD_ENVIRONMENT` (generada por Gradle desde `-Penv=`) |
| Identificador alternativo runtime | meta tag `<meta name="app-environment" content="STAGING">` inyectado en `index.html` durante `processResources` desde `-Penv=` |
| Identificador runtime de override | `window.__APP_ENVIRONMENT__` (útil en pruebas manuales, pre-bootstrap) |
| Cómo se pasa por CLI | `./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun -PenableWeb=true -Penv=STAGING` |
| Cómo se pasa por IDE | Run config versionado `.run/Web-<AMBIENTE>.run.xml` (tipo `GradleRunConfiguration`, `scriptParameters="-PenableWeb=true -Penv=<AMBIENTE>"`, task `:platforms:web:app:wasmJsBrowserDevelopmentRun`). Una variante por ambiente: `Web-DEV`, `Web-DEV_LAN`, `Web-STAGING`, `Web-PROD`. PROD usa también `wasmJsBrowserDevelopmentRun` (smoke local contra `config/prod.json`); el bundle de release se genera con `wasmJsBrowserDistribution`. |
| Si falta | `IllegalStateException` durante `Main.main()`. **Sin fallback a hostname.** |

**Bridge BUILD_ENVIRONMENT → EnvironmentDetector**: `Main.main()` lee la constante baked y llama `EnvironmentDetector.forceEnvironment(...)` (mismo patrón que Android). El detector compartido lee `window.__APP_ENVIRONMENT__` / meta tag sólo si el callsite no forzó ya un valor (útil cuando se sirve un bundle pre-compilado y se quiere overridear sin recompilar).

**Pre-carga del JSON real (Fase 2)**: `Main.main()` llama `MainScope().launch { ConfigPrefetcher.prefetch(env); startApp() }`. `ConfigPrefetcher` hace `fetch("config/{env}.json")` contra el bundle servido por webpack — los JSON del módulo compartido se copian al output de `wasmJsProcessResources` vía `installProcessedWebResources` (ver `platforms/web/app/build.gradle.kts`). Si el fetch falla, `ResourceLoader.wasmJs.kt` cae a `DefaultConfigs` como red de seguridad.

**Heurísticas eliminadas en Fase 1** ✅:
- `getHostname()` y el `when { localhost → DEV; staging → STAGING; else → PROD }` en `EnvironmentDetector.wasmJs.kt`.

> **Variables adicionales que siguen este patrón**: `OTEL_EXPORTER_OTLP_ENDPOINT` (v2026-05-06). Primaria: `WindowGlobal("__OTEL_EXPORTER_OTLP_ENDPOINT__")` — override pre-bootstrap, paralelo a `__APP_ENVIRONMENT__`. Fallback: `MetaTag("otel-exporter-otlp-endpoint")` paralelo a `app-environment`. Ver `AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT`.

---

## 4. Tabla resumen de homogeneización

| Capa | ¿Se homogeniza? | Justificación |
|---|:-:|---|
| Nombre que ve el dev/CI/IDE | ✅ Único: `APP_ENVIRONMENT` | Reduce carga cognitiva, simplifica documentación, facilita scripts portables |
| Flag Gradle uniforme | ✅ Único: `-Penv=...` | Mismo verbo para los 4 targets |
| Identificador interno (JVM prop, BuildConfig, Info.plist key, meta tag) | ❌ Cada plataforma usa su convención | Forzar `APP_ENVIRONMENT` literal en `.plist` (UpperCamelCase) o HTML (kebab-case) rompe linters y convenciones del ecosistema |
| Mecanismo de mapeo externo→interno | ✅ Documentado en este archivo | El dev no necesita saberlo; el detector se encarga |

---

## 5. Comportamiento ante ausencia de la variable

> **Ningún detector devuelve un default silencioso.** Si la variable no llega, lanza excepción.

Mensaje estándar (mismo en las 4 plataformas):

```
APP_ENVIRONMENT no definido. Define la variable así:
  - Desktop:  -Dapp.environment=DEV  o  export APP_ENVIRONMENT=DEV
  - Android:  Gradle property -Penv=DEV  (run config tipo GradleRunConfiguration)
  - iOS:      Xcode scheme → Environment Variables → APP_ENVIRONMENT=DEV
  - Web:      Gradle property -Penv=DEV  (run config tipo GradleRunConfiguration)
Valores válidos: DEV, DEV_LAN, STAGING, PROD
```

**Excepción única documentada**: iOS tiene un default `APP_ENVIRONMENT = DEV` en `Config.xcconfig` que aplica en builds locales. Su único propósito es evitar que alguien clone el repo y no pueda compilar; en CI/release el scheme **debe** definirlo explícitamente.

---

## 6. Mapeo `Environment` → archivo de configuración

Sin cambios respecto al estado actual:

| `Environment` | Archivo cargado | Ubicación |
|---|---|---|
| `DEV` | `dev.json` | [src/commonMain/resources/config/dev.json](src/commonMain/resources/config/dev.json) |
| `DEV_LAN` | `dev-lan.json` | [src/commonMain/resources/config/dev-lan.json](src/commonMain/resources/config/dev-lan.json) |
| `STAGING` | `staging.json` | [src/commonMain/resources/config/staging.json](src/commonMain/resources/config/staging.json) |
| `PROD` | `prod.json` | [src/commonMain/resources/config/prod.json](src/commonMain/resources/config/prod.json) |

Mapeo definido en [Environment.kt](src/commonMain/kotlin/com/edugo/kmp/config/Environment.kt) (`fileName`).

**WasmJS (Fase 2)**: `ConfigPrefetcher.prefetch(env)` hace `fetch` del JSON empaquetado por webpack al boot, antes de Compose / Koin. `ResourceLoader.wasmJs.kt` lo lee del cache; si la red falló, cae a `DefaultConfigs` como red de seguridad.

> **Subgrupo `telemetry`** (v2026-05-06): cada JSON ahora contiene `"telemetry": {"otelEndpoint": "..."}`. Valores por entorno: DEV `localhost:4318`, DEV_LAN `192.168.100.20:4318`, STAGING/PROD vacíos (override por env-var-nativa cuando exista Collector cloud). Ver `TelemetryConfig` y `AppEnvVar.OTEL_EXPORTER_OTLP_ENDPOINT`.

---

## 7. Reglas para agregar una nueva variable de entorno en el futuro

> **Ejemplo aplicado**: `OTEL_EXPORTER_OTLP_ENDPOINT` se incorporó al catálogo siguiendo esta receta (v2026-05-06). Ver `multi-platform-hardening-plan/02-decisiones.md::DA-MPH-2` y `phase-2-changes.md`.

Cuando el proyecto necesite una nueva variable (ej. `FEATURE_FLAGS_URL`, `LOG_LEVEL`), seguir esta receta:

1. Agregar la entrada al enum `AppEnvVar` en [src/commonMain/kotlin/com/edugo/kmp/config/AppEnvVar.kt](src/commonMain/kotlin/com/edugo/kmp/config/AppEnvVar.kt), declarando `canonicalName`, `primaryKeys` y `fallbackKeys` por `TargetPlatform` y un `validate` opcional.
2. Agregar el campo correspondiente a `AppConfig` (interfaz e impl) y a los 4 `*.json` + `DefaultConfigs.kt`.
3. Si la nueva variable necesita un detector dedicado, replicar el patrón de `EnvironmentDetector.<platform>.kt` (lectura de la primaria, fallback, fallo accionable).
4. Decidir el mecanismo nativo por plataforma siguiendo la misma tabla de §3:
   - Desktop: system prop / env var
   - Android: `BuildConfig.<NUEVA_VAR>` bakeada por `-P<flag>=`
   - iOS: env var del scheme + key en `Info.plist`
   - Web: constante `BUILD_<VAR>` o meta tag `<meta name="app-…">`

**Reuso del framework de tests** (alcance real):

- `EnvVarSource` (expect/actual) ya sabe `set/clear/setFallback/snapshot` contra cualquier `AppEnvVar` consultando `primaryKeys`/`fallbackKeys`, así que **no requiere cambios** al añadir una variable.
- `EnvVarMatrixExtensibilityTest` valida estructuralmente que la nueva entrada declare claves para las 4 plataformas y que su `validate` se comporte como se espera — basta con extenderlo (un par de aserciones extra).
- `EnvironmentDetectorContractTest` está hardcodeado a `APP_ENVIRONMENT` y a los 4 valores de `Environment` (es la única variable que mapea a un detector compartido). Para una variable distinta con su propio detector, aportar un test parametrizado equivalente (puede reutilizar `EnvVarMatrix` y `EnvVarSource`) en el source set apropiado.

En resumen: catálogo + tests de estructura son el camino "cero archivos nuevos"; cualquier variable con semántica de detección propia necesitará su test parametrizado dedicado, pero apoyándose en la misma infraestructura.

---

## 8. Decisiones cerradas en esta fase

| # | Decisión | Estado |
|---|---|:-:|
| D1 | Nombre canónico externo: `APP_ENVIRONMENT` | ✅ |
| D2 | Flag Gradle uniforme: `-Penv=` | ✅ |
| D3 | Sin defaults silenciosos: si falta la variable, falla con mensaje accionable | ✅ |
| D4 | Excepción única: `Config.xcconfig` tiene default `DEV` para builds locales de iOS | ✅ |
| D5 | Identificadores internos respetan la convención de cada plataforma | ✅ |
| D6 | Eliminar heurísticas (hostname web, `Debug.isDebuggerConnected()` android, `BuildConfig.DEBUG → DEV`, default silencioso `PRODUCTION`/`local`/`fromStringOrDefault`) | ✅ ejecutado en Fase 1 |
| D7 | El framework de tests parametriza por `Environment` y por `AppEnvVar` para que sea extensible a N variables sin reescribir tests | ✅ (a ejecutar en Fase 4) |
| D8 | El puerto `80701` en `DefaultConfigs.kt` es una **sonda intencional** del diagnóstico, no un bug. No se modifica como parte de este plan. | ✅ |
| D9 | `OTEL_EXPORTER_OTLP_ENDPOINT` se modela como variable del catálogo `AppEnvVar` con primaryKeys/fallbackKeys por plataforma. JSON con default por entorno (vacío para STAGING/PROD); cada plataforma resuelve inline en su entry point con orden uniforme (`<env-nativa-override> > <BuildConfig/sysprop-bake> > AppConfig.telemetry.otelEndpoint > default-plataforma`). | ✅ |

---

## 9. Próximos pasos (vista del plan completo)

| Fase | Entregable | Dependencia | Estado |
|---|---|---|---|
| **0** | `STANDARD.md` aprobado | — | ✅ |
| **1** | Detectores sin heurísticas; fallan explícitamente; bridge callsite documentado en Android/Web; `AppConfigImpl` rechaza `environmentName` inválido | D1, D3, D6 | ✅ |
| **2** | Pipeline `-Penv=` uniforme en las 4 plataformas: `generateBuildConfig` desktop + Main desktop como bridge + meta tag web inyectado por `installProcessedWebResources` + default `APP_ENVIRONMENT = DEV` en `Config.xcconfig` + `ConfigPrefetcher` WasmJS que carga el JSON real del bundle | D2, D4 | ✅ cerrada 2026-05-06 |
| **3** | Matriz de run configs IntelliJ por plataforma + ambiente (12 archivos `.run/<Plataforma>-<AMBIENTE>.run.xml`, todos `GradleRunConfiguration` con `-Penv=<AMBIENTE>`) + 4 schemes Xcode versionados (`iosApp - <AMBIENTE>.xcscheme`). Eliminadas las run configs legacy (`Android-App.run.xml` tipo nativo, `Android-App-DEV-LAN.run.xml`, `Desktop.run.xml` con env var, `Web-Wasm.run.xml` sin `-Penv=`) y los schemes con sufijos descriptivos (`(Local APIs)`, `(Physical Device)`, `(Azure APIs)`). | Fase 2 | ✅ cerrada 2026-05-06 |
| **4** | Framework de tests parametrizado (`AppEnvVar`, `EnvVarMatrix`, `EnvVarSource` expect/actual con mocks de sysprop JVM / `IosEnvSeam` / DOM Karma) + `EnvironmentDetectorContractTest` abstracto + subclases per-plataforma + tests de fallback (iOS Plist, Web meta tag) + test de extensibilidad multi-variable. Validado en las 4 plataformas (Desktop / Android host / iOS Simulator ARM64 / WasmJS Karma+Chrome). | Fase 1 | ✅ cerrada 2026-05-06 |
| **5** | `OTEL_EXPORTER_OTLP_ENDPOINT` extiende el catálogo. Subgrupo `telemetry` en `AppConfig`. 4 plataformas refactorizan a resolver inline con orden uniforme. JSONs DEV/DEV_LAN tienen endpoint hardcoded local; STAGING/PROD vacíos. | DA-MPH-2 (multi-platform-hardening-plan) | ✅ cerrada 2026-05-06 |

---

## 10. Cobertura de testing

Comandos para ejecutar el contract test parametrizado por plataforma (desde `EduUI/edugo-ui-kmp/`):

| Plataforma | Comando | Pre-requisito | Tests ejecutados |
|---|---|---|---|
| Desktop (JVM) | `./gradlew :edugo-kmp-shared:config:desktopTest` | JDK 21 | 5 contract + 4 extensibility |
| Android host | `./gradlew :edugo-kmp-shared:config:testAndroidHostTest -PenableAndroid=true` | Android SDK | 5 contract + 4 extensibility |
| iOS Simulator | `./gradlew :edugo-kmp-shared:config:iosSimulatorArm64Test -PenableIos=true` | Xcode 15+, simulador booted (`xcrun simctl boot "iPhone 16"`) | 5 contract + 2 fallback Plist + 4 extensibility |
| Web (WasmJS) | `./gradlew :edugo-kmp-shared:config:wasmJsBrowserTest -PenableWeb=true` | Chrome / Chromium | 5 contract + 2 fallback meta tag + 4 extensibility |
| Suite completa | `./gradlew :edugo-kmp-shared:config:allTests -PenableAndroid=true -PenableIos=true -PenableWeb=true` | todo lo anterior | todos |

**Limitaciones documentadas (no son bugs, son trade-offs explícitos del framework):**

1. **JVM env var no es mutable en runtime**. `System.getenv("APP_ENVIRONMENT")` es read-only sin reflexión a `ProcessEnvironment`, que es frágil entre JDKs y bypassea el contrato. El framework cubre la ruta primaria (`System.setProperty("app.environment", ...)`); la ruta de env var fallback se valida por integración manual: `APP_ENVIRONMENT=PROD ./gradlew :edugo-kmp-shared:config:desktopTest --tests "*EnvironmentDetectorTest*"` (los tests legacy `EnvironmentDetectorTest`/`ConfigLoaderIntegrationTest` ya cubren `forceEnvironment` y la ruta de bridging — el env var real se prueba por inspección manual cuando se hace un cambio relevante).

2. **iOS sólo se valida en simulador, no en device real**. `NSProcessInfo.processInfo.environment` se comporta idéntico en simulator y device, así que la cobertura es equivalente. Los tests usan `IosEnvSeam` para inyectar fakes en lugar de mutar `NSBundle`/`NSProcessInfo` (que son read-only en runtime — no hay API pública para mutarlos desde el proceso).

3. **WasmJS requiere DOM real**. Los tests corren bajo Karma + Chrome headless (`browser { testTask { enabled = true } }` en `kmp.android.gradle.kts`). Cambiar a `nodejs()` rompería `EnvVarSource` por falta de `document` y `window` — por eso el plugin lo deja explícitamente desactivado.

4. **El detector Android compartido (`EnvironmentDetector.android.kt`) sólo lee `System.getProperty("app.environment")` + env var**. `BuildConfig.BUILD_ENVIRONMENT` vive en el módulo `androidApp`; el bridge se hace en `MainActivity.onCreate()` con `EnvironmentDetector.forceEnvironment(...)`. Por eso los tests de `:config` no necesitan mockear `BuildConfig` y comparten `actual EnvVarSource` con Desktop vía source set intermedio `jvmCommonTest`.

**Estructura de source sets de tests:**

```
config/src/
  commonMain/kotlin/.../AppEnvVar.kt                       ← catálogo (internal)
  commonTest/kotlin/.../EnvVarSource.kt                    ← expect class
  commonTest/kotlin/.../EnvVarMatrix.kt                    ← DSL declarativo
  commonTest/kotlin/.../EnvironmentDetectorContractTest.kt ← contract abstracto
  commonTest/kotlin/.../EnvVarMatrixExtensibilityTest.kt   ← multi-variable smoke
  jvmCommonTest/kotlin/.../EnvVarSource.jvm.kt             ← actual JVM (Desktop+Android)
  desktopTest/kotlin/.../DesktopEnvironmentDetectorContractTest.kt
  androidHostTest/kotlin/.../AndroidEnvironmentDetectorContractTest.kt
  iosMain/kotlin/.../IosEnvSeam.kt                         ← seam internal-only
  iosTest/kotlin/.../EnvVarSource.ios.kt                   ← actual iOS
  iosTest/kotlin/.../IosEnvironmentDetectorContractTest.kt
  iosTest/kotlin/.../IosFallbackPlistTest.kt
  wasmJsTest/kotlin/.../EnvVarSource.wasmJs.kt             ← actual WasmJS
  wasmJsTest/kotlin/.../WasmJsEnvironmentDetectorContractTest.kt
  wasmJsTest/kotlin/.../WasmJsFallbackMetaTest.kt
```
