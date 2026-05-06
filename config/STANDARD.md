# Estándar de detección de entorno — `edugo-kmp-shared/config`

> Documento contractual. Define cómo cada plataforma del proyecto EduGo UI KMP recibe, valida y consume la variable de entorno que determina qué archivo `config-{env}.json` se carga en el arranque.
>
> **Estado**: Fase 1 cerrada — heurísticas eliminadas; cada plataforma falla con mensaje accionable si la variable no llega. Fase 2 pendiente (pipeline único de inyección).

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
| Identificador interno preferido | JVM system property `app.environment` |
| Identificador alternativo | env var del SO `APP_ENVIRONMENT` |
| Cómo se pasa por CLI | `./gradlew :platforms:desktop:app:run -Penv=STAGING` (**pendiente Fase 2**) o `-Dapp.environment=STAGING` |
| Cómo se pasa por IDE | Run Config IntelliJ → `Environment variables` → `APP_ENVIRONMENT=STAGING` |
| Si falta | `IllegalStateException` con el mensaje accionable de §5 (sysprop o env var). |

**Pendiente Fase 2**:
- Agregar `generateBuildConfig` al módulo `platforms/desktop/app` para que `-Penv=` se traduzca a `BUILD_ENVIRONMENT` bakeado y `Main.kt` haga el bridge a `EnvironmentDetector.forceEnvironment(...)` (mismo patrón que Android/Web).
- Cablear el `application` block de Compose Desktop con la system property `-Dapp.environment=$env` para que `./gradlew run -Penv=...` funcione fuera de IntelliJ sin pasos manuales.

Hoy (Fase 1): el detector desktop lee sysprop / env var y falla limpio si nada llega. El cableo end-to-end es trabajo de Fase 2.

### 3.2 Android

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | `BuildConfig.BUILD_ENVIRONMENT` (constante bakeada por Gradle) |
| Identificador alternativo | JVM system property `app.environment` o env var `APP_ENVIRONMENT` (tests instrumentados / forzar override) |
| Cómo se pasa por CLI | `./gradlew :platforms:mobile:androidApp:installDebug -PenableAndroid=true -Penv=STAGING` |
| Cómo se pasa por IDE | Run Config tipo **`GradleRunConfiguration`** con `scriptParameters="-PenableAndroid=true -Penv=STAGING"` (Fase 3 unifica todos los `.run/Android-*.run.xml`) |
| Si falta | `IllegalStateException` durante `MainActivity.onCreate()`. Sin fallback a `BuildConfig.DEBUG`. |

**Bridge BuildConfig → EnvironmentDetector** (patrón estándar):

`BuildConfig` vive en el módulo de la app (`androidApp`), no en `edugo-kmp-shared/config`. El detector compartido no puede importarlo. Por eso **`MainActivity.onCreate()` actúa como puente**: lee `BuildConfig.BUILD_ENVIRONMENT`, valida con `Environment.fromString(...)`, y llama `EnvironmentDetector.forceEnvironment(...)` antes de cualquier consumidor de `AppConfig`. Si el valor llega vacío o inválido, falla con `IllegalStateException` accionable. La system property / env var quedan como rutas alternas para tests instrumentados.

**Heurísticas eliminadas en Fase 1** ✅:
- `Debug.isDebuggerConnected()` en `EnvironmentDetector.android.kt`.
- Shortcut `BuildConfig.DEBUG → DEV` en `EduGoApplication`.
- Default silencioso `?: "PRODUCTION"` en `androidApp/build.gradle.kts` (ahora `?: ""`).

### 3.3 iOS

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | env var `APP_ENVIRONMENT` del scheme Xcode (resuelta por `NSProcessInfo`) |
| Identificador alternativo | `Info.plist["AppEnvironment"]` (resuelto en build-time por `Config.xcconfig`) |
| Cómo se pasa por CLI | `xcodebuild ... APP_ENVIRONMENT=STAGING` |
| Cómo se pasa por IDE | Xcode → Edit Scheme → Run → Arguments → Environment Variables → `APP_ENVIRONMENT=STAGING` |
| Default en `Config.xcconfig` | `APP_ENVIRONMENT = DEV` (**pendiente Fase 2** — fallback seguro de desarrollo local) |
| Si falta | `IllegalStateException` durante `MainViewController.bootstrap()` con mensaje accionable. |

