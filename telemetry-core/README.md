# Módulo Telemetry

El módulo `telemetry` proporciona una infraestructura unificada y desacoplada para el monitoreo,
análisis de uso y reporte de fallos en el ecosistema EduGo KMP. Utiliza un patrón de fachada (
facade) que permite a la aplicación registrar métricas y eventos sin depender de proveedores
específicos (como Firebase o Sentry) en la lógica de negocio.

## Arquitectura de Telemetría

El sistema se centraliza en la clase `Telemetry`, que agrupa tres áreas de responsabilidad:

### 1. Grabador de Métricas (`MetricsRecorder`)

Enfocado en el rendimiento técnico y salud del sistema:

- **Contadores**: Para eventos discretos (ej: `http_requests_total`).
- **Histogramas**: Para distribuciones de tiempo y tamaño (ej: `http_request_duration_ms`).
- **Gauges**: Para estados puntuales (ej: `active_connections`).

### 2. Grabador de Analíticas (`AnalyticsRecorder`)

Enfocado en el comportamiento del usuario y flujo de la aplicación:

- **Eventos**: Acciones específicas con propiedades dinámicas.
- **Pantallas**: Seguimiento de navegación y vistas de página.
- **Propiedades de Usuario**: Segmentación basada en roles, planes o preferencias.

### 3. Grabador de Fallos (`CrashRecorder`)

Enfocado en la estabilidad y diagnóstico:

- **Excepciones**: Registro de errores no controlados con contexto adicional.
- **Logs de Diagnóstico**: Breadcrumbs para reconstruir los pasos previos a un fallo.

## Helpers y Estandarización

El módulo incluye un catálogo exhaustivo de nombres en `MetricNames` para garantizar la consistencia
en los dashboards. Además, provee extensiones por dominio en el paquete `helpers` para simplificar
el registro de:

- Autenticación (`recordLogin`, `recordTokenRefresh`).
- Ciclo de vida de la App (`recordAppStart`).
- Red (`recordHttpRequest`, `recordNetworkError`).
- Sincronización offline y evaluaciones.

## Implementaciones

El sistema utiliza el patrón `Noop` (No-Operation) por defecto para todas las interfaces,
permitiendo que la aplicación funcione sin telemetría activa hasta que se inyecten los drivers
reales por plataforma.

## Dependencias

- `:modules:foundation`: Modelos base y Result.
- `:modules:core`: Abstracciones de plataforma.
