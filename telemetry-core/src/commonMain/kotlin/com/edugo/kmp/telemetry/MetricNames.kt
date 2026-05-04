package com.edugo.kmp.telemetry

/**
 * Constantes centralizadas para nombres de metricas.
 *
 * Convenciones:
 * - snake_case con sufijo de unidad donde aplique (_ms, _total)
 * - Agrupadas por dominio funcional
 */
public object MetricNames {

    // ==================== HTTP ====================
    public const val HTTP_REQUESTS_TOTAL: String = "http_requests_total"
    public const val HTTP_REQUEST_DURATION_MS: String = "http_request_duration_ms"
    public const val HTTP_ACTIVE_REQUESTS: String = "http_active_requests"
    public const val HTTP_ERRORS_TOTAL: String = "http_errors_total"

    // ==================== Auth ====================
    public const val AUTH_LOGINS_TOTAL: String = "auth_logins_total"
    public const val AUTH_LOGIN_DURATION_MS: String = "auth_login_duration_ms"
    public const val AUTH_TOKEN_REFRESH_TOTAL: String = "auth_token_refresh_total"
    public const val AUTH_RATE_LIMIT_HITS_TOTAL: String = "auth_rate_limit_hits_total"

    // ==================== UI ====================
    public const val UI_ERRORS_TOTAL: String = "ui_errors_total"

    // ==================== Cache ====================
    public const val CACHE_HITS_TOTAL: String = "cache_hits_total"
    public const val CACHE_MISSES_TOTAL: String = "cache_misses_total"
    public const val CACHE_ERRORS_TOTAL: String = "cache_errors_total"

    // ==================== Assessment ====================
    public const val ASSESSMENT_ATTEMPTS_TOTAL: String = "assessment_attempts_total"
    public const val ASSESSMENT_DURATION_MS: String = "assessment_duration_ms"
    public const val ASSESSMENT_ERRORS_TOTAL: String = "assessment_errors_total"

    // ==================== Navigation ====================
    public const val SCREEN_VIEWS_TOTAL: String = "screen_views_total"
    public const val NAVIGATION_TOTAL: String = "navigation_total"

    // ==================== Sync ====================
    public const val SYNC_OPERATIONS_TOTAL: String = "sync_operations_total"
    public const val SYNC_DURATION_MS: String = "sync_duration_ms"

    // ==================== App ====================
    public const val APP_START_DURATION_MS: String = "app_start_duration_ms"
    public const val APP_STATE_CHANGES_TOTAL: String = "app_state_changes_total"
}