**Bridge en iOS**: a diferencia de Android/Web, `MainViewController.bootstrap()` no actúa como puente — invoca `EnvironmentDetector.detect()` directamente. El detector ya cubre las dos fuentes (`NSProcessInfo` → `Info.plist`) y falla por sí mismo si ambas están ausentes.

**Heurísticas eliminadas en Fase 1** ✅:
- Default `"local"` en `MainViewController.readEnvironment()` (función eliminada; el detector centraliza la lectura).

**Schemes a versionar en Fase 3**: uno por ambiente bajo `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/`.

### 3.4 Web (WasmJS)

| Aspecto | Valor |
|---|---|
| Identificador interno preferido | constante de build `BUILD_ENVIRONMENT` (generada por Gradle desde `-Penv=`) |
| Identificador alternativo runtime | meta tag `<meta name="app-environment" content="STAGING">` inyectado en `index.html` durante el build (**pendiente Fase 2**) |
| Identificador runtime de override | `window.__APP_ENVIRONMENT__` (útil en pruebas manuales, pre-bootstrap) |
| Cómo se pasa por CLI | `./gradlew :platforms:web:app:wasmJsBrowserDevelopmentRun -PenableWeb=true -Penv=STAGING` |
| Cómo se pasa por IDE | Run Config tipo **`GradleRunConfiguration`** con `scriptParameters="-PenableWeb=true -Penv=STAGING"` |
| Si falta | `IllegalStateException` durante `Main.main()`. **Sin fallback a hostname.** |

**Bridge BUILD_ENVIRONMENT → EnvironmentDetector**: `Main.main()` lee la constante baked y llama `EnvironmentDetector.forceEnvironment(...)` (mismo patrón que Android). El detector compartido lee `window.__APP_ENVIRONMENT__` / meta tag sólo si el callsite no forzó ya un valor (path runtime que se completa con la inyección del meta tag en Fase 2).

**Heurísticas eliminadas en Fase 1** ✅:
- `getHostname()` y el `when { localhost → DEV; staging → STAGING; else → PROD }` en `EnvironmentDetector.wasmJs.kt`.

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

**Nota Fase 2**: el `ResourceLoader` de WasmJS hoy delega siempre a `DefaultConfigs`. Se cambiará para que cargue el JSON real empaquetado en el bundle web; mientras tanto `DefaultConfigs` se mantiene como red de seguridad.

---

## 7. Reglas para agregar una nueva variable de entorno en el futuro

Cuando el proyecto necesite una segunda variable (ej. `FEATURE_FLAGS_URL`, `OTEL_ENDPOINT`), seguir esta receta:

1. Agregar el nombre canónico al enum `AppEnvVar` en `src/commonTest/.../EnvVarsContract.kt` (Fase 4).
2. Agregar el campo correspondiente a `AppConfig` (interfaz e impl) y a los 4 `*.json` + `DefaultConfigs.kt`.
3. Agregar la expectativa por ambiente a `EnvVarMatrix.expectations` (Fase 4).
4. Decidir el mecanismo nativo por plataforma siguiendo la misma tabla de §3:
   - Desktop: system prop / env var
   - Android: `BuildConfig.<NUEVA_VAR>` bakeada por `-P<flag>=`
   - iOS: env var del scheme + key en `Info.plist`
   - Web: constante `BUILD_<VAR>` o meta tag `<meta name="app-…">`

**Cero archivos de test nuevos**: el framework parametrizado los itera todos.

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

---

## 9. Próximos pasos (vista del plan completo)

| Fase | Entregable | Dependencia | Estado |
|---|---|---|---|
| **0** | `STANDARD.md` aprobado | — | ✅ |
| **1** | Detectores sin heurísticas; fallan explícitamente; bridge callsite documentado en Android/Web; `AppConfigImpl` rechaza `environmentName` inválido | D1, D3, D6 | ✅ |
| 2 | Pipeline `-Penv=` uniforme en las 4 plataformas + `generateBuildConfig` desktop + Main desktop como bridge + meta tag web inyectado + default `APP_ENVIRONMENT = DEV` en `Config.xcconfig` + WasmJS ResourceLoader real | D2, D4 | pendiente |
| 3 | Run configs por ambiente (`.run/<Plataforma>-<AMBIENTE>.run.xml`) + schemes Xcode versionados | Fase 2 | pendiente |
| 4 | Framework de tests parametrizado (`AppEnvVar`, `EnvVarMatrix`, contract tests + tests por plataforma con mocks de sysprop / `NSBundle` / DOM) | Fase 1 | pendiente |
