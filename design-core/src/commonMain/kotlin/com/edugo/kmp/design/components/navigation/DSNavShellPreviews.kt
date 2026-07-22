package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Previews del shell de navegación de escritorio (plan 050 F1, task 1.7): sidebar expandida,
 * riel colapsado y flyout con subárbol profundo. Reutilizan [NavTreePreviewFixtures] (mismos
 * casos límite del árbol: profundidad 3+, 10 hijos, labels largos).
 */

@Preview(name = "DSNavSidebar - Expandida (profundidad 3+)", showBackground = true)
@Composable
private fun DSNavSidebarPreview() {
    DSTheme {
        Surface {
            DSNavSidebar(
                nodes = NavTreePreviewFixtures.deepTree,
                activeKey = "users-edit",
                expandedKeys = emptySet(),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxHeight().height(420.dp),
                header = {
                    Text(
                        "EduGo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(Spacing.spacing4),
                    )
                },
            )
        }
    }
}

@Preview(name = "DSNavSidebar - Labels largos", showBackground = true)
@Composable
private fun DSNavSidebarLongLabelsPreview() {
    DSTheme {
        Surface {
            DSNavSidebar(
                nodes = NavTreePreviewFixtures.longLabels,
                activeKey = "long-child",
                expandedKeys = setOf("long-parent"),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxHeight().height(220.dp),
            )
        }
    }
}

@Preview(name = "DSNavRail - Colapsado (iconos L1)", showBackground = true)
@Composable
private fun DSNavRailPreview() {
    DSTheme {
        Surface {
            DSNavRail(
                nodes = NavTreePreviewFixtures.deepTree,
                activeKey = "users-edit",
                expandedKeys = emptySet(),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxHeight().height(320.dp),
            )
        }
    }
}

@Preview(name = "DSNavRail - Flyout 10 hijos (scroll)", showBackground = true)
@Composable
private fun DSNavRailFlyout10Preview() {
    DSTheme {
        Surface {
            Box(modifier = Modifier.padding(Spacing.spacing4)) {
                NavRailFlyoutPanel(
                    section = NavTreePreviewFixtures.tenChildren.first(),
                    flyout = NavRailFlyoutBinding(
                        activeKey = "report-7",
                        expandedKeys = emptySet(),
                        onToggle = {},
                        onSelect = {},
                    ),
                )
            }
        }
    }
}

@Preview(name = "DSNavRail - Flyout profundidad 3+", showBackground = true)
@Composable
private fun DSNavRailFlyoutDeepPreview() {
    DSTheme {
        Surface {
            Box(modifier = Modifier.padding(Spacing.spacing4)) {
                NavRailFlyoutPanel(
                    section = NavTreePreviewFixtures.deepTree.first(), // "admin" → users(3), audit
                    flyout = NavRailFlyoutBinding(
                        activeKey = "users-edit",
                        expandedKeys = emptySet(),
                        onToggle = {},
                        onSelect = {},
                    ),
                )
            }
        }
    }
}

@Preview(name = "DSNavShell - Oscuro", showBackground = true)
@Composable
private fun DSNavShellDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSNavSidebar(
                nodes = NavTreePreviewFixtures.deepTree,
                activeKey = "users-edit",
                expandedKeys = emptySet(),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxHeight().height(420.dp),
            )
        }
    }
}
