package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Item del NavigationRail.
 *
 * @param enabled `false` atenúa el item y lo deja sin click; el resolver de la
 *   app lo usa para bloquear pantallas que necesitan un contexto (colegio/unidad)
 *   que todavía no está activo. Cuando está bloqueado se superpone el indicador de
 *   candado vía [DSLockedNavIcon].
 */
data class DSNavigationRailItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val enabled: Boolean = true,
)

@Composable
fun DSNavigationRail(
    items: List<DSNavigationRailItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
) {
    NavigationRail(
        modifier = modifier,
        header = header,
    ) {
        // Plan 026 (A.7): con muchos items el rail desbordaba sin affordance. Los
        // items van en una columna scrollable vertical (overflow natural del rail,
        // sin "Más"); el `header` queda fijo arriba (es slot propio del rail).
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            items.forEachIndexed { index, item ->
                // Bloqueado = visualmente atenuado + candado, pero SIGUE clickable:
                // el consumidor intercepta el tap para abrir el selector de contexto
                // faltante en vez de navegar. Por eso no usamos `enabled=false` de M3
                // (que tragaría el click); atenuamos con alpha en el modifier.
                NavigationRailItem(
                    modifier = Modifier.alpha(if (item.enabled) 1f else DisabledNavItemAlpha),
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = {
                        DSLockedNavIcon(locked = !item.enabled) {
                            Icon(
                                imageVector = if (selectedIndex == index) (item.selectedIcon ?: item.icon) else item.icon,
                                contentDescription = item.label,
                            )
                        }
                    },
                    // Etiqueta en UNA línea: si no cabe se trunca con ellipsis en vez
                    // de partir la palabra a mitad (consistente con la barra inferior).
                    label = {
                        Text(
                            item.label,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Preview(name = "DSNavigationRail - Light", showBackground = true)
@Composable
fun DSNavigationRailPreviewLight() {
    val items =
        listOf(
            DSNavigationRailItem(label = "Inicio", icon = Icons.Filled.Home),
            DSNavigationRailItem(label = "Buscar", icon = Icons.Filled.Search),
            DSNavigationRailItem(label = "Ajustes", icon = Icons.Filled.Settings),
        )
    DSTheme {
        Surface {
            DSNavigationRail(
                items = items,
                selectedIndex = 0,
                onItemSelected = {},
                header = { Text("Menu") },
            )
        }
    }
}

@Preview(name = "DSNavigationRail - Dark", showBackground = true)
@Composable
private fun DSNavigationRailPreviewDark() {
    val items =
        listOf(
            DSNavigationRailItem(label = "Inicio", icon = Icons.Filled.Home),
            DSNavigationRailItem(label = "Buscar", icon = Icons.Filled.Search),
            DSNavigationRailItem(label = "Ajustes", icon = Icons.Filled.Settings),
        )
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSNavigationRail(
                items = items,
                selectedIndex = 0,
                onItemSelected = {},
                header = { Text("Menu") },
            )
        }
    }
}
