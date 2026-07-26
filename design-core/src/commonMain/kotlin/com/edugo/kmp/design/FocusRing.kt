package com.edugo.kmp.design

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.tokens.Shapes

/**
 * Anillo de foco visible del sistema (spec §10, D-051.5): borde de
 * [Sizes.borderFocus] (2dp) en color `primary` que aparece cuando el elemento
 * recibe foco de teclado y desaparece en cualquier otro estado.
 *
 * Es la extracción del patrón que ya vivía embebido en `DSListRow` (que cambia su
 * borde fino por un anillo 2dp `primary` al enfocarse), para que cualquier
 * superficie clicable — dentro o fuera de design-core — pueda señalar el foco con
 * el MISMO anillo en vez de reimplementarlo.
 *
 * **Uso:** encadenar ANTES del modificador que aporta la focusabilidad
 * (`clickable`, `selectable`, `focusable`…), porque [onFocusChanged] observa el
 * foco de los modificadores que quedan a su derecha en la cadena:
 *
 * ```
 * Row(modifier = Modifier.dsFocusRing(Shapes.small).clickable(onClick = onPick))
 * ```
 *
 * **Sobre el "offset 2dp" del spec:** el anillo se dibuja al borde del componente,
 * igual que en `DSListRow` — separarlo con un inset propio desplazaría el layout de
 * cada sitio donde se aplica. La coherencia con el patrón ya establecido en el DS
 * manda sobre la lectura literal del offset.
 *
 * @param shape forma del anillo; debe coincidir con la del contenedor que envuelve.
 * @param width grosor del anillo (por defecto [Sizes.borderFocus]).
 */
@Composable
fun Modifier.dsFocusRing(
    shape: Shape = Shapes.none,
    width: Dp = Sizes.borderFocus,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val ringColor = MaterialTheme.colorScheme.primary
    return this
        .onFocusChanged { focused = it.isFocused }
        .border(
            width = if (focused) width else Dp.Hairline,
            color = if (focused) ringColor else Color.Transparent,
            shape = shape,
        )
}
