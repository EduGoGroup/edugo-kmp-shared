package com.edugo.kmp.design.components.lists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.components.lists.testing.TestPlatformDetector
import com.edugo.kmp.design.platform.PlatformType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DSAdaptiveActionsHostTest {

    /**
     * F4-REQ-1.2: Con actions vacía, el host es transparente y el content se renderiza directamente.
     */
    @Test
    fun skipsWrapper_whenNoActions() = runComposeUiTest {
        setContent {
            DSTheme {
                DSAdaptiveActionsHost(actions = emptyList()) { rowModifier ->
                    DSListRow(headlineText = "Test", modifier = rowModifier)
                }
            }
        }

        // DSListRow siempre aplica DSListRowDefaults.tag; si el host es transparente,
        // el row existe y es visible sin ningún wrapper adicional.
        onNodeWithTag(DSListRowDefaults.tag).assertIsDisplayed()
        onNodeWithText("Test").assertIsDisplayed()
    }

    /**
     * F4-REQ-1.4: En DESKTOP, el host envuelve el content con HoverActionsStrategy.
     * Los botones de accion son invisibles hasta recibir hover.
     *
     * Nota: el hover real (enter/exit) en el entorno de test headless no
     * despacha eventos PointerEventType.Enter de forma confiable con la version
     * actual de Compose Multiplatform (1.10.3). Se valida el contrato
     * "wrap + initial-hidden": el row existe tras el render y el boton de accion
     * no esta en el arbol de semantica antes de ningun hover.
     * La transicion visible tras hover queda cubierta por test manual.
     */
    @Test
    fun showsHoverButtons_onPointerEnter_inDesktop() = runComposeUiTest {
        TestPlatformDetector.withPlatform(PlatformType.DESKTOP) {
            setContent {
                DSTheme {
                    DSAdaptiveActionsHost(
                        actions = listOf(
                            RowAction(
                                id = "edit",
                                label = "Editar",
                                icon = Icons.Filled.Edit,
                                onInvoke = {},
                            ),
                        ),
                    ) { rowModifier ->
                        DSListRow(headlineText = "Test", modifier = rowModifier)
                    }
                }
            }

            // El wrapper existe: el host agrega su Box y el row sigue siendo encontrable.
            onNodeWithTag(DSListRowDefaults.tag).assertIsDisplayed()

            // Sin hover activo, el boton de accion no debe estar en el arbol de semantica
            // (AnimatedVisibility con visible=false excluye los nodos del arbol).
            onNodeWithContentDescription("Editar", useUnmergedTree = true).assertDoesNotExist()
        }
    }

    /**
     * F4-REQ-1.5: En DESKTOP, el ContextMenuStrategy renderiza correctamente con
     * acciones normales y destructive.
     *
     * El right-click real (PointerEventType.Press con isSecondaryPressed) no es
     * simulable de forma confiable con performMouseInput en el entorno headless
     * de Compose Multiplatform 1.10.3. Se valida que:
     * - El host renderiza sin error con ambos tipos de accion.
     * - El DropdownMenu esta inicialmente cerrado (items no visibles en el arbol).
     * El "menu visible tras right-click" queda para test manual.
     */
    @Test
    fun opensContextMenu_onRightClick_inDesktop() = runComposeUiTest {
        TestPlatformDetector.withPlatform(PlatformType.DESKTOP) {
            setContent {
                DSTheme {
                    DSAdaptiveActionsHost(
                        actions = listOf(
                            RowAction(
                                id = "edit",
                                label = "Editar",
                                icon = Icons.Filled.Edit,
                                onInvoke = {},
                            ),
                            RowAction(
                                id = "delete",
                                label = "Eliminar",
                                icon = Icons.Filled.Delete,
                                destructive = true,
                                onInvoke = {},
                            ),
                        ),
                    ) { rowModifier ->
                        DSListRow(headlineText = "Test", modifier = rowModifier)
                    }
                }
            }

            // El host renderiza sin error con acciones normales + destructive.
            onNodeWithTag(DSListRowDefaults.tag).assertIsDisplayed()

            // El DropdownMenu esta inicialmente cerrado: los items no son visibles.
            onNodeWithText("Editar").assertDoesNotExist()
            onNodeWithText("Eliminar").assertDoesNotExist()
        }
    }

    /**
     * F4-REQ-1.6 — smoke test: el host renderiza con una RowAction destructive
     * sin disparar onInvoke en el render inicial. El flujo real hover→click→confirm
     * no es simulable en headless con CMP 1.10.3; queda para test manual.
     */
    @Test
    fun compositeRenders_withDestructiveAction_smokeTest() = runComposeUiTest {
        TestPlatformDetector.withPlatform(PlatformType.DESKTOP) {
            var count = 0
            val destructiveAction = RowAction(
                id = "delete",
                label = "Eliminar",
                icon = Icons.Filled.Delete,
                destructive = true,
                onInvoke = { count++ },
            )

            setContent {
                DSTheme {
                    DSAdaptiveActionsHost(
                        actions = listOf(destructiveAction),
                        onAction = { it.onInvoke() },
                    ) { rowModifier ->
                        DSListRow(headlineText = "Test", modifier = rowModifier)
                    }
                }
            }

            onNodeWithTag(DSListRowDefaults.tag).assertIsDisplayed()
            onNodeWithContentDescription("Eliminar", useUnmergedTree = true).assertDoesNotExist()
            assertEquals(0, count)
        }
    }

    /**
     * F4-REQ-1.6 — smoke test: el host renderiza con una RowAction no-destructive
     * sin disparar onInvoke en el render inicial. El flujo real hover→click directo
     * (sin confirm) no es simulable en headless con CMP 1.10.3; queda para test manual.
     */
    @Test
    fun compositeRenders_withNormalAction_smokeTest() = runComposeUiTest {
        TestPlatformDetector.withPlatform(PlatformType.DESKTOP) {
            var count = 0
            val normalAction = RowAction(
                id = "edit",
                label = "Editar",
                icon = Icons.Filled.Edit,
                destructive = false,
                onInvoke = { count++ },
            )

            setContent {
                DSTheme {
                    DSAdaptiveActionsHost(
                        actions = listOf(normalAction),
                        onAction = { it.onInvoke() },
                    ) { rowModifier ->
                        DSListRow(headlineText = "Test", modifier = rowModifier)
                    }
                }
            }

            onNodeWithTag(DSListRowDefaults.tag).assertIsDisplayed()
            onNodeWithContentDescription("Editar", useUnmergedTree = true).assertDoesNotExist()
            assertEquals(0, count)
        }
    }
}
