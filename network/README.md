# Módulo Network

El módulo `network` proporciona una infraestructura de red robusta, type-safe y multiplataforma para
EduGo KMP. Basado en **Ktor Client**, ofrece una abstracción de alto nivel para realizar peticiones
HTTP con soporte nativo para serialización, interceptores, reintentos automáticos y monitoreo de
conectividad.

## Características Principales

### 1. EduGoHttpClient

Un cliente HTTP personalizado que simplifica las operaciones comunes:

- **Peticiones Type-Safe**: Métodos genéricos para GET, POST, PUT, PATCH y DELETE con
  deserialización automática a modelos de dominio.
- **Variantes Safe**: Métodos `getSafe`, `postSafe`, etc., que capturan excepciones y retornan tipos
  `Result<T>` del módulo `foundation`.
- **Ruteo Dinámico**: Facilita el trabajo con múltiples APIs y parámetros de consulta.

### 2. Motores por Plataforma

Utiliza el motor HTTP óptimo para cada sistema operativo:

- **Android**: `OkHttp` (Soporte HTTP/2, pooling de conexiones).
- **iOS**: `Darwin` (Integración nativa con `URLSession`).
- **JVM/Desktop**: `CIO` (I/O basado en corrutinas, 100% Kotlin).
- **Web (WasmJS)**: `Js` (Basado en la Fetch API del navegador).

### 3. Sistema de Interceptores (`Middleware`)

Cadena de procesamiento para modificar peticiones y observar respuestas:

- **Headers**: Inyección automática de User-Agent, Content-Type y lenguajes.
- **Auth**: Integración con el módulo `auth` para inyectar tokens Bearer.
- **Logging**: Registro detallado de tráfico con **Sanitización Automática** de datos sensibles (
  passwords, tokens).
- **Telemetry**: Registro de métricas de rendimiento y errores en el módulo `telemetry`.

### 4. Resiliencia y Retry

Configuración de reintentos automáticos con:

- **Exponential Backoff**: Tiempos de espera incrementales entre fallos.
- **Jitter**: Variabilidad aleatoria para evitar colisiones de reintentos (Thundering Herd problem).
- **Filtros Inteligentes**: Reintenta solo en errores de red o códigos HTTP específicos (5xx, 429).

### 5. Observador de Red (`NetworkObserver`)

Monitoreo en tiempo real del estado de la conexión:

- Expone un `StateFlow<NetworkStatus>` (AVAILABLE, UNAVAILABLE, LOSING).
- Implementaciones nativas (ConnectivityManager en Android, NWPathMonitor en iOS).

## Cómo Usar

### Creación del Cliente

```kotlin
val client = EduGoHttpClient.builder()
    .logging(LogLevel.INFO)
    .retry(RetryConfig.Default)
    .build()
```

### Petición Segura

```kotlin
val result = client.getSafe<User>("/api/v1/profile")
result.onSuccess { user -> println(user.name) }
```

## Dependencias

- `io.ktor:ktor-client`: Motor base.
- `:modules:foundation`: Tipos de resultado y error.
- `:modules:logger`: Diagnóstico de red.
- `:modules:telemetry`: Métricas de rendimiento.
