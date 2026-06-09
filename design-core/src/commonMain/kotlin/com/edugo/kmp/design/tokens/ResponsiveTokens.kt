package com.edugo.kmp.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive breakpoints for adapting layouts across device sizes.
 *
 * Based on Material Design 3 window size classes:
 * - [COMPACT]: phones in portrait (~0-599dp)
 * - [MEDIUM]: tablets, phones in landscape (~600-839dp)
 * - [EXPANDED]: desktop, large tablets (~840-1199dp)
 * - [LARGE]: ultra-wide desktop, large monitors (~1200dp+)
 */
enum class Breakpoint(
    val minWidthDp: Int,
) {
    COMPACT(0),
    MEDIUM(600),
    EXPANDED(840),
    LARGE(1200),
}

/**
 * Resolves the [Breakpoint] for the given screen [widthDp].
 */
fun breakpointFromWidth(widthDp: Int): Breakpoint =
    when {
        widthDp >= Breakpoint.LARGE.minWidthDp -> Breakpoint.LARGE
        widthDp >= Breakpoint.EXPANDED.minWidthDp -> Breakpoint.EXPANDED
        widthDp >= Breakpoint.MEDIUM.minWidthDp -> Breakpoint.MEDIUM
        else -> Breakpoint.COMPACT
    }

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

    /** Recommended grid column count per breakpoint. */
    fun gridColumns(breakpoint: Breakpoint): Int =
        when (breakpoint) {
            Breakpoint.COMPACT -> 1
            Breakpoint.MEDIUM -> 2
            Breakpoint.EXPANDED -> 3
            Breakpoint.LARGE -> 4
        }
}

/**
 * Kind of content a screen renders. Drives column counts and two-pane decisions in
 * [ResponsiveLayout]. Intentionally semantic (no pixels), so the contract only needs to express
 * intention while the front decides the spatial realization per breakpoint.
 */
enum class ContentKind {
    FORM,
    LIST,
    DETAIL,
    DASHBOARD,
    SETTINGS,
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
     * (D4). Enabled from EXPANDED upward.
     */
    fun useTwoPane(breakpoint: Breakpoint): Boolean =
        breakpoint == Breakpoint.EXPANDED || breakpoint == Breakpoint.LARGE

    /**
     * Number of content columns for a given [breakpoint] and [kind] (design.md §3).
     *
     * FORM/SETTINGS: 1 column until EXPANDED, where fields pack into 2 columns.
     * LIST/DETAIL: a single content column; their EXPANDED richness is two *panes*
     *   (see [useTwoPane]), not more columns inside a pane.
     * DASHBOARD: tracks [ResponsiveSpacing.gridColumns] (1/2/3/4) — the metric grid grows with width.
     */
    fun contentColumns(
        breakpoint: Breakpoint,
        kind: ContentKind,
    ): Int =
        when (kind) {
            ContentKind.FORM,
            ContentKind.SETTINGS,
            ->
                when (breakpoint) {
                    Breakpoint.COMPACT -> 1
                    Breakpoint.MEDIUM -> 1
                    Breakpoint.EXPANDED -> 2
                    Breakpoint.LARGE -> 2
                }

            ContentKind.LIST,
            ContentKind.DETAIL,
            -> 1

            ContentKind.DASHBOARD -> ResponsiveSpacing.gridColumns(breakpoint)
        }
}
