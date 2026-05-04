# Guía del Módulo Validation para Gemini

Este documento define los estándares para la implementación de validaciones de datos en el proyecto EduGo KMP.

## Reglas de Implementación de Validadores

### 1. Firma de Funciones
Cualquier función de validación atómica debe seguir este estándar:
- **Retorno**: Debe retornar `String?` (null si es válido, el mensaje de error si no).
- **Parámetros**: El primer parámetro debe ser el valor a validar, seguido por `fieldName` y opcionalmente un `customMessage`.

### 2. Mensajes de Error
- Los mensajes deben ser descriptivos e incluir el nombre del campo (`fieldName`) para dar contexto al usuario.
- **Formato**: `"$fieldName debe tener al menos $minLength caracteres"`.
- Los errores acumulados se concatenan por defecto con `"; "`.

## Uso del Acumulador (`ValidationErrorAccumulator`)

### Validación Condicional
Utiliza `validateIf` para disparar validaciones que dependen del estado del objeto:
```kotlin
add(validateIf(producto.enOferta) {
    validateRange(producto.descuento, 0, 100, "Descuento")
})
```

### Lógica Booleana
- **AND Lógico**: Usa `validateAll` para asegurar que un grupo de reglas pase antes de continuar.
- **OR Lógico**: Usa `validateAtLeastOne` cuando existan campos alternativos (ej: el usuario debe proveer Email O Teléfono).

## Integración con Otros Módulos

### Modelos y DTOs
- Es **obligatorio** usar validación acumulativa en cualquier DTO que se reciba desde un formulario de UI.
- Los modelos que implementen `ValidatableModel` (del módulo `foundation`) deben utilizar los helpers de este módulo en su método `validate()`.

### Mapeadores (`DomainMapper`)
Utiliza la extensión `validateWith` para validar un DTO antes de convertirlo al modelo de dominio:
```kotlin
override fun toDomain(dto: UserDto): Result<User> = dto.validateWith {
    add(validateEmail(it.email))
    // ... más validaciones
}.map { User(dto.email, ...) }
```

## Testing de Validaciones
Al testear validaciones:
- Verifica tanto el caso de éxito (null) como los casos de falla esperados.
- En validaciones acumulativas, comprueba que se están capturando todos los errores simultáneamente utilizando `errorCount()` o verificando que el mensaje final contiene todos los fragmentos esperados.
