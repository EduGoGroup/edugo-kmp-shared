package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class DSNavigationRailItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
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
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) (item.selectedIcon ?: item.icon) else item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
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
