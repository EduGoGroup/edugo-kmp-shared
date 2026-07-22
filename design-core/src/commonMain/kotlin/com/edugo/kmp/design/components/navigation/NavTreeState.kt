package com.edugo.kmp.design.components.navigation

/**
 * Derivación de estado del árbol de navegación, en funciones PURAS y testeables,
 * separadas del composable de render ([DSNavTree]).
 *
 * Regla del plan 049 (lección detekt del 047): el cálculo de qué se ve, qué está
 * en la ruta activa y cómo cambia el set de expandidos NO vive en el composable.
 * Así el render queda trivial (bajo el umbral de complejidad 15) y esta lógica se
 * cubre con tests unitarios sin Compose.
 */

/**
 * Fila aplanada lista para pintar: un nodo del árbol con todo su estado derivado.
 *
 * El renderer recorre una `List<NavRowData>` plana (no recursiona en composables):
 * la recursión ocurre aquí, en [flattenVisibleRows].
 *
 * @property node Nodo original.
 * @property depth Profundidad (0 = raíz). Determina la indentación.
 * @property isExpanded Solo relevante si [hasChildren]: si la rama está abierta.
 * @property isActive `true` si este nodo ES el activo (pinta pill si además es hoja).
 * @property isOnActivePath `true` si es ANCESTRO del activo (se pinta en `primary`
 *   con chevron abierto).
 * @property hasChildren Atajo de `node.isBranch`.
 */
data class NavRowData(
    val node: NavTreeNode,
    val depth: Int,
    val isExpanded: Boolean,
    val isActive: Boolean,
    val isOnActivePath: Boolean,
    val hasChildren: Boolean,
)

/**
 * Alterna la expansión de [key] en [expandedKeys] (toggle inmutable).
 *
 * Es la operación que el caller aplica en `onToggle`: si estaba expandido lo
 * quita, si no, lo agrega. No muta el set de entrada.
 */
fun toggleExpanded(expandedKeys: Set<String>, key: String): Set<String> =
    if (key in expandedKeys) expandedKeys - key else expandedKeys + key

/**
 * Ruta activa completa como lista ordenada de keys desde la raíz hasta [activeKey]
 * inclusive. Vacía si [activeKey] es `null` o no existe en el árbol.
 *
 * Ej.: activo = "users-edit" => `[admin, users, users-edit]`.
 */
fun List<NavTreeNode>.activePath(activeKey: String?): List<String> {
    if (activeKey == null) return emptyList()
    return findPath(this, activeKey) ?: emptyList()
}

private fun findPath(nodes: List<NavTreeNode>, target: String): List<String>? {
    for (node in nodes) {
        if (node.key == target) return listOf(node.key)
        val childPath = findPath(node.children, target)
        if (childPath != null) return listOf(node.key) + childPath
    }
    return null
}

/**
 * Keys de los ANCESTROS del nodo activo (la ruta activa SIN el propio activo).
 *
 * Ej.: activo = "users-edit" => `{admin, users}`. Vacío si el activo es raíz o
 * no existe. Base para resaltar ancestros en `primary` y para auto-revelar la
 * rama activa ([effectiveExpandedKeys]).
 */
fun List<NavTreeNode>.ancestorKeysOf(activeKey: String?): Set<String> {
    val path = activePath(activeKey)
    if (path.size <= 1) return emptySet()
    return path.dropLast(1).toSet()
}

/**
 * Set de expandidos EFECTIVO para el render: los expandidos por el usuario más
 * los ancestros del activo.
 *
 * Decisión (plan 049): la rama que lleva al nodo activo se muestra SIEMPRE abierta
 * —no puedes ocultar el camino a donde estás parado—, aunque el usuario no la haya
 * expandido manualmente. Es derivación pura (determinista desde `expandedKeys` +
 * `activeKey`), no estado oculto: el caller sigue siendo dueño de [expandedKeys].
 */
fun List<NavTreeNode>.effectiveExpandedKeys(
    expandedKeys: Set<String>,
    activeKey: String?,
): Set<String> = expandedKeys + ancestorKeysOf(activeKey)

/**
 * Aplana el árbol a la lista de filas VISIBLES según el estado de expansión.
 *
 * Un nodo es visible si todos sus ancestros están expandidos (en el set efectivo,
 * ver [effectiveExpandedKeys]). Marca por fila `isActive` / `isOnActivePath` /
 * `isExpanded` para que el render sea directo.
 *
 * Esta es LA función de derivación que cubren los tests de casos límite
 * (profundidad 3+, 10 hijos, ruta activa).
 */
fun List<NavTreeNode>.flattenVisibleRows(
    expandedKeys: Set<String>,
    activeKey: String?,
): List<NavRowData> {
    val effective = effectiveExpandedKeys(expandedKeys, activeKey)
    val ancestors = ancestorKeysOf(activeKey)
    val out = ArrayList<NavRowData>()
    appendRows(this, depth = 0, effective = effective, activeKey = activeKey, ancestors = ancestors, out = out)
    return out
}

private fun appendRows(
    nodes: List<NavTreeNode>,
    depth: Int,
    effective: Set<String>,
    activeKey: String?,
    ancestors: Set<String>,
    out: MutableList<NavRowData>,
) {
    for (node in nodes) {
        val expanded = node.isBranch && node.key in effective
        out.add(
            NavRowData(
                node = node,
                depth = depth,
                isExpanded = expanded,
                isActive = node.key == activeKey,
                isOnActivePath = node.key in ancestors,
                hasChildren = node.isBranch,
            ),
        )
        if (expanded) {
            appendRows(node.children, depth + 1, effective, activeKey, ancestors, out)
        }
    }
}
