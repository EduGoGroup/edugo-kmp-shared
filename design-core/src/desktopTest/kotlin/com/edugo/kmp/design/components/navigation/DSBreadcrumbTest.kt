package com.edugo.kmp.design.components.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.edugo.kmp.design.DSTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests del breadcrumb neutral (plan 050 F2, task 2.1/2.4).
 *
 * El entorno headless de Compose Multiplatform 1.10.x no simula hover/foco de forma
 * confiable; se validan el contrato de render (tramos visibles) y la navegación al pulsar
 * un ANCESTRO clicable, más la NO-navegación del tramo actual.
 */
@OptIn(ExperimentalTestApi::class)
class DSBreadcrumbTest {

    private fun deepTrail(onAncestor: (String) -> Unit) = listOf(
        DSBreadcrumbItem(label = "Administración", onClick = { onAncestor("admin") }),
        DSBreadcrumbItem(label = "Usuarios", onClick = { onAncestor("users") }),
        DSBreadcrumbItem(label = "Editar: Miguel Castro", onClick = null),
    )

    @Test
    fun rendersAllSegments() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    DSBreadcrumb(items = deepTrail(onAncestor = {}))
                }
            }
            onNodeWithText("Administración").assertIsDisplayed()
            onNodeWithText("Usuarios").assertIsDisplayed()
            onNodeWithText("Editar: Miguel Castro").assertIsDisplayed()
        }

    @Test
    fun clickingAncestorNavigates() =
        runComposeUiTest {
            var navigatedTo: String? = null
            setContent {
                DSTheme {
                    DSBreadcrumb(items = deepTrail(onAncestor = { navigatedTo = it }))
                }
            }
            onNodeWithText("Administración").performClick()
            assertEquals("admin", navigatedTo)
        }

    @Test
    fun currentSegmentDoesNotNavigate() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                DSTheme {
                    DSBreadcrumb(
                        items = listOf(
                            DSBreadcrumbItem(label = "Usuarios", onClick = { clicks++ }),
                            // Último tramo con onClick no nulo: la POSICIÓN manda, no debe navegar.
                            DSBreadcrumbItem(label = "Editar: Miguel Castro", onClick = { clicks++ }),
                        ),
                    )
                }
            }
            onNodeWithText("Editar: Miguel Castro").performClick()
            assertEquals(0, clicks)
        }
}
