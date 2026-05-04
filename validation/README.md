# Módulo Validation

El módulo `validation` proporciona un motor de validación potente, reutilizable y multiplataforma para el ecosistema EduGo KMP. Ofrece un conjunto exhaustivo de utilidades para garantizar la integridad de los datos, integrándose estrechamente con el sistema de resultados del módulo `foundation`.

## Estrategias de Validación

El módulo soporta dos enfoques principales según la necesidad:

### 1. Validación Fail-Fast
Se detiene en el primer error encontrado. Es ideal para validaciones de lógica de negocio o procesos internos donde no tiene sentido continuar si falla un paso previo. Se implementa típicamente mediante encadenamiento de `flatMap`.

### 2. Validación Acumulativa (`AccumulativeValidation`)
Recolecta todos los errores de validación antes de fallar. Es la estrategia recomendada para **formularios de UI**, ya que permite mostrar al usuario todos los campos incorrectos de una sola vez, mejorando significativamente la experiencia de usuario.

## Características Principales

### 1. Helpers de Validación
Funciones atómicas para validar:
- **Strings**: No blanco (`validateNotBlank`), emails, longitud mínima/máxima, rangos de longitud y patrones Regex.
- **Números**: Rangos (`Int`, `Long`, `Double`), valores positivos y no negativos.
- **Colecciones**: No vacía, tamaño mínimo/máximo y pertenencia a un conjunto (`validateIn`).
- **Formatos Especiales**: Soporte nativo para validación de UUID v4.

### 2. DSL de Acumulación
Provee una sintaxis fluida para agrupar múltiples validaciones:
```kotlin
val result = accumulateValidationErrors {
    add(validateEmail(email))
    add(validateRange(age, 18, 120, "Edad"))
    add(validateNotBlank(name, "Nombre"))
}
```

### 3. Extensiones Fluídas
Sintaxis tipo "sugar" para verificaciones rápidas:
- `"user@mail.com".isValidEmail()`
- `id.isValidUUID()`
- `password.matchesPassword(confirmation)`

## Cómo Usar

### En Modelos Validables
```kotlin
data class User(val email: String, val age: Int) : ValidatableModel {
    override fun validate(): Result<Unit> = accumulateValidationErrors {
        add(validateEmail(email))
        add(validateRange(age, 18, 120, "Edad"))
    }
}
```

## Dependencias
- `:modules:foundation`: Provee el tipo `Result<T>` y la interfaz `ValidatableModel`.
