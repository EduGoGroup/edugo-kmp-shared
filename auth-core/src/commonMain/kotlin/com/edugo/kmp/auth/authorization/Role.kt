package com.edugo.kmp.auth.authorization

/**
 * Interface genérica para representar un rol en el sistema.
 *
 * Las apps consumidoras crean sus propios enums/sealed classes implementando esta interface.
 *
 * Ejemplo:
 * ```kotlin
 * enum class SystemRole(
 *     override val displayName: String,
 *     override val level: Int
 * ) : Role {
 *     STUDENT("Estudiante", 10),
 *     TEACHER("Profesor", 20),
 *     ADMIN("Administrador", 30);
 *
 *     override val name: String get() = this.toString()
 * }
 * ```
 */
interface Role {
    val name: String
    val displayName: String
    val level: Int
}
