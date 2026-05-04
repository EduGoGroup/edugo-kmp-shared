# Guía del Módulo Telemetry para Gemini

Este documento define los estándares y mejores prácticas para el registro de telemetría y
diagnósticos en EduGo KMP.

## Registro de Nuevas Métricas y Eventos

Al añadir telemetría a una nueva funcionalidad, sigue este flujo de trabajo obligatorio:

1. **Definir Constantes**: Añade el nombre técnico en `MetricNames.kt` utilizando `snake_case` y
   sufijos de unidad (ej: `_ms`, `_total`).
2. **Crear Helper**: Crea una función de extensión en el paquete `helpers`. **Regla Estricta**: No
   llames directamente a los grabadores desde la lógica de negocio; utiliza siempre los helpers para
   asegurar la consistencia de los labels y propiedades.

## Privacidad y Seguridad

### Enmascaramiento de Datos (PII)

- **Nunca** incluyas correos electrónicos, nombres reales, números de teléfono o contraseñas en las
  propiedades de los eventos o en los labels de las métricas.
- Al registrar fallos con `recordException`, asegúrate de que el mapa de contexto solo contenga
  metadatos técnicos (ej: `screen_key`, `api_version`).

### Identificación de Usuario

- Utiliza `setUserId(userId)` únicamente tras un login exitoso.
- Es **obligatorio** llamar a `setUserId(null)` durante el flujo de logout para desvincular el
  dispositivo del usuario anterior.

## Gestión de la Cardinalidad (Métricas)

La cardinalidad es el número de combinaciones únicas de etiquetas (labels). Una cardinalidad alta
puede degradar el sistema de métricas.

### Sanitización de Paths

Al registrar métricas de red, utiliza **siempre** la función `sanitizePath(path)`. Esta función
reemplaza automáticamente UUIDs e IDs numéricos por el marcador `:id`.

```kotlin
// Correcto
val sanitized = sanitizePath("/api/v1/schools/123-abc/units") // "/api/v1/schools/:id/units"
```

## Integración Automática

El ecosistema EduGo KMP ya integra telemetría en los siguientes puntos:

- **Red**: Todas las peticiones HTTP son grabadas por el `TelemetryInterceptor` (latencia, status,
  errores).
- **SDUI**: El `DynamicScreenViewModel` registra errores de carga y renderizado.
- **Identidad**: El `AuthService` registra éxitos y fallos de autenticación.

Antes de añadir telemetría manual, verifica si el componente base ya lo está haciendo para evitar
duplicidad de datos.
