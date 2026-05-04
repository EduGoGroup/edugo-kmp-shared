package com.edugo.kmp.auth.authorization

/**
 * Interface genérica para representar un permiso en el sistema.
 *
 * Ejemplo:
 * ```kotlin
 * enum class AppPermission(
 *     override val resource: String,
 *     override val action: String
 * ) : Permission {
 *     VIEW_GRADES("grades", "view"),
 *     EDIT_GRADES("grades", "edit"),
 *     MANAGE_USERS("users", "manage");
 *
 *     override val name: String get() = this.toString()
 * }
 * ```
 */
interface Permission {
    val name: String
    val resource: String
    val action: String
}
