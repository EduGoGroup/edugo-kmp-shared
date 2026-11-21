package com.edugo.kmp.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// `Breakpoint`, `breakpointFromWidth` y `ContentKind` viven en `design-tokens` (mismo paquete,
// sin Compose; deuda 131). Aquí queda solo lo que mide en `Dp`; lo entero delega en
// `ResponsiveGrid` para que haya una sola política.

/**
 * Responsive spacing presets per breakpoint.
 */
object ResponsiveSpacing {
    /** Horizontal content padding per breakpoint. */
    fun contentPadding(breakpoint: Breakpoint): Dp =
        when (breakpoint) {
            Breakpoint.COMPACT -> 16.dp
            Breakpoint.MEDIUM -> 24.dp
            Breakpoint.EXPANDED -> 32.dp
            Breakpoint.LARGE -> 40.dp
        }

    /** Gap between grid items per breakpoint. */
    fun gridGap(breakpoint: Breakpoint): Dp =
        when (breakpoint) {
            Breakpoint.COMPACT -> 8.dp
            Breakpoint.MEDIUM -> 16.dp
            Breakpoint.EXPANDED -> 24.dp
            Breakpoint.LARGE -> 32.dp
        }

    /** Recommended grid column count per breakpoint. Delega en [ResponsiveGrid.gridColumns]. */
    fun gridColumns(breakpoint: Breakpoint): Int = ResponsiveGrid.gridColumns(breakpoint)
}

/**
 * Reading-width and column policy per [Breakpoint]. Sibling of [ResponsiveSpacing] (which owns
 * padding/gaps): both read from the same canonical [Breakpoint], but [ResponsiveLayout] owns the
 * spatial dimensions (container width, columns, two-pane) while [ResponsiveSpacing] owns spacing.
 *
 * All functions are pure. See plan 016 design.md §2 / §3 (decisions D3/D4).
 */
object ResponsiveLayout {
    /**
     * Maximum readable container width per breakpoint (D3). COMPACT is [Dp.Unspecified] so content
     * goes full-bleed (with the breakpoint's content padding); wider classes cap the width and the
     * caller centers the content.
     */
    fun containerMaxWidth(breakpoint: Breakpoint): Dp =
        when (breakpoint) {
            Breakpoint.COMPACT -> Dp.Unspecified
            Breakpoint.MEDIUM -> 720.dp
            Breakpoint.EXPANDED -> 1040.dp
            Breakpoint.LARGE -> 1280.dp
        }

    /**
     * Whether the layout should use two side-by-side panes (e.g. list+detail) at this breakpoint
     * (D4). Enabled from EXPANDED upward. Delega en [ResponsiveGrid.useTwoPane].
     */
    fun useTwoPane(breakpoint: Breakpoint): Boolean = ResponsiveGrid.useTwoPane(breakpoint)

    /**
     * Number of content columns for a given [breakpoint] and [kind] (design.md §3). Delega en
     * [ResponsiveGrid.contentColumns], que documenta la política por [ContentKind].
     */
    fun contentColumns(
        breakpoint: Breakpoint,
        kind: ContentKind,
    ): Int = ResponsiveGrid.contentColumns(breakpoint, kind)
}
