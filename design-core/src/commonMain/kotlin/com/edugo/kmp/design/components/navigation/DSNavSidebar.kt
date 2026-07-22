package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing

/**
 * Sidebar de navegación persistente para Expanded/Large (plan 050 D-050.1).
 *
 * Ancho [Sizes.sidebarWidth] (280dp), anclada arriba, superficie `surfaceContainerLow` y un
 * divisor `borderThin` × `outlineVariant` en su borde derecho (contra el contenido, spec §4).
 * El cuerpo es el renderer ÚNICO [DSNavTree] — el MISMO árbol que pinta el overlay (plan 049)
 * y el flyout del riel ([DSNavRail]): un solo renderer, sin duplicar.
 *
 * Estado 100% HOISTED: el caller es dueño de [expandedKeys] y del [activeKey] (se persisten en
 * `MainScreen`). [onToggle] alterna una rama; [onSelect] navega a una hoja (el árbol solo
 * dispara `onSelect` en hojas — las ramas expanden/colapsan).
 *
 * @param nodes Árbol a pintar (mapeado desde SDUI en kmp-screens).
 * @param activeKey Key del nodo activo (hoja seleccionada). `null` = ninguno.
 * @param expandedKeys Keys de ramas expandidas por el usuario (hoisted).
 * @param onToggle Se invoca al tocar una RAMA: alterna su expansión.
 * @param onSelect Se invoca al tocar una HOJA: selección/navegación.
 * @param modifier Modificador del contenedor (típicamente `fillMaxHeight`).
 * @param header Slot fijo arriba (logo/contexto/acciones). `null` => sin header.
 * @param footer Slot fijo abajo (p. ej. botón de colapsar a riel). `null` => sin footer.
 */
@Composable
fun DSNavSidebar(
    nodes: List<NavTreeNode>,
    activeKey: String?,
    expandedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onSelect: (NavTreeNode) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.width(Sizes.sidebarWidth),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (header != null) {
                    header()
                    HorizontalDivider()
                }
                DSNavTree(
                    nodes = nodes,
                    activeKey = activeKey,
                    expandedKeys = expandedKeys,
                    onToggle = onToggle,
                    onSelect = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = Spacing.spacing2),
                )
                if (footer != null) {
                    HorizontalDivider()
                    footer()
                }
            }
            // Divisor contra el contenido: borderThin × outlineVariant (spec §4, D-050.1).
            VerticalDivider(
                thickness = Sizes.borderThin,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
