# Guía del Módulo Logger para Gemini

Este documento establece las convenciones y reglas para el uso del sistema de logs en el proyecto
EduGo KMP.

## Convenciones de Etiquetas (Tags)

### Estructura Jerárquica

Utiliza siempre la notación de puntos para definir la jerarquía. El formato recomendado es:
`Proyecto.Modulo.Componente` (ej: `EduGo.Auth.TokenManager`).

### Nombramiento de Clases

En la mayoría de los casos, utiliza `fromClass(this::class)` para generar automáticamente el tag
basado en el nombre de la clase. Esto garantiza consistencia y facilita el seguimiento en el Logcat.

## Reglas de Implementación

### 1. Niveles de Log

- **DEBUG (d)**: Información detallada para desarrollo. **Nunca** debe activarse en producción.
- **INFO (i)**: Hitos importantes del ciclo de vida (login exitoso, cambio de pantalla).
- **WARNING (w)**: Situaciones anómalas pero recuperables (fallos de red con reintento, caché
  expirada).
- **ERROR (e)**: Fallos críticos o excepciones no controladas.

### 2. Rendimiento y Seguridad

- **Mensajes Complejos**: Usa siempre la variante de bloque `{ }` para mensajes que requieran
  concatenación de strings o cálculos. Esto evita que el código se ejecute si el nivel de log está
  desactivado.
- **Datos Sensibles**: **Regla Prohibida**: Nunca loguees contraseñas, tokens completos (usa
  `toLogString()`), ni información personal identificable (PII) sin enmascarar.

## Gestión de Configuración (`LoggerConfig`)

### Filtrado por Entorno

El sistema permite silenciar módulos específicos en tiempo de ejecución.

```kotlin
// Desactivar logs de red pesados
LoggerConfig.setLevel("EduGo.Network.*", LogLevel.INFO)
```

## Integración con Arquitectura

Al trabajar con flujos funcionales, utiliza las extensiones de `Result` para mantener el código
limpio:

```kotlin
repository.fetchData()
    .logOnFailure("MyTag", "Error al recuperar datos")
    .map { process(it) }
```

## Testing

Para verificar que el código emite los logs correctos en las pruebas unitarias, utiliza
`MockLogger`. Este componente captura todas las llamadas y permite realizar aserciones sobre ellas (
`assertEquals(1, mockLogger.errorCount)`).
