package com.edugo.kmp.design.components.progress

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * El parámetro `size` de [DSCircularProgress] se aplica. Hasta el 2026-11-23 no se aplicaba: todos los
 * anillos salían con los 40dp por defecto de Material 3, y el del resultado del examen de `edugo-ui-kmp`
 * quedaba debajo de su porcentaje (fase 4 del MVP-KMP). Un `Modifier.size` del que llama sigue ganando.
 */
@OptIn(ExperimentalTestApi::class)
class DSCircularProgressSizeTest {
    private fun measure(content: @Composable (Modifier) -> Unit): Pair<Dp, Dp> {
        var result = 0.dp to 0.dp
        runComposeUiTest {
            setContent { DSTheme { content(Modifier.testTag(TAG)) } }
            val bounds = onNodeWithTag(TAG).getUnclippedBoundsInRoot()
            result = bounds.width to bounds.height
        }
        return result
    }

    @Test
    fun determinateRingTakesTheRequestedSize() {
        val (width, height) =
            measure { modifier ->
                DSCircularProgress(modifier = modifier, progress = { 0.75f }, size = 120.dp)
            }
        assertEquals(120.dp, width)
        assertEquals(120.dp, height)
    }

    @Test
    fun indeterminateRingTakesTheRequestedSize() {
        val (width, height) =
            measure { modifier ->
                DSCircularProgress(modifier = modifier, size = 20.dp)
            }
        assertEquals(20.dp, width)
        assertEquals(20.dp, height)
    }

    @Test
    fun withoutSizeTheRingUsesTheProgressLargeToken() {
        val (width, _) =
            measure { modifier ->
                DSCircularProgress(modifier = modifier)
            }
        assertEquals(Sizes.progressLarge, width)
    }

    @Test
    fun callerSizeModifierWinsOverTheSizeParameter() {
        val (width, _) =
            measure { modifier ->
                DSCircularProgress(modifier = modifier.size(16.dp), size = 120.dp)
            }
        assertEquals(16.dp, width)
    }

    private companion object {
        const val TAG = "ds-circular-progress"
    }
}
