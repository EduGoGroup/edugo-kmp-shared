# Guía del Módulo Storage para Gemini

Este documento define los estándares para la persistencia de datos y el manejo seguro de información
en EduGo KMP.

## Reglas de Validación de Claves

Para garantizar la compatibilidad entre plataformas (especialmente en Android y iOS), todas las
claves de almacenamiento deben seguir estas reglas estrictas:

- **Caracteres Permitidos**: Alfanuméricos (`a-z`, `A-Z`, `0-9`), puntos (`.`), guiones (`-`) y
  guiones bajos (`_`).
- **Prohibido**: Espacios, caracteres especiales (`@`, `#`, `$`, `/`), y emojis.
- **Longitud Máxima**: 256 caracteres.
- **Convención**: Usa la notación de puntos para jerarquía (ej: `settings.theme.dark_mode`).

## Manejo de Datos Sensibles

- **Ofuscación Automática**: El sistema está configurado para ofuscar automáticamente las claves de
  identidad (`auth_token`, `auth_user`, `auth_context`).
- **Regla de Oro**: Nunca guardes contraseñas en texto plano. Si necesitas guardar un dato sensible
  nuevo, regístralo en la lista `AUTH_SENSITIVE_KEYS` de `ObfuscatedStorage`.
- **Precedencia**: Prefiere siempre inyectar `SafeEduGoStorage` en lugar de `EduGoStorage` para
  asegurar que la validación y ofuscación estén activas.

## Rendimiento y Asincronía

- **Dispatcher**: Utiliza `AsyncEduGoStorage` para operaciones que ocurran fuera del hilo principal,
  especialmente al serializar objetos JSON grandes o listas.
- **Lazy Loading**: No cargues todo el contenido del storage en memoria al inicio. Deja que los
  servicios manejen sus propias claves de forma granular.
- **StateFlow**: Usa `StateFlowStorage` solo para configuraciones que la UI necesite observar
  reactivamente (ej: preferencias visuales).

## Migración de Datos

Si el formato de un objeto guardado cambia:

1. Crea una clase que implemente `StorageMigration`.
2. Define la lógica de transformación en el método `migrate`.
3. Incrementa la versión en el `StorageMigrator`.
4. El sistema ejecutará el cambio secuencialmente en el próximo inicio de la app.

## Testing

En pruebas unitarias:

- **Nunca** utilices la persistencia real de la plataforma.
- Utiliza `EduGoStorage.withSettings(MapSettings())` para crear un almacén en memoria rápido y
  volátil.
- Limpia el estado entre pruebas usando `storage.clear()`.

