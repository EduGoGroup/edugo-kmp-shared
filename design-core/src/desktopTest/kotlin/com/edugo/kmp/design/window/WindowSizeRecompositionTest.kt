package com.edugo.kmp.design.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.Breakpoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Contrato de recomposición de [ProvideWindowSize] (bug 0086, F2).
 *
 * Lo que se protege aquí NO se ve en un test de valores: con `staticCompositionLocalOf` Compose no
 * rastrea lectores y **cada dp** de arrastre invalidaba el `content` completo del proveedor (la app
 * entera). Estos tests miden **quién se recompone**, que es justo el efecto que un gate verde de
 * compilación deja pasar.
 *
 * Se prueba en `desktopTest` porque `runComposeUiTest` da un motor de composición real.
 */
@OptIn(ExperimentalTestApi::class)
class WindowSizeRecompositionTest {
    /**
     * Arrastre dentro de una misma banda (900 → 950 dp, ambos EXPANDED): solo el lector de dp
     * crudos se recompone. Ni el lector de clase ni un composable ajeno pagan el arrastre.
     */
    @Test
    fun dragInsideSameClass_recomposesOnlyRawDpReader() =
        runComposeUiTest {
            Recompositions.reset()
            var widthDp by mutableStateOf(WIDTH_EXPANDED_LOW)

            setContent {
                Box(Modifier.size(widthDp.dp, WINDOW_HEIGHT.dp)) {
                    ProvideWindowSize {
                        RawDpReader()
                        WidthClassReader()
                        NonReader()
                    }
                }
            }
            waitForIdle()

            val baseRawDp = Recompositions.rawDpReader
            val baseWidthClass = Recompositions.widthClassReader
            val baseNonReader = Recompositions.nonReader
            assertEquals(WIDTH_EXPANDED_LOW, Recompositions.lastWidthDp)
            assertEquals(Breakpoint.EXPANDED, Recompositions.lastWidthClass)

            widthDp = WIDTH_EXPANDED_HIGH
            waitForIdle()

            assertTrue(
                Recompositions.rawDpReader > baseRawDp,
                "el lector de dp crudos debe ver el ancho nuevo",
            )
            assertEquals(WIDTH_EXPANDED_HIGH, Recompositions.lastWidthDp)
            assertEquals(
                baseWidthClass,
                Recompositions.widthClassReader,
                "leer solo la clase no debe recomponer dentro de la misma banda",
            )
            assertEquals(
                baseNonReader,
                Recompositions.nonReader,
                "quien no lee ningún local no debe recomponerse (esto era lo que rompía el static)",
            )
        }

    /**
     * Cruce de umbral (900 → 700 dp, EXPANDED → MEDIUM): el lector de clase sí se recompone —
     * el local derivado no se queda pegado— y el ajeno sigue sin pagar nada.
     */
    @Test
    fun crossingThreshold_recomposesWidthClassReader() =
        runComposeUiTest {
            Recompositions.reset()
            var widthDp by mutableStateOf(WIDTH_EXPANDED_LOW)

            setContent {
                Box(Modifier.size(widthDp.dp, WINDOW_HEIGHT.dp)) {
                    ProvideWindowSize {
                        RawDpReader()
                        WidthClassReader()
                        NonReader()
                    }
                }
            }
            waitForIdle()

            val baseWidthClass = Recompositions.widthClassReader
            val baseNonReader = Recompositions.nonReader

            widthDp = WIDTH_MEDIUM
            waitForIdle()

            assertTrue(
                Recompositions.widthClassReader > baseWidthClass,
                "cruzar el umbral debe recomponer al lector de clase",
            )
            assertEquals(Breakpoint.MEDIUM, Recompositions.lastWidthClass)
            assertEquals(
                baseNonReader,
                Recompositions.nonReader,
                "quien no lee ningún local no debe recomponerse ni al cruzar umbral",
            )
        }

    /**
     * El overload con [WindowSize] explícito (tests/hosts que ya saben el tamaño) deja los DOS
     * locales provistos y coherentes entre sí. Proveer solo uno a mano dejaría al otro sin valor.
     */
    @Test
    fun explicitWindowSize_providesBothLocals() =
        runComposeUiTest {
            Recompositions.reset()

            setContent {
                ProvideWindowSize(
                    WindowSize(WIDTH_MEDIUM, WINDOW_HEIGHT, Orientation.LANDSCAPE),
                ) {
                    RawDpReader()
                    WidthClassReader()
                }
            }
            waitForIdle()

            assertEquals(WIDTH_MEDIUM, Recompositions.lastWidthDp)
            assertEquals(Breakpoint.MEDIUM, Recompositions.lastWidthClass)
        }
}

private const val WINDOW_HEIGHT = 600
private const val WIDTH_EXPANDED_LOW = 900
private const val WIDTH_EXPANDED_HIGH = 950
private const val WIDTH_MEDIUM = 700

/** Contadores de recomposición por tipo de lector. Sin parámetros: los composables son skippables. */
private object Recompositions {
    var rawDpReader = 0
    var widthClassReader = 0
    var nonReader = 0
    var lastWidthDp = 0
    var lastWidthClass: Breakpoint? = null

    fun reset() {
        rawDpReader = 0
        widthClassReader = 0
        nonReader = 0
        lastWidthDp = 0
        lastWidthClass = null
    }
}

@Composable
private fun RawDpReader() {
    Recompositions.rawDpReader++
    Recompositions.lastWidthDp = LocalWindowSize.current.widthDp
    Box(Modifier.size(1.dp))
}

@Composable
private fun WidthClassReader() {
    Recompositions.widthClassReader++
    Recompositions.lastWidthClass = LocalWindowWidthClass.current
    Box(Modifier.size(1.dp))
}

@Composable
private fun NonReader() {
    Recompositions.nonReader++
    Box(Modifier.size(1.dp))
}
