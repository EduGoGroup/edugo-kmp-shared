# Módulo Settings

El módulo `settings` gestiona las preferencias locales del usuario y los estados persistentes de la
interfaz en EduGo KMP. Provee servicios reactivos para controlar aspectos visuales como el tema de
la aplicación y la disposición de los componentes de navegación.

## Características Principales

### 1. Gestión de Temas (`ThemeService`)

Permite controlar la apariencia visual de la aplicación:

- **ThemeOption**: Soporte para temas `LIGHT`, `DARK` y seguimiento del esquema del sistema (
  `SYSTEM`).
- **Persistencia**: Las preferencias se guardan automáticamente en el almacenamiento seguro.
- **Reactividad**: Utiliza `StateFlow` para notificar a la UI sobre cambios en tiempo real.

### 2. Estado de Navegación (`SidebarService`)

Gestiona la disposición de la barra lateral (sidebar):

- **Collapsed/Expanded**: Controla si el sidebar está contraído o extendido.
- **Sincronización**: Mantiene el estado consistente entre sesiones del usuario.

## Arquitectura de los Servicios

Todos los servicios en este módulo siguen un patrón de diseño consistente:

1. **Interface**: Define el contrato reactivo y los métodos de actualización.
2. **Implementación**: Utiliza `SafeEduGoStorage` para garantizar que los cambios se guarden de
   forma persistente y segura.
3. **Flujos**: Exponen `StateFlow` para permitir que los componentes de Compose Multiplatform se
   suscriban a los cambios de estado.

## Cómo Usar

### Cambiar el Tema

```kotlin
val themeService: ThemeService = ... // Inyectado via Koin
themeService.setThemePreference(ThemeOption.DARK)
```

### Alternar el Sidebar

```kotlin
sidebarService.toggleCollapsed()
```

## Dependencias

- `:modules:storage`: Motor de persistencia local.
- `kotlinx-coroutines-core`: Soporte para flujos reactivos.
