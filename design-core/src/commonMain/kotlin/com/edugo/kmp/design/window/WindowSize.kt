package com.edugo.kmp.design.window

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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
    /**
     * Clase de ancho responsiva derivada del [widthDp] (fuente única: [breakpointFromWidth]).
     *
     * Si estás en composición y solo te interesa la clase, lee [LocalWindowWidthClass] en vez de
     * `LocalWindowSize.current.widthClass`: evita recomponer con cada dp de arrastre.
     */
    val widthClass: Breakpoint get() = breakpointFromWidth(widthDp)
}

/**
 * [androidx.compose.runtime.CompositionLocal] con el [WindowSize] vigente (dp **exactos**).
 *
 * Es un `compositionLocalOf` **no-static a propósito**: el valor cambia con **cada dp** de arrastre
 * al redimensionar la ventana. Con `staticCompositionLocalOf` Compose no rastrea lectores y cada
 * cambio invalida el `content` **completo** del proveedor — es decir, toda la app colgada de
 * [ProvideWindowSize] (bug 0086, mecanismo 1). Con el local no-static solo se recompone quien lee.
 *
 * Léelo **solo si necesitas el dp crudo** (altura proporcional de un sheet/diálogo, un umbral
 * propio de la app). Si únicamente decides layout por breakpoint, lee [LocalWindowWidthClass]: su
 * valor cambia al cruzar umbral, no dp a dp.
 *
 * No tiene default: hay que envolver el árbol con [ProvideWindowSize]. Acceder sin proveedor es
 * un error de programación (raíz de composición mal cableada), por eso falla ruidosamente.
 */
val LocalWindowSize =
    compositionLocalOf<WindowSize> {
        error("WindowSize no provisto. Envuelve con ProvideWindowSize.")
    }

/**
 * [androidx.compose.runtime.CompositionLocal] con la **clase de ancho** ([Breakpoint]) vigente.
 *
 * Se deriva de [WindowSize.widthClass] y lo publica [ProvideWindowSize] en el mismo punto, como
 * local **aparte** para que quien solo necesita el breakpoint no pague el dp a dp: el valor solo
 * cambia al **cruzar** un umbral de [breakpointFromWidth] (600 / 840 / 1200 dp), así que arrastrar
 * la ventana dentro de una misma banda no recompone a estos lectores.
 *
 * Prefiérelo sobre `LocalWindowSize.current.widthClass` siempre que la decisión sea por clase.
 *
 * No tiene default, por el mismo motivo que [LocalWindowSize].
 */
val LocalWindowWidthClass =
    compositionLocalOf<Breakpoint> {
        error("WindowSize no provisto. Envuelve con ProvideWindowSize.")
    }

/**
 * Mide la ventana disponible con [BoxWithConstraints] y publica el [WindowSize] resultante en
 * [LocalWindowSize], más su clase de ancho en [LocalWindowWidthClass], para todo el [content].
 *
 * La orientación es [Orientation.LANDSCAPE] cuando el ancho disponible es mayor o igual al alto,
 * y [Orientation.PORTRAIT] en caso contrario. Se ubica una sola vez en la raíz de composición.
 */
@Composable
fun ProvideWindowSize(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val orientation =
            if (maxWidth >= maxHeight) Orientation.LANDSCAPE else Orientation.PORTRAIT
        ProvideWindowSize(
            windowSize =
                WindowSize(
                    widthDp = maxWidth.value.toInt(),
                    heightDp = maxHeight.value.toInt(),
                    orientation = orientation,
                ),
            content = content,
        )
    }
}

/**
 * Publica un [windowSize] ya conocido en [LocalWindowSize] y [LocalWindowWidthClass].
 *
 * Para tests y para hosts que ya saben el tamaño (no hace falta medir). Úsalo en vez de un
 * `CompositionLocalProvider(LocalWindowSize provides …)` a mano: los dos locales deben viajar
 * juntos y este overload lo garantiza — proveer solo uno deja al otro sin valor y, como ninguno
 * tiene default, el primer lector falla.
 */
@Composable
fun ProvideWindowSize(
    windowSize: WindowSize,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalWindowSize provides windowSize,
        LocalWindowWidthClass provides windowSize.widthClass,
    ) {
        content()
    }
}
