package com.edugo.kmp.design.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Item de la barra de navegación inferior.
 *
 * @param enabled `false` atenúa el item y lo deja sin click; el resolver de la
 *   app lo usa para bloquear pantallas que necesitan un contexto (colegio/unidad)
 *   que todavía no está activo. Cuando está bloqueado se superpone el indicador de
 *   candado vía [DSLockedNavIcon].
 */
data class DSNavigationBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun DSBottomNavigationBar(
    items: List<DSNavigationBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // Insets aplicados por la barra. Por defecto los de M3 (incluye el safe-area
    // inferior). Cuando la barra se apila SOBRE otro pie que ya asume ese inset
    // (p. ej. ContextStatusBar), el consumidor pasa `WindowInsets(0)` para que la
    // nav se pegue al pie sin dejar un hueco muerto intermedio.
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
) {
    NavigationBar(modifier = modifier, windowInsets = windowInsets) {
        items.forEachIndexed { index, item ->
            // Bloqueado = atenuado + candado, pero SIGUE clickable: el consumidor
            // intercepta el tap para abrir el selector de contexto faltante en vez
            // de navegar. No usamos `enabled=false` de M3 (tragaría el click).
            NavigationBarItem(
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
                // Etiqueta en UNA línea: si no cabe ("Administración") se trunca con
                // ellipsis ("Administ…") en vez de partir la palabra en dos renglones.
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

@Preview(name = "DSBottomNavigationBar - Light", showBackground = true)
@Composable
fun DSBottomNavigationBarPreviewLight() {
    val items =
        listOf(
            DSNavigationBarItem(label = "Inicio", icon = Icons.Filled.Home),
            DSNavigationBarItem(label = "Buscar", icon = Icons.Filled.Search),
            DSNavigationBarItem(label = "Perfil", icon = Icons.Filled.Person),
        )
    DSTheme {
        Surface {
            DSBottomNavigationBar(
                items = items,
                selectedIndex = 0,
                onItemSelected = {},
            )
        }
    }
}

@Preview(name = "DSBottomNavigationBar - Dark", showBackground = true)
@Composable
private fun DSBottomNavigationBarPreviewDark() {
    val items =
        listOf(
            DSNavigationBarItem(label = "Inicio", icon = Icons.Filled.Home),
            DSNavigationBarItem(label = "Buscar", icon = Icons.Filled.Search),
            DSNavigationBarItem(label = "Perfil", icon = Icons.Filled.Person),
        )
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSBottomNavigationBar(
                items = items,
                selectedIndex = 0,
                onItemSelected = {},
            )
        }
    }
}
