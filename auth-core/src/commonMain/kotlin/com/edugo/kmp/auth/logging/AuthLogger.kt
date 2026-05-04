package com.edugo.kmp.auth.logging

import com.edugo.kmp.logger.Logger

class AuthLogger(
    private val logger: Logger,
) {
    private companion object {
        private const val TAG = "Auth"

        private fun maskEmail(email: String): String {
            val atIdx = email.indexOf('@')
            if (atIdx <= 0) return "***"
            return email.take(1) + "***@" + email.substring(atIdx + 1)
        }
    }

    fun logLoginAttempt(email: String) {
        logger.i(TAG, "LOGIN_ATTEMPT | user=${maskEmail(email)}")
    }

    fun logLoginSuccess(
        email: String,
        userId: String,
    ) {
        logger.i(TAG, "LOGIN_SUCCESS | user=${maskEmail(email)} | userId=$userId")
    }

    fun logLoginFailure(
        email: String,
        reason: String,
    ) {
        logger.w(TAG, "LOGIN_FAILURE | user=${maskEmail(email)} | reason=$reason")
    }

    fun logLogout(userId: String?) {
        logger.i(TAG, "LOGOUT | userId=${userId ?: "unknown"}")
    }

    fun logTokenRefresh(success: Boolean) {
        if (success) {
            logger.d(TAG, "TOKEN_REFRESH | status=success")
        } else {
            logger.w(TAG, "TOKEN_REFRESH | status=failure")
        }
    }

    fun logSessionExpired(reason: String) {
        logger.w(TAG, "SESSION_EXPIRED | reason=$reason")
    }

    fun logSessionRestored(userId: String) {
        logger.i(TAG, "SESSION_RESTORED | userId=$userId")
    }

    fun logAutoRefreshStarted() {
        logger.d(TAG, "AUTO_REFRESH | status=started")
    }

    fun logAutoRefreshStopped() {
        logger.d(TAG, "AUTO_REFRESH | status=stopped")
    }
}
