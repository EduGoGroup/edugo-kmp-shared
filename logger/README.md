# Módulo Logger

El módulo `logger` proporciona una abstracción unificada y potente para el registro de eventos (
logging) en el ecosistema EduGo KMP. Utiliza **Kermit** como motor subyacente, garantizando que los
logs se redirijan a los mecanismos nativos de cada plataforma (Logcat en Android, NSLog en iOS,
Console en JS/Wasm).

## Características Principales

### 1. Abstracción Multiplataforma

Interfaz `Logger` única que delega en implementaciones nativas:

- **Android**: Integración con `android.util.Log`.
- **iOS**: Integración con `NSLog` (visible en Xcode).
- **JVM/Desktop**: Salida por consola con soporte de colores ANSI.
- **WasmJS**: Integración con `console.log` del navegador.

### 2. Logging Jerárquico (`TaggedLogger`)

Soporta etiquetas anidadas mediante notación de puntos (ej: `EduGo.Auth.Login`), permitiendo
organizar la salida y aplicar filtros granulares por módulos o funcionalidades.

### 3. Evaluación Lazy

Variantes de métodos que aceptan lambdas `() -> String`, asegurando que la construcción de mensajes
complejos solo ocurra si el nivel de log está habilitado, optimizando el rendimiento en producción.

### 4. Filtrado Dinámico (`LoggerConfig`)

Sistema de configuración en tiempo de ejecución para controlar la verbosidad:

- **Patrones con Wildcards**: `EduGo.Network.*` para afectar a todo un submódulo.
- **Soporte Regex**: Filtrado avanzado mediante expresiones regulares.
- **Niveles Estándar**: DEBUG, INFO, WARNING, ERROR.

### 5. Extensiones de Integración

Utilidades para loguear directamente estados de la arquitectura:

- **Result Extensions**: `logOnFailure` y `logOnSuccess` para monitorear flujos funcionales.
- **AppError Extensions**: Mapeo automático de errores estructurados a niveles de log adecuados (
  WARNING para reintentables, ERROR para críticos).

## Cómo Usar

### Obtener un Logger

```kotlin
// Usar el logger global predefinido
DefaultLogger.d("MiTag", "Mensaje de depuración")

// Crear un logger específico para una clase (recomendado)
val logger = createDefaultLogger().withTag("MiModulo.MiClase")
logger.i("Servicio inicializado")
```

### Logging con Lazy Evaluation

```kotlin
logger.d { "Resultado de operación costosa: ${calcularDatos()}" }
```

## Dependencias

- `co.touchlab:kermit`: Backend de logging multiplataforma.
- `:modules:core`: Abstracciones de sincronización.
