package com.edugo.kmp.design.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.edugo.kmp.design.DSTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests de UI del shell de navegación de escritorio (plan 050 F1): sidebar expandida y riel
 * colapsado + flyout. Verifican el contrato del renderer ÚNICO ([DSNavTree]) reusado en ambos.
 */
@OptIn(ExperimentalTestApi::class)
class DSNavShellTest {

    private val deep = listOf(
        NavTreeNode(
            key = "admin",
            label = "Administración",
            icon = Icons.Outlined.Settings,
            children = listOf(
                NavTreeNode(
                    key = "users",
                    label = "Usuarios",
                    children = listOf(
                        NavTreeNode(key = "users-list", label = "Listado"),
                        NavTreeNode(key = "users-edit", label = "Editar"),
                    ),
                ),
            ),
        ),
        NavTreeNode(key = "dashboard", label = "Panel"),
    )

    @Test
    fun sidebarRendersTreeAndSelectsLeaf() = runComposeUiTest {
        var selected: String? = null
        setContent {
            DSTheme {
                DSNavSidebar(
                    nodes = deep,
                    activeKey = "users-edit",
                    expandedKeys = emptySet(), // ancestros (admin, users) se revelan solos
                    onToggle = {},
                    onSelect = { selected = it.key },
                )
            }
        }
        // El activo profundo revela su rama: la hoja "Editar" está visible.
        onNodeWithText("Editar").assertIsDisplayed()
        onNodeWithText("Editar").performClick()
        assertEquals("users-edit", selected)
    }

    @Test
    fun sidebarBranchTogglesInsteadOfSelecting() = runComposeUiTest {
        var toggled: String? = null
        var selected: String? = null
        setContent {
            DSTheme {
                DSNavSidebar(
                    nodes = deep,
                    activeKey = null,
                    expandedKeys = emptySet(),
                    onToggle = { toggled = it },
                    onSelect = { selected = it.key },
                )
            }
        }
        onNodeWithText("Administración").performClick()
        assertEquals("admin", toggled)
        assertNull(selected) // una rama nunca selecciona
    }

    @Test
    fun railFlyoutOpensSubtreeAndSelectsLeaf() = runComposeUiTest {
        var selected: String? = null
        setContent {
            DSTheme {
                DSNavRail(
                    nodes = deep,
                    activeKey = null,
                    expandedKeys = setOf("users"), // subárbol expandido dentro del flyout
                    onToggle = {},
                    onSelect = { selected = it.key },
                )
            }
        }
        // Cerrado: las hojas del subárbol NO están montadas todavía.
        onNodeWithText("Editar").assertDoesNotExist()
        // Clic en la sección L1 (icono, contentDescription = label) → abre el flyout.
        // El icono puede compartir descripción con el título del flyout, por eso tomamos el
        // primer nodo con esa descripción (el propio icono del riel).
        onAllNodesWithContentDescription("Administración")[0].performClick()
        onNodeWithText("Editar").assertIsDisplayed()
        onNodeWithText("Editar").performClick()
        assertEquals("users-edit", selected)
    }
}
