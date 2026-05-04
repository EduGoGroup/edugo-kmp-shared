# auth-core

Módulo neutro de la capa 1 del shared `edugo-kmp-shared`. Define los puertos
para autenticación basada en tokens y los componentes auxiliares (circuit breaker,
retry policy, JWT parser, rate limiter, AuthInterceptor).

## Propósito

`auth-core` provee la lógica de orquestación de tokens (refresh, validación,
manejo de fallos, single-flight, scheduling automático) sin acoplarse a ningún
backend EduGo concreto. La inversión de dependencias (DA-12) deja como puertos
los tres puntos de contacto que SÍ son específicos de producto:

- `TokenProvider` — almacenamiento local del par access/refresh.
- `RefreshTokenSource` — operación remota POST `/auth/refresh`.
- `TokenVerifier` — operación remota de verificación JWT.

Las impls concretas viven en el producto consumidor (en EduGo: `:modules:auth-edugo`).

## Las 3 interfaces

```kotlin
interface TokenProvider {
    suspend fun currentAccessToken(): Result<String>
    suspend fun currentRefreshToken(): Result<String>
    suspend fun clearTokens()
}

interface RefreshTokenSource {
    suspend fun refresh(refreshToken: String): Result<TokenPair>
}

interface TokenVerifier {
    suspend fun verify(token: String): Result<TokenVerificationResult>
}
```

## Ejemplo mínimo

```kotlin
val manager = TokenRefreshManagerImpl(
    tokenProvider = MyKeychainTokenProvider(),
    refreshTokenSource = MyHttpRefreshSource(httpClient),
    config = TokenRefreshConfig.DEFAULT,
    scope = appScope,
)

val validator = JwtValidatorImpl(
    tokenVerifier = MyHttpVerifier(httpClient),
)
```

## Símbolos públicos

- Token: `TokenPair`, `TokenRefreshConfig`, `TokenRefreshManager`, `TokenRefreshManagerImpl`,
  `RefreshFailureReason`.
- JWT: `JwtClaims`, `JwtParser`, `JwtParseResult`, `JwtValidator`, `JwtValidatorImpl`,
  `JwtValidationResult`, `TokenVerificationResult`.
- Authorization: `Permission`, `PermissionChecker`, `PermissionCheckerImpl`, `Role`,
  `RoleHierarchy`.
- Auxiliares: `RateLimiter`, `AuthInterceptor`, `AuthLogger`, `AuthConfig`,
  `circuit/CircuitBreaker(+Config,+State)`, `retry/RetryPolicy`.

## Tests

`commonTest/kotlin/com/edugo/kmp/auth/test/` contiene los dobles `FakeTokenProvider`,
`FakeRefreshTokenSource`, `FakeTokenVerifier` reutilizables en cualquier consumidor
externo de `auth-core`.

## Referencias

- DA-12 (split `auth-core` + DI invertida): `docs/architecture/kmp-shared-extraction-v2/02-adrs.md`.
- `01-arquitectura-objetivo.md §5` para el patrón completo.
- Fase 5 (extracción): `docs/architecture/kmp-shared-extraction-v2/phase-5-auth-decoupling/`.
