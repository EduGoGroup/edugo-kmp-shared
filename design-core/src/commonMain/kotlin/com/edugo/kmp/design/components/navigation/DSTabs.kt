package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

data class DSTabItem(
    val label: String,
    val icon: ImageVector? = null,
)

enum class DSTabVariant { PRIMARY, SECONDARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTabs(
    tabs: List<DSTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: DSTabVariant = DSTabVariant.PRIMARY,
) {
    val tabContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(tab.label) },
                icon = tab.icon?.let { icon -> { Icon(icon, contentDescription = tab.label) } },
            )
        }
    }

    when (variant) {
        DSTabVariant.PRIMARY ->
            PrimaryTabRow(
                selectedTabIndex = selectedIndex,
                modifier = modifier,
                tabs = tabContent,
            )
        DSTabVariant.SECONDARY ->
            SecondaryTabRow(
                selectedTabIndex = selectedIndex,
                modifier = modifier,
                tabs = tabContent,
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTabs - Light", showBackground = true)
@Composable
fun DSTabsPreviewLight() {
    val tabs =
        listOf(
            DSTabItem(label = "Inicio"),
            DSTabItem(label = "Actividad"),
            DSTabItem(label = "Perfil"),
        )
    var selectedIndex by remember { mutableStateOf(0) }

    DSTheme {
        Surface {
            Column {
                DSTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it },
                    variant = DSTabVariant.PRIMARY,
                )
                Spacer(Modifier.height(Spacing.spacing3))
                DSTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it },
                    variant = DSTabVariant.SECONDARY,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTabs - Dark", showBackground = true)
@Composable
private fun DSTabsPreviewDark() {
    val tabs =
        listOf(
            DSTabItem(label = "Inicio"),
            DSTabItem(label = "Actividad"),
            DSTabItem(label = "Perfil"),
        )
    var selectedIndex by remember { mutableStateOf(0) }

    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column {
                DSTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it },
                    variant = DSTabVariant.PRIMARY,
                )
                Spacer(Modifier.height(Spacing.spacing3))
                DSTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it },
                    variant = DSTabVariant.SECONDARY,
                )
            }
        }
    }
}
