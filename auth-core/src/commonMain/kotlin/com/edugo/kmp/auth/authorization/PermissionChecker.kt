package com.edugo.kmp.auth.authorization

/**
 * Interface genérica para verificar permisos basados en roles.
 *
 * @param R Tipo concreto de Role
 * @param P Tipo concreto de Permission
 */
interface PermissionChecker<R : Role, P : Permission> {
    /**
     * Verifica si un rol tiene un permiso específico.
     */
    fun hasPermission(
        role: R,
        permission: P,
    ): Boolean

    /**
     * Verifica si un rol tiene al menos uno de los permisos dados.
     */
    fun hasAnyPermission(
        role: R,
        permissions: Set<P>,
    ): Boolean

    /**
     * Verifica si un rol tiene todos los permisos dados.
     */
    fun hasAllPermissions(
        role: R,
        permissions: Set<P>,
    ): Boolean

    /**
     * Obtiene todos los permisos efectivos para un rol.
     */
    fun getEffectivePermissions(role: R): Set<P>
}
