# Módulo Storage

El módulo `storage` proporciona una solución unificada y segura para la persistencia de datos
clave-valor en EduGo KMP. Actúa como un wrapper sobre **multiplatform-settings**, ofreciendo una API
type-safe, asíncrona y reactiva que funciona de forma nativa en todas las plataformas.

## Arquitectura de Almacenamiento

El sistema se organiza en capas para balancear simplicidad, seguridad y rendimiento:

### 1. Capa Base (`EduGoStorage`)

Provee la API fundamental para tipos primitivos (String, Int, Boolean, etc.) con soporte para
prefijos de clave, permitiendo el aislamiento de datos entre diferentes componentes.

### 2. Capa de Seguridad (`SafeEduGoStorage`)

Agrega una capa de robustez sobre la base:

- **Validación de Claves**: Asegura que las claves sigan el estándar permitido.
- **Manejo de Errores**: Captura excepciones de bajo nivel y retorna tipos `Result`.
- **Auditoría**: Integra con el módulo `logger` para registrar fallos en operaciones de E/S.
- **Ofuscación**: Protege datos sensibles (tokens de sesión) mediante `ObfuscatedStorage` utilizando
  un motor XOR.

### 3. Capa Asíncrona (`AsyncEduGoStorage`)

Wrapper basado en corrutinas que delega las operaciones al dispatcher adecuado (
`Dispatchers.Default` o `Dispatchers.IO`), evitando el bloqueo del hilo de interfaz de usuario.

### 4. Capa Reactiva (`StateFlowStorage`)

Mantiene estados sincronizados entre el disco y la memoria, exponiendo `StateFlows` que notifican
automáticamente a la UI cuando un valor cambia.

## Características Avanzadas

- **Delegated Properties**: Permite declarar preferencias como variables normales:
  `var name by storage.string("user_name")`.
- **Serialización JSON**: Soporte nativo para guardar objetos complejos, listas, sets y maps
  mediante `kotlinx.serialization`.
- **Migraciones**: Sistema de versionado de esquema (`StorageMigrator`) para transformar datos entre
  versiones de la aplicación.

## Implementaciones por Plataforma

- **Android**: `SharedPreferences`.
- **iOS**: `NSUserDefaults`.
- **JVM/Desktop**: `java.util.prefs.Preferences`.
- **WasmJS**: `LocalStorage`.

## Dependencias

- `multiplatform-settings`: Motor subyacente.
- `:modules:foundation`: Tipos de resultado y serialización.
- `:modules:logger`: Registro de errores.

