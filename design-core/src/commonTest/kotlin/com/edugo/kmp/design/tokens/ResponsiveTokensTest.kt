package com.edugo.kmp.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * La API en `Dp` de design-core se conserva tras sacar lo puro a design-tokens (deuda 131): los
 * miembros enteros delegan en [ResponsiveGrid] y los anchos siguen con sus valores.
 */
class ResponsiveTokensTest {
    @Test
    fun integerMembersDelegateToResponsiveGrid() {
        Breakpoint.entries.forEach { breakpoint ->
            assertEquals(ResponsiveGrid.gridColumns(breakpoint), ResponsiveSpacing.gridColumns(breakpoint))
            assertEquals(ResponsiveGrid.useTwoPane(breakpoint), ResponsiveLayout.useTwoPane(breakpoint))
            ContentKind.entries.forEach { kind ->
                assertEquals(
                    ResponsiveGrid.contentColumns(breakpoint, kind),
                    ResponsiveLayout.contentColumns(breakpoint, kind),
                )
            }
        }
    }

    @Test
    fun containerMaxWidthKeepsItsValues() {
        assertEquals(Dp.Unspecified, ResponsiveLayout.containerMaxWidth(Breakpoint.COMPACT))
        assertEquals(720.dp, ResponsiveLayout.containerMaxWidth(Breakpoint.MEDIUM))
        assertEquals(1040.dp, ResponsiveLayout.containerMaxWidth(Breakpoint.EXPANDED))
        assertEquals(1280.dp, ResponsiveLayout.containerMaxWidth(Breakpoint.LARGE))
    }

    @Test
    fun spacingKeepsItsValues() {
        val padding = Breakpoint.entries.map { ResponsiveSpacing.contentPadding(it) }
        val gap = Breakpoint.entries.map { ResponsiveSpacing.gridGap(it) }
        assertEquals(listOf(16.dp, 24.dp, 32.dp, 40.dp), padding)
        assertEquals(listOf(8.dp, 16.dp, 24.dp, 32.dp), gap)
    }
}
