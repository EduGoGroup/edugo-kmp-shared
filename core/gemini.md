# Guía del Módulo Core para Gemini

Este documento proporciona el contexto técnico necesario para trabajar con el módulo `core`, el cual
contiene la infraestructura crítica y las abstracciones de plataforma del sistema EduGo KMP.

## Abstracciones de Plataforma

### 1. Coroutines Dispatchers

Utiliza siempre `AppDispatchers` en lugar de los nativos de Kotlin para asegurar la portabilidad
multiplataforma:

- `AppDispatchers.Main`: UI y operaciones ligeras.
- `AppDispatchers.IO`: Red, base de datos y operaciones bloqueantes (I/O).
- `AppDispatchers.Default`: Computaciones pesadas (Parsing JSON, procesamiento de imágenes).

### 2. Sincronización y Concurrencia

- Usa `@PlatformVolatile` para garantizar la visibilidad de memoria en campos compartidos entre
  hilos.
- Usa `platformSynchronized(lock, block)` para secciones críticas seguras en todas las plataformas.
- **Nota**: En `WasmJS`, estas primitivas son no-ops debido a la naturaleza single-threaded de JS.

## Manipulación de Datos (Merge y Patch)

Al trabajar con modelos de datos que heredan de `EntityBase<ID>`, debes seguir estas reglas
estrictas de integridad:

### Reglas de Integridad

- **ID Inmutable**: Nunca modifiques el `id` de una entidad durante un merge o patch.
- **createdAt Inmutable**: La fecha de creación debe preservarse utilizando `preserveCreatedAt`.
- **updatedAt Dinámico**: Siempre actualiza `updatedAt` al realizar cambios utilizando
  `updateTimestamp`.
- **Validación Proactiva**: Prefiere los helpers con validación integrada (`mergeWithValidation`,
  `validateAndBuild`) cuando el modelo implemente `ValidatableModel`.

### Patrones de Uso Comunes

#### Actualización Parcial (Patch) con Builder

```kotlin
val result = user.buildPatch()
    .patch("name", updates.name) { copy(name = it) }
    .patch("email", updates.email) { copy(email = it) }
    .updateTimestampIfEntityBase()
    .validateAndBuild()
```

#### Fusión de Entidades (Merge)

```kotlin
fun mergeEntities(original: MyEntity, updates: MyEntity): MyEntity {
    return original.mergeEntityBase(updates) { orig, upd ->
        orig.copy(
            name = upd.name,
            updatedAt = Clock.System.now()
        )
    }
}
```

## Estándares de Plataforma

- **Android/JVM**: Utiliza mecanismos nativos de Java para thread-safety.
- **iOS**: Aprovecha `AtomicReference` y el modelo de memoria de Kotlin/Native.
- **WasmJS**: Optimizado para el event-loop de JavaScript.
