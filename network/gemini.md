# Guía del Módulo Network para Gemini

Este documento establece los estándares de comunicación y manejo de errores de red en el proyecto
EduGo KMP.

## Uso del Cliente HTTP

### Métodos Preferidos

- **Uso Obligatorio de Métodos Safe**: Siempre que realices una petición desde un Repositorio o
  Servicio, utiliza las variantes que terminan en `Safe` (ej: `getSafe`, `postSafe`). Estas capturan
  automáticamente las excepciones y retornan un `Result<T>`.
- **Inmutabilidad**: Utiliza `HttpRequestConfig` para pasar parámetros dinámicos a una petición sin
  alterar la configuración global del cliente.

### Ejemplo de Implementación en Repositorio

```kotlin
suspend fun fetchItems(): Result<List<Item>> {
    return client.getSafe("/api/v1/items")
}
```

## Manejo de Errores y Excepciones

### Jerarquía `NetworkException`

El módulo traduce los fallos de Ktor a una jerarquía tipada. Nunca lances excepciones genéricas;
utiliza o captura:

- `NetworkException.Timeout`: Problemas de tiempo de espera.
- `NetworkException.NoConnection`: El dispositivo no tiene acceso a internet.
- `NetworkException.ServerError`: Error 5xx del servidor.

### Mapeo Automático

Utiliza `ExceptionMapper.map(throwable)` para convertir cualquier error de bajo nivel en un
`AppError` estructurado del módulo `foundation`.

## Seguridad y Logs

### Sanitización

- El `NetworkLogger` y `LogSanitizer` ocultan automáticamente campos como `password`, `token`,
  `secret` y headers de `Authorization`.
- **Regla Estricta**: Si necesitas loguear manualmente una petición, asegúrate de pasar los datos
  por `LogSanitizer.sanitizeBody()` o `sanitizeUrl()`. **Nunca** imprimas cuerpos de petición en
  crudo si contienen credenciales.

## Monitoreo de Conectividad

### Uso de `NetworkObserver`

- Utiliza la propiedad `isOnline` para validaciones rápidas antes de disparar peticiones costosas.
- Suscríbete al flujo `status` para actualizar estados de la UI (ej: mostrar un banner de "Sin
  conexión").

## Configuración de Interceptores

Al añadir interceptores a la cadena (`InterceptorChain`), respeta la propiedad `order`:

- `order = 10`: Modificaciones de headers.
- `order = 20`: Autenticación y tokens.
- `order = 99`: Telemetría y métricas.
- `order = 100`: Logging (debe ser el último en peticiones y el primero en respuestas).
