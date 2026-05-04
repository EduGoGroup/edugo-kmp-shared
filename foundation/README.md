# Módulo Foundation

El módulo `foundation` es la piedra angular del proyecto EduGo KMP. Provee los tipos de datos base,
interfaces de modelado, sistema de errores y utilidades de serialización que son compartidos por
todos los módulos del ecosistema. Su objetivo es garantizar la consistencia arquitectónica y el
rigor técnico en todo el proyecto.

## Componentes Principales

### 1. Gestión de Resultados (`Result<T>`)

Implementa un patrón funcional para el manejo de operaciones que pueden fallar:

- **Estados**: `Success`, `Failure` (con soporte para reintentos), y `Loading`.
- **Operaciones**: Soporte exhaustivo para transformaciones (`map`, `flatMap`), combinaciones (
  `zip3` a `zip5`), y recuperación de errores (`recover`).
- **Colecciones**: Extensiones para procesar listas de resultados (`sequence`, `traverse`).

### 2. Modelado de Entidades (`Entity`)

Interfaces base para definir el comportamiento de los modelos de dominio:

- `EntityBase<ID>`: Define identidad y timestamps básicos (`createdAt`, `updatedAt`).
- `ValidatableModel`: Contrato para validación interna de reglas de negocio.
- `AuditableModel`: Trazabilidad extendida (`createdBy`, `updatedBy`).
- `SoftDeletable`: Soporte nativo para eliminación lógica.

### 3. Sistema de Errores Estructurado

- `AppError`: Representación inmutable de errores con contexto rico, causa original y metadatos.
- `ErrorCode`: Catálogo estandarizado de códigos de error categorizados (Network, Auth, Business,
  etc.) con mapeo automático a códigos HTTP.

### 4. Serialización y JSON

- `JsonConfig`: Configuraciones centralizadas de `kotlinx.serialization` (Default, Pretty, Strict,
  Lenient).
- `Extensions`: Métodos seguros (`toJson`, `fromJson`) que capturan excepciones y las convierten en
  tipos `Result`.
- `Serializers`: Soporte para tipos complejos como `Instant` y `Throwable`.

### 5. Paginación y Mapeo

- `PagedResult`: Estructura estandarizada para conjuntos de datos paginados con metadatos de
  navegación.
- `DomainMapper`: Interfaz para conversiones seguras entre DTOs y modelos de dominio con validación
  integrada.

## Principios de Diseño

- **Inmutabilidad**: Todos los modelos y estados son inmutables por defecto.
- **Type-Safety**: Uso intensivo de genéricos y tipos sellados para evitar errores en tiempo de
  ejecución.
- **Multiplataforma**: Código 100% Kotlin Common, compatible con todos los targets del proyecto.

## Dependencias

- Es un módulo de nivel "hoja", no depende de otros módulos internos, solo de librerías base de
  Kotlin.
