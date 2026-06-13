package com.edugo.kmp.design.window

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.edugo.kmp.design.tokens.Breakpoint
import com.edugo.kmp.design.tokens.breakpointFromWidth

/**
 * Orientación de la ventana de la app, derivada de la relación ancho/alto disponibles.
 *
 * [PORTRAIT]: alto > ancho (vertical). [LANDSCAPE]: ancho >= alto (horizontal o cuadrado).
 */
enum class Orientation {
    PORTRAIT,
    LANDSCAPE,
}

/**
 * Tamaño actual de la ventana de la app medido en dp, más su [orientation].
 *
 * Enfocado en tamaño + orientación: la clase de ancho ([widthClass]) se deriva del [Breakpoint]
 * canónico vía [breakpointFromWidth], reutilizando la única fuente de breakpoints
 * (`tokens/ResponsiveTokens.kt`). NO incluye plataforma: si una fase futura la necesita, el
 * `expect object Platform` global ya está disponible (no se duplica aquí).
 *
 * Se provee en la raíz de composición vía [ProvideWindowSize] y se consume con [LocalWindowSize].
 */
data class WindowSize(
    val widthDp: Int,
    val heightDp: Int,
    val orientation: Orientation,
) {
    /** Clase de ancho responsiva derivada del [widthDp] (fuente única: [breakpointFromWidth]). */
    val widthClass: Breakpoint get() = breakpointFromWidth(widthDp)
}

/**
 * [androidx.compose.runtime.CompositionLocal] con el [WindowSize] vigente.
 *
 * No tiene default: hay que envolver el árbol con [ProvideWindowSize]. Acceder sin proveedor es
 * un error de programación (raíz de composición mal cableada), por eso falla ruidosamente.
 */
val LocalWindowSize =
    staticCompositionLocalOf<WindowSize> {
        error("WindowSize no provisto. Envuelve con ProvideWindowSize.")
    }

/**
 * Mide la ventana disponible con [BoxWithConstraints] y publica el [WindowSize] resultante en
 * [LocalWindowSize] para todo el [content].
 *
 * La orientación es [Orientation.LANDSCAPE] cuando el ancho disponible es mayor o igual al alto,
 * y [Orientation.PORTRAIT] en caso contrario. Se ubica una sola vez en la raíz de composición.
 */
@Composable
fun ProvideWindowSize(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val orientation =
            if (maxWidth >= maxHeight) Orientation.LANDSCAPE else Orientation.PORTRAIT
        val windowSize =
            WindowSize(
                widthDp = maxWidth.value.toInt(),
                heightDp = maxHeight.value.toInt(),
                orientation = orientation,
            )
        CompositionLocalProvider(LocalWindowSize provides windowSize) {
            content()
        }
    }
}
