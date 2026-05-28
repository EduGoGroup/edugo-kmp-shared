package com.edugo.kmp.auth.authorization

/**
 * Mirror Kotlin exacto de `auth.PermissionMatches` (Go) y de
 * `iam.permission_matches()` (Postgres). Debe coincidir 1:1.
 *
 * Reglas D1 path-based + extensión wildcard-first:
 *
 *   `*`                 → cubre cualquier request
 *   `pattern == request`→ exacto
 *   `prefix.*`          → cubre `prefix` y `prefix.<lo-que-sea>` (subárbol)
 *   `*.suffix`          → cubre cualquier request cuyo último tramo sea
 *                         `.suffix` (ej. `*.create` matchea `users.create`
 *                         y `academic.units.create`)
 *   `prefix.*.suffix`   → cubre cualquier request que empiece con
 *                         `prefix.`, tenga al menos un segmento intermedio
 *                         y termine con `.suffix`
 *   sufijo `:own`       → semánticamente distinto, no se mezcla con
 *                         el mismo pattern sin `:own`
 *
 * La gramática válida de pattern y request vive en
 * `enum.PathPermissionRegex` (BE).
 */
object PermissionMatcher {
    fun matches(
        pattern: String,
        request: String,
    ): Boolean {
        if (pattern == "*") return true
        if (pattern == request) return true
        // prefix.*  (subárbol). Debe evaluarse antes que prefix.*.suffix
        // para preservar la semántica histórica cuando el pattern termina
        // literalmente con `.*`.
        if (pattern.endsWith(".*")) {
            val prefix = pattern.removeSuffix(".*")
            return request == prefix || request.startsWith("$prefix.")
        }
        // *.suffix → cualquier request `<algo>.suffix`
        if (pattern.startsWith("*.")) {
            val suffix = pattern.substring(1) // conserva el punto, ej ".create"
            return request.length > suffix.length && request.endsWith(suffix)
        }
        // prefix.*.suffix → request startsWith `prefix.` + algo + `.suffix`
        val i = pattern.indexOf(".*.")
        if (i > 0) {
            val head = pattern.substring(0, i + 1) // `prefix.`
            val tail = pattern.substring(i + 2) // `.suffix`
            // Los patterns soportados son los listados arriba; ya descartamos
            // `prefix.*`. Cualquier otro `*` invalida el match.
            if (head.contains('*') || tail.contains('*')) return false
            if (!request.startsWith(head) || !request.endsWith(tail)) return false
            // Si head y tail solapan en request, no hay segmento intermedio.
            if (request.length <= head.length + tail.length) return false
            val middle = request.substring(head.length, request.length - tail.length)
            return !middle.startsWith('.') && !middle.endsWith('.')
        }
        return false
    }
}
