package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Fixtures de árbol para previews y demos de casos límite (plan 049, maqueta
 * `Nav Arbol Extremo`). `internal` para que los tests del módulo los reutilicen.
 */
internal object NavTreePreviewFixtures {

    /** Profundidad 3+: Administración › Usuarios › Editar. */
    val deepTree: List<NavTreeNode> = listOf(
        NavTreeNode(
            key = "admin",
            label = "Administración",
            icon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings,
            children = listOf(
                NavTreeNode(
                    key = "users",
                    label = "Usuarios",
                    icon = Icons.Outlined.Person,
                    children = listOf(
                        NavTreeNode(key = "users-list", label = "Listado"),
                        NavTreeNode(key = "users-edit", label = "Editar usuario"),
                        NavTreeNode(key = "users-roles", label = "Roles y permisos"),
                    ),
                ),
                NavTreeNode(key = "audit", label = "Auditoría"),
            ),
        ),
        NavTreeNode(key = "dashboard", label = "Panel"),
    )

    /** Sección con 10 hijos: debe scrollear sin partición "Más". */
    val tenChildren: List<NavTreeNode> = listOf(
        NavTreeNode(
            key = "reports",
            label = "Reportes",
            icon = Icons.Outlined.Folder,
            selectedIcon = Icons.Filled.Folder,
            children = (1..10).map { NavTreeNode(key = "report-$it", label = "Reporte $it") },
        ),
    )

    /** Labels largos: elipsis AL FINAL (nunca "Administra…" en medio). */
    val longLabels: List<NavTreeNode> = listOf(
        NavTreeNode(
            key = "long-parent",
            label = "Administración de unidades académicas y colegios federados",
            icon = Icons.Outlined.Settings,
            children = listOf(
                NavTreeNode(
                    key = "long-child",
                    label = "Configuración avanzada de permisos por rol y por unidad organizativa",
                ),
            ),
        ),
    )
}

@Preview(name = "DSNavTree - Profundidad 3+", showBackground = true)
@Composable
private fun DSNavTreeDeepPreview() {
    DSTheme {
        Surface {
            DSNavTree(
                nodes = NavTreePreviewFixtures.deepTree,
                activeKey = "users-edit",
                expandedKeys = emptySet(), // los ancestros (admin, users) se revelan solos
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxWidth().height(320.dp),
            )
        }
    }
}

@Preview(name = "DSNavTree - 10 hijos (scroll)", showBackground = true)
@Composable
private fun DSNavTreeTenChildrenPreview() {
    DSTheme {
        Surface {
            DSNavTree(
                nodes = NavTreePreviewFixtures.tenChildren,
                activeKey = "report-7",
                expandedKeys = setOf("reports"),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )
        }
    }
}

@Preview(name = "DSNavTree - Labels largos (elipsis)", showBackground = true)
@Composable
private fun DSNavTreeLongLabelsPreview() {
    DSTheme {
        Surface {
            DSNavTree(
                nodes = NavTreePreviewFixtures.longLabels,
                activeKey = "long-child",
                expandedKeys = setOf("long-parent"),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
        }
    }
}

@Preview(name = "DSNavTree - Oscuro", showBackground = true)
@Composable
private fun DSNavTreeDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSNavTree(
                nodes = NavTreePreviewFixtures.deepTree,
                activeKey = "users-edit",
                expandedKeys = emptySet(),
                onToggle = {},
                onSelect = {},
                modifier = Modifier.fillMaxWidth().height(320.dp),
            )
        }
    }
}
