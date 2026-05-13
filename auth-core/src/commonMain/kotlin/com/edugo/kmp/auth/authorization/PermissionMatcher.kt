package com.edugo.kmp.auth.authorization

/**
 * Mirror Kotlin exacto de `auth.PermissionMatches` (Go) y de
 * `iam.permission_matches()` (Postgres). Debe coincidir 1:1.
 *
 * Reglas D1 path-based:
 *
 *   `*`                 → cubre cualquier request
 *   `pattern == request`→ exacto
 *   `prefix.*`          → cubre `prefix` y `prefix.<lo-que-sea>` (subárbol)
 *   sufijo `:own`       → semánticamente distinto, no se mezcla con
 *                         el mismo pattern sin `:own`
 *
 * La gramática válida de pattern y request vive en
 * `enum.PathPermissionRegex` (BE).
 */
object PermissionMatcher {
    fun matches(pattern: String, request: String): Boolean {
        if (pattern == "*") return true
        if (pattern == request) return true
        if (pattern.endsWith(".*")) {
            val prefix = pattern.removeSuffix(".*")
            return request == prefix || request.startsWith("$prefix.")
        }
        return false
    }
}
