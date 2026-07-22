package com.edugo.kmp.design.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.AnimationDuration

private const val ChevronCollapsed = 0f
private const val ChevronExpanded = 180f

/**
 * Renderer ÚNICO del árbol de navegación (plan 049 D-049.2), evolución de N niveles
 * de [DSExpandableDrawerContent]. Lo reutilizan el overlay (plan 049) y la sidebar
 * acordeón + flyout (plan 050).
 *
 * Reutiliza las piezas del módulo: [NavigationDrawerItem] (que ya aporta el pill
 * `secondaryContainer` de radio full para el ítem seleccionado y la densidad/ripple
 * estándar), [DSLockedNavIcon] + [DisabledNavItemAlpha] para el estado bloqueado, y
 * el chevron rotado con [AnimationDuration.medium1] igual que el drawer.
 *
 * Acordeón recursivo pintado como una lista PLANA: la recursión y toda la derivación
 * de estado viven en funciones puras ([flattenVisibleRows]); este composable solo
 * pinta filas. Indentación `Spacing.spacing4` por nivel, `labelLarge` con elipsis AL
 * FINAL. Ruta activa: los ancestros del activo se pintan en `primary` con chevron
 * abierto; la hoja activa lleva el pill nativo de [NavigationDrawerItem].
 *
 * Estado 100% HOISTED: el caller es dueño de [expandedKeys] y persiste la expansión
 * (en v1, `rememberSaveable` a nivel de MainScreen). El único estado interno es la
 * posición de scroll (UI, no de navegación), igual que [DSExpandableDrawerContent].
 *
 * Scroll: [DSNavTree] YA aporta su propio scroll vertical (`Column` + `verticalScroll`,
 * mismo patrón que el drawer). NO lo envuelvas en otro contenedor scrolleable; dale un
 * tamaño acotado con [modifier].
 *
 * @param nodes Árbol a pintar (mapeado desde SDUI en kmp-screens).
 * @param activeKey Key del nodo activo (hoja seleccionada). `null` = ninguno.
 * @param expandedKeys Keys de ramas expandidas por el usuario (hoisted). Los ancestros
 *   del activo se revelan aunque no estén aquí (ver [effectiveExpandedKeys]).
 * @param onToggle Se invoca al tocar una RAMA: alterna su expansión (típicamente el
 *   caller aplica `toggleExpanded(expandedKeys, key)`).
 * @param onSelect Se invoca al tocar una HOJA: selección/navegación.
 * @param modifier Modificador del contenedor (tamaño / insets del caller).
 */
@Composable
fun DSNavTree(
    nodes: List<NavTreeNode>,
    activeKey: String?,
    expandedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onSelect: (NavTreeNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = nodes.flattenVisibleRows(expandedKeys, activeKey)
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        rows.forEach { row -> NavTreeRow(row = row, onToggle = onToggle, onSelect = onSelect) }
    }
}

@Composable
private fun NavTreeRow(
    row: NavRowData,
    onToggle: (String) -> Unit,
    onSelect: (NavTreeNode) -> Unit,
) {
    val itemModifier =
        Modifier
            .padding(
                start = Spacing.spacing3 + Spacing.spacing4 * row.depth,
                end = Spacing.spacing3,
            ).alpha(if (row.node.enabled) 1f else DisabledNavItemAlpha)
    if (row.hasChildren) {
        NavTreeBranchRow(row = row, modifier = itemModifier, onToggle = onToggle)
    } else {
        NavTreeLeafRow(row = row, modifier = itemModifier, onSelect = onSelect)
    }
}

@Composable
private fun NavTreeLeafRow(
    row: NavRowData,
    modifier: Modifier,
    onSelect: (NavTreeNode) -> Unit,
) {
    val node = row.node
    NavigationDrawerItem(
        label = { NavTreeLabel(node.label) },
        selected = row.isActive,
        onClick = { onSelect(node) },
        icon = leafIcon(node = node, active = row.isActive),
        modifier = modifier,
    )
}

@Composable
private fun NavTreeBranchRow(
    row: NavRowData,
    modifier: Modifier,
    onToggle: (String) -> Unit,
) {
    val node = row.node
    val rotation by animateFloatAsState(
        targetValue = if (row.isExpanded) ChevronExpanded else ChevronCollapsed,
        animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
        label = "navtree_chevron_${node.key}",
    )
    NavigationDrawerItem(
        label = { NavTreeLabel(node.label) },
        selected = false,
        onClick = { onToggle(node.key) },
        icon = branchIcon(node = node),
        badge = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (row.isExpanded) "Colapsar" else "Expandir",
                modifier = Modifier.rotate(rotation),
            )
        },
        colors =
            if (row.isOnActivePath) {
                NavigationDrawerItemDefaults.colors(
                    unselectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedBadgeColor = MaterialTheme.colorScheme.primary,
                )
            } else {
                NavigationDrawerItemDefaults.colors()
            },
        modifier = modifier,
    )
}

/**
 * Label del ítem: `labelLarge` con una sola línea y elipsis AL FINAL (nunca truncado
 * en medio tipo "Administra…"). Cubre el caso límite de labels largos del plan.
 */
@Composable
private fun NavTreeLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Slot de icono de una hoja: variante seleccionada cuando está activa, con candado
 * si está bloqueada. `null` si la hoja no declara icono (igual que el drawer).
 */
private fun leafIcon(node: NavTreeNode, active: Boolean): (@Composable () -> Unit)? {
    val icon = node.icon ?: return null
    return {
        DSLockedNavIcon(locked = !node.enabled) {
            Icon(
                imageVector = if (active) node.selectedIcon ?: icon else icon,
                contentDescription = node.label,
            )
        }
    }
}

/** Slot de icono de una rama (sin estado seleccionado). `null` si no declara icono. */
private fun branchIcon(node: NavTreeNode): (@Composable () -> Unit)? {
    val icon: ImageVector = node.icon ?: return null
    return {
        Icon(imageVector = icon, contentDescription = node.label)
    }
}
