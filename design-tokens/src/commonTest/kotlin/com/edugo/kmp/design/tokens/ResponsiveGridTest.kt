package com.edugo.kmp.design.tokens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponsiveGridTest {
    @Test
    fun breakpointFromWidthUsesMaterialThresholds() {
        assertEquals(Breakpoint.COMPACT, breakpointFromWidth(0))
        assertEquals(Breakpoint.COMPACT, breakpointFromWidth(599))
        assertEquals(Breakpoint.MEDIUM, breakpointFromWidth(600))
        assertEquals(Breakpoint.MEDIUM, breakpointFromWidth(839))
        assertEquals(Breakpoint.EXPANDED, breakpointFromWidth(840))
        assertEquals(Breakpoint.EXPANDED, breakpointFromWidth(1199))
        assertEquals(Breakpoint.LARGE, breakpointFromWidth(1200))
    }

    @Test
    fun gridColumnsGrowWithTheBreakpoint() {
        assertEquals(
            listOf(1, 2, 3, 4),
            Breakpoint.entries.map { ResponsiveGrid.gridColumns(it) },
        )
    }

    @Test
    fun twoPaneStartsAtExpanded() {
        assertFalse(ResponsiveGrid.useTwoPane(Breakpoint.COMPACT))
        assertFalse(ResponsiveGrid.useTwoPane(Breakpoint.MEDIUM))
        assertTrue(ResponsiveGrid.useTwoPane(Breakpoint.EXPANDED))
        assertTrue(ResponsiveGrid.useTwoPane(Breakpoint.LARGE))
    }

    @Test
    fun contentColumnsFollowTheKind() {
        val formLike = listOf(1, 1, 2, 2)
        assertEquals(formLike, Breakpoint.entries.map { ResponsiveGrid.contentColumns(it, ContentKind.FORM) })
        assertEquals(formLike, Breakpoint.entries.map { ResponsiveGrid.contentColumns(it, ContentKind.SETTINGS) })
        assertEquals(List(4) { 1 }, Breakpoint.entries.map { ResponsiveGrid.contentColumns(it, ContentKind.LIST) })
        assertEquals(List(4) { 1 }, Breakpoint.entries.map { ResponsiveGrid.contentColumns(it, ContentKind.DETAIL) })
        assertEquals(
            Breakpoint.entries.map { ResponsiveGrid.gridColumns(it) },
            Breakpoint.entries.map { ResponsiveGrid.contentColumns(it, ContentKind.DASHBOARD) },
        )
    }

    @Test
    fun semanticToneFallbackMapsKnownStatuses() {
        assertEquals(SemanticTone.Success, SemanticTone.fromStatus(" Active "))
        assertEquals(SemanticTone.Warning, SemanticTone.fromStatus("pendiente"))
        assertEquals(SemanticTone.Danger, SemanticTone.fromStatus("failed"))
        assertEquals(SemanticTone.Info, SemanticTone.fromStatus("notice"))
        assertEquals(SemanticTone.Neutral, SemanticTone.fromStatus("whatever"))
    }
}
