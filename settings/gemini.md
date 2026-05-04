# Guía del Módulo Settings para Gemini

Este documento establece las reglas técnicas para la gestión de preferencias y estados de UI
persistentes en EduGo KMP.

## Implementación de Nuevas Preferencias

Si necesitas añadir una nueva configuración global a la aplicación, sigue este patrón:

### 1. Definición del Modelo

Crea un enum o data class en el paquete `model` si la configuración tiene múltiples opciones.

```kotlin
enum class MySetting { ON, OFF }
```

### 2. Definición del Servicio

Crea una interfaz que exponga un `StateFlow`.

```kotlin
interface MyService {
    val value: StateFlow<MySetting>
    fun setValue(newValue: MySetting)
}
```

### 3. Implementación y Persistencia

Utiliza siempre `SafeEduGoStorage` para la persistencia. Asegúrate de manejar el valor por defecto
si no existe nada guardado.

- Usa `putStringSafe` / `getStringSafe` para asegurar que los datos estén protegidos.
- Inicializa el `MutableStateFlow` con el valor cargado desde el storage.

## Reglas de Integridad

- **Carga Temprana**: Los servicios deben cargar su estado desde el disco en el bloque `init`.
- **Atomicidad**: La actualización del disco y del `StateFlow` debe ocurrir en el mismo método para
  evitar discrepancias entre lo que ve el usuario y lo que se guarda.
- **Nombres de Claves**: Utiliza el prefijo `app.` para las claves de storage (ej:
  `app.sidebar.collapsed`) para mantener organizado el espacio de nombres de preferencias.

## Uso en la UI (Compose)

Al consumir estos servicios en la capa de presentación:

- Utiliza `.collectAsState()` o `.collectAsStateWithLifecycle()` para observar los cambios.
- No realices lógica de guardado directamente en los Composables; delega siempre en el servicio
  correspondiente.

## Testing

Para probar servicios de configuración:

- Utiliza `MapSettings` como motor de almacenamiento para evitar dependencias de plataforma.
- Verifica que el `StateFlow` emita el nuevo valor inmediatamente tras llamar a un método de
  actualización.
- Verifica la persistencia creando una segunda instancia del servicio apuntando al mismo
  `MapSettings`.

