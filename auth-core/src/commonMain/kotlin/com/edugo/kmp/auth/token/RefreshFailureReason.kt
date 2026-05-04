package com.edugo.kmp.auth.token

/**
 * Sealed class que representa las razones por las cuales un refresh de token puede fallar.
 *
 * Tras la inversión de dependencias (DA-12) este tipo es 100 % neutro: NO depende de
 * `AuthError` (modelo wire EduGo). Los códigos canónicos expuestos en [errorCode]
 * son `"no_refresh_token"`, `"token_expired"`, `"token_revoked"`, `"network"`,
 * `"server_<HTTP>"`. Los adaptadores EduGo traducen `AuthError` → estos códigos.
 */
sealed class RefreshFailureReason {
    abstract val errorCode: String

    object TokenExpired : RefreshFailureReason() {
        override val errorCode: String = "token_expired"
    }

    object TokenRevoked : RefreshFailureReason() {
        override val errorCode: String = "token_revoked"
    }

    object NoRefreshToken : RefreshFailureReason() {
        override val errorCode: String = "no_refresh_token"
    }

    data class NetworkError(
        val cause: String,
    ) : RefreshFailureReason() {
        override val errorCode: String = "network"
    }

    data class ServerError(
        val code: Int,
        val message: String,
    ) : RefreshFailureReason() {
        override val errorCode: String = "server_$code"
    }

    fun isRetryable(): Boolean =
        when (this) {
            is NetworkError -> true
            is ServerError -> code >= 500
            is TokenExpired,
            is TokenRevoked,
            is NoRefreshToken,
            -> false
        }

    fun toLogString(): String =
        when (this) {
            is TokenExpired -> "RefreshFailureReason.TokenExpired"
            is TokenRevoked -> "RefreshFailureReason.TokenRevoked"
            is NoRefreshToken -> "RefreshFailureReason.NoRefreshToken"
            is NetworkError -> "RefreshFailureReason.NetworkError(cause=$cause)"
            is ServerError -> "RefreshFailureReason.ServerError(code=$code, message=$message)"
        }
}
