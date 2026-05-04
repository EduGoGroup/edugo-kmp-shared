package com.edugo.kmp.design.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class DSNavigationBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badge: String? = null,
)

@Composable
fun DSBottomNavigationBar(
    items: List<DSNavigationBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
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
