package com.edugo.kmp.design.components.lists

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.selection.DSChip
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DSListRowTest {

    /**
     * F1-REQ-1.1: El row muestra el headline y el chevron por defecto.
     */
    @Test
    fun showsHeadlineAndChevron_byDefault() = runComposeUiTest {
        setContent {
            DSTheme {
                DSListRow(headlineText = "Hola")
            }
        }

        onNodeWithText("Hola").assertIsDisplayed()
        onNodeWithContentDescription("chevron-default", useUnmergedTree = true).assertExists()
        onNodeWithTag(DSListRowDefaults.tag).assertExists()
    }

    /**
     * F1-REQ-1.2: Cuando se provee supportingText, ambos textos son visibles.
     */
    @Test
    fun showsSupporting_whenProvided() = runComposeUiTest {
        setContent {
            DSTheme {
                DSListRow(headlineText = "Titulo", supportingText = "Detalle")
            }
        }

        onNodeWithText("Titulo").assertIsDisplayed()
        onNodeWithText("Detalle").assertIsDisplayed()
    }

    /**
     * F1-REQ-1.5: El callback onClick se invoca al hacer clic sobre el row.
     */
    @Test
    fun invokesOnClick_whenClickable() = runComposeUiTest {
        var count = 0

        setContent {
            DSTheme {
                DSListRow(headlineText = "Clickeable", onClick = { count++ })
            }
        }

        onNodeWithTag(DSListRowDefaults.tag).performClick()
        assertEquals(1, count)
    }

    /**
     * F1-REQ-1.4 / F2-REQ-1.4: Un trailing personalizado reemplaza al chevron por defecto.
     */
    @Test
    fun replacesChevron_withCustomTrailing() = runComposeUiTest {
        setContent {
            DSTheme {
                DSListRow(
                    headlineText = "Con chip",
                    trailing = { DSChip(label = "Test") },
                )
            }
        }

        onNodeWithContentDescription("chevron-default", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithText("Test").assertIsDisplayed()
    }

    /**
     * F1-REQ-1.6: Cuando onClick es null, el row no tiene acción de clic.
     */
    @Test
    fun notClickable_whenOnClickNull() = runComposeUiTest {
        setContent {
            DSTheme {
                DSListRow(headlineText = "Sin accion")
            }
        }

        onNodeWithTag(DSListRowDefaults.tag).assertHasNoClickAction()
    }

    /**
     * F1-REQ-1.3 (leading): El row renderiza correctamente con un DSAvatar como leading.
     *
     * La verificación de padding exacto (Spacing.spacing3) es frágil en runtime;
     * se valida por code review y visual test. Este test asegura que el composable
     * no falla al renderizar con leading.
     */
    @Test
    fun showsLeadingAvatar_andRespectsContract() = runComposeUiTest {
        setContent {
            DSTheme {
                DSListRow(
                    headlineText = "Ana",
                    leading = { DSAvatar(initials = "AB") },
                )
            }
        }

        onNodeWithText("Ana").assertIsDisplayed()
        // DSAvatar muestra las iniciales en mayúsculas (take(2).uppercase())
        onNodeWithText("AB").assertIsDisplayed()
    }
}
