package com.edugo.kmp.design.tokens

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
    MEDIUM(MEDIUM_MIN_WIDTH_DP),
    EXPANDED(EXPANDED_MIN_WIDTH_DP),
    LARGE(LARGE_MIN_WIDTH_DP),
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
 * Kind of content a screen renders. Drives column counts and two-pane decisions in
 * [ResponsiveGrid]. Intentionally semantic (no pixels), so the contract only needs to express
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
 * Política de columnas y de dos paneles por [Breakpoint], sin Compose: solo cuenta columnas y
 * decide paneles; no mide nada en `Dp`. Es la fuente única de esas decisiones: en `design-core`,
 * `ResponsiveLayout` (anchos de contenedor en `Dp`) y `ResponsiveSpacing` (márgenes y huecos en
 * `Dp`) delegan aquí sus miembros enteros, de modo que la lógica que no pinta (el motor SDUI) usa
 * esta misma política sin depender de Compose.
 *
 * Todas las funciones son puras (plan 016, design.md §2 / §3, decisiones D3/D4/D6).
 */
object ResponsiveGrid {
    /** Columnas recomendadas de una rejilla por [Breakpoint] (1/2/3/4). */
    fun gridColumns(breakpoint: Breakpoint): Int =
        when (breakpoint) {
            Breakpoint.COMPACT -> 1
            Breakpoint.MEDIUM -> 2
            Breakpoint.EXPANDED -> GRID_COLUMNS_EXPANDED
            Breakpoint.LARGE -> GRID_COLUMNS_LARGE
        }

    /**
     * Si el layout usa dos paneles lado a lado (p. ej. lista + detalle) en este [Breakpoint]
     * (D4). Se activa desde EXPANDED.
     */
    fun useTwoPane(breakpoint: Breakpoint): Boolean = breakpoint.minWidthDp >= Breakpoint.EXPANDED.minWidthDp

    /**
     * Columnas de contenido para un [breakpoint] y un [kind] (design.md §3).
     *
     * FORM/SETTINGS: 1 columna hasta EXPANDED, donde los campos se empaquetan en 2.
     * LIST/DETAIL: una sola columna de contenido; su riqueza en EXPANDED son dos *paneles*
     *   (ver [useTwoPane]), no más columnas dentro de un panel.
     * DASHBOARD: sigue a [gridColumns] (1/2/3/4): la rejilla de métricas crece con el ancho.
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

            ContentKind.DASHBOARD -> gridColumns(breakpoint)
        }
}

// Anchos mínimos (dp) de cada clase de ventana de Material 3.
private const val MEDIUM_MIN_WIDTH_DP = 600
private const val EXPANDED_MIN_WIDTH_DP = 840
private const val LARGE_MIN_WIDTH_DP = 1200

// Columnas de la rejilla en las clases anchas.
private const val GRID_COLUMNS_EXPANDED = 3
private const val GRID_COLUMNS_LARGE = 4
