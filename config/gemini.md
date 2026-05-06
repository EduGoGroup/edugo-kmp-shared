# Guía del Módulo Config para Gemini

Este documento contiene las reglas técnicas y patrones para la gestión de configuraciones y
detección de ambientes en el ecosistema EduGo KMP.

## Modificación de AppConfig

Si necesitas añadir una nueva propiedad de configuración a la aplicación, debes seguir estos pasos:

1. **Interfaz `AppConfig`**: Añade la propiedad en la interface `AppConfig`.
2. **Implementación `AppConfigImpl`**: Añade la propiedad al constructor y aplica la lógica
   necesaria (ej: si debe ser false en PROD).
3. **Archivos JSON**: Actualiza `dev.json`, `staging.json` y `prod.json` en
   `commonMain/resources/config/`.
4. **Fallback `DefaultConfigs`**: Es **CRÍTICO** actualizar `DefaultConfigs.kt` con la misma
   propiedad para asegurar que las pruebas unitarias y entornos sin recursos no fallen.

## Detección de Ambientes

> **Fuente única de verdad**: `STANDARD.md` (en este mismo módulo) define el contrato
> completo de la variable `APP_ENVIRONMENT` y su mecanismo nativo por plataforma.
> Lo que sigue es un resumen.

### Principio

Cada plataforma extrae el valor de su mecanismo nativo (system property JVM,
`BuildConfig`, `NSProcessInfo`/`Info.plist`, `window.__APP_ENVIRONMENT__` /
meta tag) y lo mapea a un `Environment`. **No hay heurísticas ni defaults
silenciosos**: si la variable falta, el detector lanza `IllegalStateException`
con un mensaje que indica cómo definirla por plataforma.

### Lógica por Plataforma (resumen)

- **Desktop (JVM)**: system property `app.environment` → env var `APP_ENVIRONMENT`.
- **Android**: system property `app.environment` (típicamente populada desde
  `BuildConfig.BUILD_ENVIRONMENT` por el host app antes de inicializar Koin).
- **iOS**: env var `APP_ENVIRONMENT` (`NSProcessInfo`) → `Info.plist["AppEnvironment"]`.
- **WasmJS**: `window.__APP_ENVIRONMENT__` → `<meta name="app-environment">`.

### Forzado de Ambiente

Para propósitos de testing o depuración específica, puedes usar:

```kotlin
EnvironmentDetector.forceEnvironment(Environment.STAGING)
```

Recuerda siempre llamar a `EnvironmentDetector.reset()` en el `AfterTest` de tus suites de prueba.

## Reglas de Seguridad (PROD)

- **mockMode**: La propiedad `mockMode` en `AppConfigImpl` está forzada a `false` si el ambiente es
  `Environment.PROD`, independientemente de lo que diga el archivo JSON. Nunca intentes bypassear
  esta seguridad.
- **debugMode**: Debe ser `false` en PROD para desactivar logs sensibles y herramientas de
  desarrollador.

## Carga de Recursos

- El sistema utiliza `loadResourceAsString` (expect/actual) para leer los JSONs.
- Si el archivo no se encuentra físicamente en la plataforma, el sistema **siempre** cae de nuevo (
  fallback) a `DefaultConfigs`.
