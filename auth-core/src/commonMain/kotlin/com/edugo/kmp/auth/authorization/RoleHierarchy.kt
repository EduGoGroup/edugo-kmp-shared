package com.edugo.kmp.auth.authorization

/**
 * Utilidad genérica para manejar jerarquías de roles.
 *
 * Ordena roles por level (mayor = más privilegiado) y permite
 * comparaciones jerárquicas.
 *
 * @param R Tipo concreto de Role
 * @property roles Lista de roles disponibles, ordenados automáticamente por level
 */
class RoleHierarchy<R : Role>(
    roles: List<R>,
) {
    private val sortedRoles: List<R> = roles.sortedBy { it.level }
    private val roleByName: Map<String, R> = roles.associateBy { it.name }

    /**
     * Obtiene todos los roles con nivel >= al rol dado.
     */
    fun getRolesAtLeast(role: R): List<R> = sortedRoles.filter { it.level >= role.level }

    /**
     * Obtiene todos los roles con nivel <= al rol dado.
     */
    fun getRolesAtMost(role: R): List<R> = sortedRoles.filter { it.level <= role.level }

    /**
     * Obtiene el rol con el nivel más alto de una lista.
     */
    fun getHighestRole(roles: List<R>): R? = roles.maxByOrNull { it.level }

    /**
     * Obtiene el rol con el nivel más bajo de una lista.
     */
    fun getLowestRole(roles: List<R>): R? = roles.minByOrNull { it.level }

    /**
     * Verifica si roleA tiene nivel mayor que roleB.
     */
    fun isHigherThan(
        roleA: R,
        roleB: R,
    ): Boolean = roleA.level > roleB.level

    /**
     * Verifica si roleA tiene nivel mayor o igual que roleB.
     */
    fun isAtLeast(
        roleA: R,
        roleB: R,
    ): Boolean = roleA.level >= roleB.level

    /**
     * Busca un rol por nombre.
     */
    fun findByName(name: String): R? = roleByName[name]

    /**
     * Obtiene todos los roles ordenados por nivel ascendente.
     */
    fun allRoles(): List<R> = sortedRoles.toList()
}
