package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Elevation
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.overlays.DSPlainTooltip
import com.edugo.kmp.design.components.overlays.DSPopup

/** Ancho del flyout de sección (mismo cuerpo que la sidebar: [Sizes.sidebarWidth]). */
private val RailFlyoutWidth = Sizes.sidebarWidth

/** Alto máximo del flyout: por encima, [DSNavTree] scrollea su subárbol dentro del popup. */
private val RailFlyoutMaxHeight = 480.dp

/** Lado del indicador/target táctil de cada icono L1 del riel (48dp, spec táctil §5.1). */
private val RailIndicatorSize = Sizes.TouchTarget.minimum

/**
 * Enlace del árbol al flyout del riel: estado HOISTED del árbol + callbacks. Agrupa los
 * parámetros cohesivos del subárbol para no inflar las firmas internas (regla detekt del plan).
 * [onSelect] ya incluye el cierre del flyout tras elegir una hoja.
 */
internal data class NavRailFlyoutBinding(
    val activeKey: String?,
    val expandedKeys: Set<String>,
    val onToggle: (String) -> Unit,
    val onSelect: (NavTreeNode) -> Unit,
)

/**
 * Riel colapsado de navegación para Expanded/Large (plan 050 D-050.2): la sidebar plegada.
 *
 * Ancho [Sizes.railWidth] (80dp), anclado arriba, `surfaceContainerLow` con divisor
 * `borderThin` × `outlineVariant` contra el contenido. Muestra SOLO los iconos L1; el label
 * completo vive en un [DSPlainTooltip] (los iconos no llevan texto — el tooltip es su única
 * forma de leer el nombre, task 1.5).
 *
 * Clic en una SECCIÓN (rama L1) → abre un **flyout** anclado a su derecha con el subárbol de
 * esa sección pintado por el MISMO [DSNavTree] (nunca L2 como pestañas). Clic en una HOJA L1 →
 * navega directo. El flyout cierra por selección, Escape/back y clic fuera ([DSPopup]).
 *
 * `openKey` (qué sección tiene el flyout abierto) es estado de UI LOCAL —como el scroll del
 * árbol—; la selección y la expansión ([expandedKeys]) siguen HOISTED en el caller.
 *
 * @param nodes Árbol a pintar (mapeado desde SDUI). Solo el nivel L1 se pinta como iconos.
 * @param activeKey Key del nodo activo. Un icono L1 se resalta si el activo cuelga de su
 *   sección (ver [containsKey]).
 * @param expandedKeys Keys de ramas expandidas (hoisted) — aplican dentro del flyout.
 * @param onToggle Se invoca al tocar una RAMA dentro del flyout: alterna su expansión.
 * @param onSelect Se invoca al elegir una HOJA (L1 directo o dentro del flyout): navegación.
 * @param modifier Modificador del contenedor (típicamente `fillMaxHeight`).
 * @param header Slot fijo arriba (p. ej. botón de expandir la sidebar). `null` => sin header.
 */
@Composable
fun DSNavRail(
    nodes: List<NavTreeNode>,
    activeKey: String?,
    expandedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onSelect: (NavTreeNode) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
) {
    var openKey by remember { mutableStateOf<String?>(null) }
    val flyout = NavRailFlyoutBinding(
        activeKey = activeKey,
        expandedKeys = expandedKeys,
        onToggle = onToggle,
        onSelect = { leaf ->
            openKey = null
            onSelect(leaf)
        },
    )
    Surface(
        modifier = modifier.width(Sizes.railWidth),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                header?.invoke(this)
                nodes.forEach { node ->
                    NavRailItem(
                        node = node,
                        active = node.containsKey(activeKey),
                        flyoutOpen = openKey == node.key,
                        onClick = {
                            when {
                                node.isBranch -> openKey = if (openKey == node.key) null else node.key
                                else -> {
                                    openKey = null
                                    onSelect(node)
                                }
                            }
                        },
                        onDismissFlyout = { openKey = null },
                        flyout = flyout,
                    )
                }
            }
            VerticalDivider(
                thickness = Sizes.borderThin,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

/**
 * Un icono L1 del riel + su flyout anclado. La `Box` externa es el ancla del [DSPopup]: el
 * flyout se posiciona a la derecha del riel, a la altura de este item.
 */
@Composable
private fun NavRailItem(
    node: NavTreeNode,
    active: Boolean,
    flyoutOpen: Boolean,
    onClick: () -> Unit,
    onDismissFlyout: () -> Unit,
    flyout: NavRailFlyoutBinding,
) {
    Box(contentAlignment = Alignment.TopStart) {
        DSPlainTooltip(tooltipText = node.label) {
            RailIcon(node = node, active = active, onClick = onClick)
        }
        if (flyoutOpen && node.isBranch) {
            NavRailFlyout(section = node, flyout = flyout, onDismiss = onDismissFlyout)
        }
    }
}

/**
 * Icono L1 con indicador de activo (pill `secondaryContainer`, look del rail de M3). El item
 * bloqueado se atenúa + candado pero SIGUE clickable (el consumidor intercepta el tap para
 * abrir el selector de contexto), igual que [DSNavTree] / [DSNavigationRail].
 */
@Composable
private fun RailIcon(
    node: NavTreeNode,
    active: Boolean,
    onClick: () -> Unit,
) {
    val indicator = if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val tint = if (active) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .padding(vertical = Spacing.spacing1)
            .clip(CircleShape)
            .background(indicator)
            .clickable(onClick = onClick)
            .size(RailIndicatorSize)
            .alpha(if (node.enabled) 1f else DisabledNavItemAlpha),
        contentAlignment = Alignment.Center,
    ) {
        DSLockedNavIcon(locked = !node.enabled) {
            val icon = if (active) node.selectedIcon ?: node.icon else node.icon
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = node.label, tint = tint)
            } else {
                // Degenerado: sección sin icono declarado — inicial como affordance neutra.
                Text(
                    text = node.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = tint,
                )
            }
        }
    }
}

/** Flyout de sección: [DSPopup] `surfaceContainerHigh` / level2 anclado a la derecha del riel. */
@Composable
private fun NavRailFlyout(
    section: NavTreeNode,
    flyout: NavRailFlyoutBinding,
    onDismiss: () -> Unit,
) {
    val railOffsetPx = with(LocalDensity.current) { Sizes.railWidth.roundToPx() }
    DSPopup(
        onDismissRequest = onDismiss,
        alignment = Alignment.TopStart,
        offset = IntOffset(railOffsetPx, 0),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = Elevation.level2,
    ) {
        NavRailFlyoutPanel(section = section, flyout = flyout)
    }
}

/**
 * Contenido del flyout: título de la sección + subárbol (`section.children`) pintado por el
 * renderer ÚNICO [DSNavTree]. `internal` para que previews/tests del módulo lo monten aislado.
 */
@Composable
internal fun NavRailFlyoutPanel(
    section: NavTreeNode,
    flyout: NavRailFlyoutBinding,
) {
    Column(modifier = Modifier.width(RailFlyoutWidth)) {
        Text(
            text = section.label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.spacing4, vertical = Spacing.spacing3),
        )
        HorizontalDivider()
        DSNavTree(
            nodes = section.children,
            activeKey = flyout.activeKey,
            expandedKeys = flyout.expandedKeys,
            onToggle = flyout.onToggle,
            onSelect = flyout.onSelect,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = RailFlyoutMaxHeight)
                .padding(vertical = Spacing.spacing2),
        )
    }
}
