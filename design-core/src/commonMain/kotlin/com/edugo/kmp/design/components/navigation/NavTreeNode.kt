package com.edugo.kmp.design.components.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Nodo de un árbol de navegación de N niveles, agnóstico de SDUI.
 *
 * `design-core` NO conoce el contrato SDUI (`NavigationItem`): el mapeo
 * `NavigationItem -> NavTreeNode` vive en `kmp-screens` (ver `NavTreeMapping.kt`).
 * Este modelo solo transporta lo que el renderer necesita para pintar el árbol:
 * identidad (`key`), texto (`label`), iconos ya resueltos a [ImageVector],
 * estado de habilitado y los hijos (recursivo).
 *
 * Es el modelo de entrada del renderer único [DSNavTree], reutilizado por el
 * overlay (plan 049) y por sidebar acordeón + flyout (plan 050).
 *
 * @property key Identidad estable del nodo dentro del árbol. Debe ser única en
 *   todo el árbol (el activo y los expandidos se referencian por `key`).
 * @property label Texto visible. El renderer aplica elipsis AL FINAL si no cabe.
 * @property icon Icono en variante outlined (estado normal). `null` => sin icono.
 * @property selectedIcon Icono en variante filled (nodo activo / en ruta activa).
 *   `null` => se reutiliza [icon].
 * @property enabled Si `false`, el nodo se atenúa y no dispara callbacks.
 * @property children Sub-nodos. Vacío => hoja (seleccionable). No vacío => rama
 *   (expandible/colapsable).
 */
data class NavTreeNode(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
    val enabled: Boolean = true,
    val children: List<NavTreeNode> = emptyList(),
) {
    /** `true` si el nodo es una hoja (sin hijos): se selecciona, no se expande. */
    val isLeaf: Boolean get() = children.isEmpty()

    /** `true` si el nodo es una rama (con hijos): se expande/colapsa. */
    val isBranch: Boolean get() = children.isNotEmpty()
}
