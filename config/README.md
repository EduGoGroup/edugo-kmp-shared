# Módulo Config

El módulo `config` es el responsable de gestionar la configuración dinámica de la aplicación y la
detección del entorno de ejecución (DEV, STAGING, PROD). Proporciona una interfaz unificada para
acceder a parámetros críticos como URLs de APIs, tiempos de espera y modos de depuración.

## Características Principales

### 1. Gestión de Ambientes

Define los entornos de ejecución soportados mediante el enum `Environment`:

- **DEV**: Desarrollo local (localhost, debug habilitado).
- **STAGING**: Pruebas pre-producción.
- **PROD**: Producción (configuración optimizada y segura).

### 2. Detección Automática (`EnvironmentDetector`)

Identifica el ambiente de forma inteligente según la plataforma:

- **Android**: Verifica el estado del debugger o propiedades del sistema.
- **Desktop/JVM**: Verifica variables de entorno (`APP_ENVIRONMENT`) o propiedades JVM.
- **iOS**: Utiliza flags de Xcode o valores en `Info.plist`.
- **WasmJS**: Detecta el ambiente basándose en el `hostname` del navegador.

### 3. Carga de Configuración (`ConfigLoader`)

Carga archivos JSON ubicados en `resources/config/` de forma específica para cada plataforma:

- Soporte para **Assets** en Android.
- Soporte para **Bundle Resources** en iOS.
- Soporte para **Classpath** en Desktop.
- **Fallback Seguro**: Si los archivos no están presentes, utiliza `DefaultConfigs` para garantizar
  que la app inicie.

## Cómo Usar

### Inicialización en Android

Es obligatorio inicializar el contexto antes de cargar la configuración:

```kotlin
AndroidContextHolder.init(applicationContext)
```

### Carga de Configuración

```kotlin
val environment = EnvironmentDetector.detect()
val config = ConfigLoader.load(environment)

println(config.academicApiBaseUrl)
```

## Dependencias

- `:modules:foundation`: Tipos base.
- `:modules:core`: Abstracciones de plataforma.
