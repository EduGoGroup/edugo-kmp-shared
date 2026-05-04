# Guía del Módulo Foundation para Gemini

Este documento establece las reglas técnicas para el uso de los tipos base y patrones funcionales en
EduGo KMP.

## Manejo de Resultados (`Result`)

### Reglas de Uso

- **Nunca** uses `try-catch` para lógica de negocio; utiliza la función `catching { }` para envolver
  operaciones peligrosas.
- **Fail-Fast**: En validaciones, retorna el primer error encontrado.
- **Async/Await**: No mezcles el estado `Loading` con funciones `suspend` a menos que sea
  estrictamente necesario para la UI. Prefiere que la función `suspend` retorne `Success` o
  `Failure`.

### Patrones Recomendados

```kotlin
// Combinación de múltiples resultados
zip3(val1, val2, val3) { a, b, c -> MyModel(a, b, c) }

// Transformación segura
val user = jsonString.fromJson<UserDto>()
    .flatMap { dto -> UserMapper.toDomain(dto) }
    .recover { "Guest" }
```

## Sistema de Errores

### Clasificación

Usa siempre `ErrorCode` para clasificar errores. Si necesitas añadir uno nuevo, elige el rango
adecuado:

- `1000-1999`: Errores de Red.
- `2000-2999`: Errores de Autenticación.
- `3000-3999`: Errores de Validación.
- `4000-4999`: Errores de Negocio.

### Reporte de Errores

Para errores complejos, construye un `AppError`:

```kotlin
AppError.fromException(
    exception = e,
    code = ErrorCode.BUSINESS_RESOURCE_NOT_FOUND,
    details = mapOf("id" to userId)
)
```

## Modelado de Datos

### Composición de Entidades

Al crear modelos de dominio, hereda de las interfaces necesarias para activar comportamientos
automáticos en otros módulos:

- `EntityBase`: Obligatorio para persistencia en base de datos.
- `ValidatableModel`: Úsalo para que el `DynamicScreenViewModel` valide el formulario
  automáticamente antes de enviarlo.
- `SoftDeletable`: Úsalo si el registro debe ser recuperable tras su eliminación.

## Serialización

### Configuración JSON

- Usa `JsonConfig.Default` para la mayoría de los casos.
- Usa `JsonConfig.Pretty` **solo** en logs o depuración.
- **Importante**: Cuando trabajes con fechas, asegúrate de anotar el campo con
  `@Serializable(with = InstantSerializer::class)` o usa
  `@file:UseSerializers(InstantSerializer::class)`.

## Mapeo de Datos

Implementa siempre `DomainMapper` para separar la estructura de la API (DTO) del modelo interno (
Domain). La validación **debe** ocurrir en el método `toDomain`.
