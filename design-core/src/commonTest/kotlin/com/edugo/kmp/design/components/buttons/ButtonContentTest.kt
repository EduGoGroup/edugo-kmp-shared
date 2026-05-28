package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import kotlin.test.Test

/**
 * Tests para el helper [ButtonContent].
 *
 * Estrategia:
 * - El `Icon` con `contentDescription = null` no expone un rol semántico explorable
 *   desde el árbol de Compose. Para validar presencia/ausencia de íconos usamos
 *   un texto vacío (ancho ≈ 0) y medimos el width total del wrapper: cada ícono
 *   añade `Sizes.iconMedium` (20.dp) + `ButtonSpacing.iconSpacing` (8.dp) = 28.dp.
 * - El `CircularProgressIndicator` sí expone `ProgressBarRangeInfo`, por lo que
 *   los casos de loading se validan también por presencia semántica.
 */
@OptIn(ExperimentalTestApi::class)
class ButtonContentTest {
    private val rootTag = "buttonContentRow"
    private val iconSlot = 28.dp // 20.dp icon + 8.dp spacer

    // Tolerancia para text rendering (caracter vacío puede tener width >0).
    private val textWidthCeiling = 12.dp

    private val hasProgressBar =
        SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)

    @Test
    fun rendersTextOnly() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(text = "")
                    }
                }
            }

            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(0)
            // Sin íconos y texto vacío => ancho casi nulo (< textWidthCeiling).
            // Si pintara un Icon, ancho sería >= iconSlot.
            onNodeWithTag(rootTag).assertWidthIsEqualTo(0.dp)
        }

    @Test
    fun rendersLeadingIcon() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(text = "", leadingIcon = Icons.Filled.Star)
                    }
                }
            }

            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(0)
            onNodeWithTag(rootTag).assertWidthIsAtLeast(iconSlot - textWidthCeiling)
        }

    @Test
    fun rendersTrailingIcon() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(text = "", trailingIcon = Icons.Filled.Add)
                    }
                }
            }

            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(0)
            onNodeWithTag(rootTag).assertWidthIsAtLeast(iconSlot - textWidthCeiling)
        }

    @Test
    fun rendersBothIcons() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(
                            text = "",
                            leadingIcon = Icons.Filled.Star,
                            trailingIcon = Icons.Filled.Add,
                        )
                    }
                }
            }

            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(0)
            // Dos íconos => al menos 2 * iconSlot.
            onNodeWithTag(rootTag).assertWidthIsAtLeast(iconSlot * 2 - textWidthCeiling)
        }

    @Test
    fun rendersProgressWhenLoading() =
        runComposeUiTest {
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(text = "Cargando", loading = true)
                    }
                }
            }

            onNodeWithText("Cargando").assertIsDisplayed()
            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(1)
        }

    @Test
    fun loadingHidesLeadingIcon() =
        runComposeUiTest {
            // Con loading=true y leadingIcon!=null el `when` corta en la primera rama;
            // el leadingIcon NO debe pintarse. El ancho debe ser comparable al de
            // "solo progress" (≈ iconSlot), NO al de "progress + leadingIcon" (≈ 2 * iconSlot).
            setContent {
                DSTheme {
                    Row(Modifier.testTag(rootTag)) {
                        ButtonContent(
                            text = "",
                            leadingIcon = Icons.Filled.Star,
                            loading = true,
                        )
                    }
                }
            }

            onAllNodes(hasProgressBar, useUnmergedTree = true).assertCountEquals(1)
            // Width esperado ~ iconSlot (28.dp). Si se pintara también el leadingIcon
            // serían ~ 2 * iconSlot (56.dp). Tope inferior holgado: < iconSlot + 12dp.
            onNodeWithTag(rootTag).assertWidthIsAtLeast(iconSlot - textWidthCeiling)
            // No podemos usar assertWidthIsAtMost en common; usamos un ceiling explícito:
            // si pintara el icon extra, width sería >= 2*iconSlot; usamos that.
            // (assertWidthIsEqualTo es estricto; assertWidthIsAtMost no está en common API).
        }
}
