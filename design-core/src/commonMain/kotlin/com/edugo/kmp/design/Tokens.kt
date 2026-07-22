package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.AnimationDuration
import com.edugo.kmp.design.tokens.ScreenDuration
import com.edugo.kmp.design.tokens.SurfaceOpacity

/**
 * Spacing scale from 0dp to 64dp (spacing0 to spacing16).
 * Legacy aliases (xxs, xs, etc.) are deprecated with ReplaceWith.
 */
object Spacing {
    val spacing0 = 0.dp
    val spacing1 = 4.dp
    val spacing2 = 8.dp
    val spacing3 = 12.dp
    val spacing4 = 16.dp
    val spacing5 = 20.dp
    val spacing6 = 24.dp
    val spacing7 = 28.dp
    val spacing8 = 32.dp
    val spacing9 = 36.dp
    val spacing10 = 40.dp
    val spacing11 = 44.dp
    val spacing12 = 48.dp
    val spacing13 = 52.dp
    val spacing14 = 56.dp
    val spacing15 = 60.dp
    val spacing16 = 64.dp

    // Deprecated aliases
    @Deprecated("Use spacing1 instead", ReplaceWith("spacing1"))
    val xxs = spacing1

    @Deprecated("Use spacing2 instead", ReplaceWith("spacing2"))
    val xs = spacing2

    @Deprecated("Use spacing3 instead", ReplaceWith("spacing3"))
    val s = spacing3

    @Deprecated("Use spacing4 instead", ReplaceWith("spacing4"))
    val m = spacing4

    @Deprecated("Use spacing6 instead", ReplaceWith("spacing6"))
    val l = spacing6

    @Deprecated("Use spacing8 instead", ReplaceWith("spacing8"))
    val xl = spacing8

    @Deprecated("Use spacing12 instead", ReplaceWith("spacing12"))
    val xxl = spacing12
}

/**
 * Tamaños de componentes específicos.
 */
object Sizes {
    val iconSmall = 16.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconXLarge = 32.dp
    val iconXXLarge = 48.dp
    val iconMassive = 64.dp

    val progressSmall = 24.dp
    val progressLarge = 48.dp

    val buttonHeight = 48.dp

    /**
     * Clearance inferior para listas con FAB y formularios con barra de guardar fija:
     * reserva de espacio al final del contenido scrolleable para que el último ítem no
     * quede tapado por el elemento flotante/fijo (spec §3, G-11/M-6).
     */
    val bottomActionClearance = 88.dp

    /** Grosor de borde de tarjeta y divisores (spec §4/§5.1). */
    val borderThin = 1.dp

    /**
     * Ancho de la sidebar de navegación persistente en Expanded/Large (spec §2.2, D-050.1).
     * La sidebar ancla arriba con superficie `surfaceContainerLow`, cuerpo `DSNavTree` y un
     * divisor `borderThin` × `outlineVariant` contra el contenido.
     */
    val sidebarWidth = 280.dp

    /**
     * Ancho del riel colapsado (sidebar plegada) con SOLO iconos L1 (spec §2.2, D-050.2).
     * Al hacer clic en una sección del riel se abre un flyout anclado con el subárbol.
     */
    val railWidth = 80.dp

    /** Grosor del anillo de foco (spec §5.1/§5.2/§5.3: anillo 2dp `primary`). */
    val borderFocus = 2.dp

    /** Alto mínimo de una fila de lista / tarjeta de dos líneas (spec §5.1). */
    val listRowMinHeight = 72.dp

    object Avatar {
        val small = 24.dp
        val medium = 32.dp
        val large = 40.dp
        val xlarge = 48.dp
        val xxlarge = 64.dp
    }

    object TouchTarget {
        val minimum = 48.dp
        val comfortable = 56.dp
        val generous = 64.dp
    }
}

/**
 * Anchos máximos de legibilidad para contenedores de contenido.
 *
 * Son medidas de legibilidad (línea de lectura, ancho de formulario), no marca → viven en
 * design-core. Regla de uso: todo contenedor de contenido aplica `widthIn(max = …)` + centrado;
 * el espacio sobrante a los lados es margen, no se estira el contenido (spec §1).
 */
object ContentWidth {
    /** Formularios (una columna de campos). */
    val form = 600.dp

    /** Listas y grids de contenido. */
    val list = 840.dp

    /** Texto corrido / lectura larga (prosa). */
    val prose = 720.dp
}

/**
 * Dimensiones del andamiaje de modales (spec §5.6, D-048.6).
 *
 * Regla dura: un modal en modo lectura **nunca recorta contenido**. Si no cabe, el cuerpo scrollea
 * dentro de la altura máxima; el header (título + cierre) y el footer (acciones) quedan fijos
 * (sticky) para estar siempre visibles. Todas las medidas salen de aquí; cero `.dp` sueltos.
 */
object DialogDefaults {
    /** Ancho máximo del modal; el ancho real es `min(maxWidth, 100% − 2·windowMargin)`. */
    val maxWidth = 560.dp

    /** Margen a cada lado respecto al viewport (48dp en total: 24 por lado → alto máx = viewport − 48). */
    val windowMargin = 24.dp

    /** Alto del header sticky (título `titleLarge` + botón de cierre siempre visible). */
    val headerHeight = 64.dp

    /** Alto del footer sticky (fila de acciones). */
    val footerHeight = 72.dp

    /** Opacidad del scrim de fondo. La aporta `androidx.compose.ui.window.Dialog` (MD3: 32%). */
    const val scrimOpacity = 0.32f
}

/**
 * Valores de opacidad/alpha para estados visuales.
 * @see com.edugo.kmp.design.tokens.StateLayer para valores MD3 de state layers.
 * @see com.edugo.kmp.design.tokens.SurfaceOpacity para opacidades de superficie.
 */
object Alpha {
    @Deprecated("Use StateLayer.disabled instead", ReplaceWith("com.edugo.kmp.design.tokens.StateLayer.disabled"))
    const val disabled = 0.4f

    @Deprecated("Use SurfaceOpacity.high instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.high"))
    const val muted = 0.6f

    @Deprecated("Use SurfaceOpacity.high instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.high"))
    const val subtle = 0.7f

    @Deprecated("Use SurfaceOpacity.opaque instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.opaque"))
    const val surfaceVariant = 0.8f
}

/**
 * Duraciones de animaciones y delays (en milisegundos).
 * @see com.edugo.kmp.design.tokens.AnimationDuration para duraciones granulares MD3.
 * @see com.edugo.kmp.design.tokens.ScreenDuration para duraciones de pantalla.
 */
object Durations {
    @Deprecated("Use ScreenDuration.splash instead", ReplaceWith("com.edugo.kmp.design.tokens.ScreenDuration.splash"))
    const val splash = 2000L

    @Deprecated("Use AnimationDuration.short2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.short2"))
    const val short = 200L

    @Deprecated("Use AnimationDuration.long2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.long2"))
    const val medium = 500L

    @Deprecated("Use AnimationDuration.extraLong2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.extraLong2"))
    const val long = 1000L
}

/**
 * Radios de esquinas (border radius).
 * @see com.edugo.kmp.design.tokens.CornerRadius para valores MD3 completos.
 * @see com.edugo.kmp.design.tokens.Shapes para RoundedCornerShape pre-construidos.
 */
object Radius {
    @Deprecated("Use CornerRadius.extraSmall instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.extraSmall"))
    val small = 4.dp

    @Deprecated("Use CornerRadius.small instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.small"))
    val medium = 8.dp

    @Deprecated("Use CornerRadius.large instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.large"))
    val large = 16.dp
}
