# Módulo Core

El módulo `core` proporciona las utilidades fundamentales y abstracciones de plataforma para el
proyecto EduGo KMP. Actúa como un puente entre la capa de `foundation` y las implementaciones
específicas de cada plataforma, asegurando un comportamiento consistente en Android, iOS, JVM y
Web (WasmJS).

## Características Principales

### 1. Abstracciones de Plataforma

Proporciona una interfaz unificada para funcionalidades que varían entre sistemas operativos:

- **AppDispatchers**: Gestión de hilos mediante corrutinas (Main, IO, Default) adaptada a las
  capacidades de cada plataforma.
- **Platform**: Acceso a metadatos del sistema (nombre del OS, versión, modo debug).
- **Sincronización**: Primitivas para concurrencia segura como `platformSynchronized` y la anotación
  `@PlatformVolatile`.

### 2. Helpers de Manipulación de Modelos

Utilidades avanzadas para la gestión de estado y datos:

- **Merge Helpers**: Funciones para fusionar objetos (`mergeWithValidation`, `mergeEntityBase`) con
  soporte para estrategias de resolución de conflictos (`MergeStrategy`).
- **Patch Helpers**: Herramientas para actualizaciones parciales (`PatchBuilder`, `patchField`) que
  garantizan la integridad de campos inmutables.
- **Interfaces**: Definiciones de `Patchable` y `Mergeable` para estandarizar cómo los modelos se
  actualizan.

## Estructura del Módulo

- `commonMain`: Lógica compartida y definiciones `expect`.
- `androidMain`, `iosMain`, `desktopMain`, `wasmJsMain`: Implementaciones `actual` específicas.
- `commonTest`: Suite de pruebas exhaustiva para validar la lógica de merge y patch.

## Dependencias

- `:modules:foundation`: Proporciona las entidades base (`EntityBase`), validación (
  `ValidatableModel`) y tipos de resultado (`Result`).
