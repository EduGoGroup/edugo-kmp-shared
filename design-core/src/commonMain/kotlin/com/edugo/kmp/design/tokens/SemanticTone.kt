package com.edugo.kmp.design.tokens

/**
 * Tono semantico de un elemento visual (badge, chip de estado, indicador).
 *
 * Es un token, no un color. La conversion a colores concretos vive en el
 * componente que lo consume (ej. `DSStatusBadge`), siempre apoyandose en
 * `MaterialTheme.colorScheme` para respetar tema claro/oscuro y temas custom.
 *
 * Pertenece a `design-core` porque es reutilizable por cualquier componente
 * DS (no es propio del renderer SDUI ni de un contrato concreto).
 */
enum class SemanticTone {
    Neutral,
    Success,
    Warning,
    Danger,
    Info,
    ;

    companion object {
        /**
         * Fallback NO canonico: infiere el tono a partir de un string de
         * estado del backend cuando el contrato no declaro su mapeo en
         * `FieldMapping.statusTone`. Si vas a agregar un status nuevo,
         * declaralo explicitamente en el contrato — esto es solo una red
         * de seguridad para statuses historicos.
         *
         * Si no hay match, devuelve [Neutral].
         */
        fun fromStatus(raw: String): SemanticTone {
            val normalized = raw.trim().lowercase()
            return when (normalized) {
                "true", "active", "activo", "enabled", "ok", "success",
                "finalized", "finalizada", "published", "approved", "ready",
                -> Success
                "warning", "pending", "pendiente", "draft", "borrador",
                "processing", "in_progress",
                -> Warning
                "false", "inactive", "inactivo", "disabled",
                "error", "failed", "critical", "danger", "rejected",
                -> Danger
                "info", "notice" -> Info
                else -> Neutral
            }
        }
    }
}
