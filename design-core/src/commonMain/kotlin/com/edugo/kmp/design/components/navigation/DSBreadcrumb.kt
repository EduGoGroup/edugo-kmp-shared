package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.Spacing

/**
 * Un tramo del breadcrumb (plan 050 D-050.3): la etiqueta legible y, si es un ANCESTRO
 * navegable, su [onClick]. El ÚLTIMO tramo (pantalla actual) lleva `onClick = null` y se
 * pinta como destino no clicable.
 *
 * @property label Texto del tramo (p. ej. "Administración", "Usuarios", "Editar: Miguel Castro").
 * @property onClick Acción al pulsar un ancestro; `null` => tramo no clicable (pantalla actual).
 */
data class DSBreadcrumbItem(
    val label: String,
    val onClick: (() -> Unit)? = null,
)

/**
 * Breadcrumb del top bar de contenido en Expanded/Large (plan 050 D-050.3).
 *
 * Anatomía: tramos en una fila `bodyMedium` separados por `›` en `onSurfaceVariant`. Los
 * ANCESTROS (con [DSBreadcrumbItem.onClick]) son clicables y se pintan en `onSurfaceVariant`;
 * el ÚLTIMO tramo (pantalla actual) va en `onSurface` con peso semibold y NO es clicable,
 * aunque traiga `onClick` (la posición manda: el destino actual nunca navega a sí mismo).
 *
 * La ruta se deriva del árbol del menú aguas arriba (mapper en `kmp-screens`); este componente
 * es neutral (design-core no conoce SDUI). Si la fila no cabe, hace scroll horizontal sin
 * romper el chrome; cada tramo va en una línea con elipsis.
 *
 * @param items Tramos de la ruta, de la raíz a la pantalla actual (el último es el destino).
 * @param modifier Modificador del contenedor.
 */
@Composable
fun DSBreadcrumb(
    items: List<DSBreadcrumbItem>,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier
            .testTag(DSBreadcrumbDefaults.tag)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val lastIndex = items.lastIndex
        items.forEachIndexed { index, item ->
            val isCurrent = index == lastIndex
            BreadcrumbSegmentText(item = item, isCurrent = isCurrent)
            if (!isCurrent) {
                Text(
                    text = DSBreadcrumbDefaults.SEPARATOR,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.spacing1),
                )
            }
        }
    }
}

/**
 * Un tramo del breadcrumb. El ancestro clicable ([DSBreadcrumbItem.onClick] no nulo y NO actual)
 * usa `onSurfaceVariant`; el tramo actual usa `onSurface` + semibold y nunca es clicable.
 */
@Composable
private fun BreadcrumbSegmentText(item: DSBreadcrumbItem, isCurrent: Boolean) {
    val onClick = item.onClick
    val baseModifier = if (!isCurrent && onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Text(
        text = item.label,
        style = if (isCurrent) {
            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        } else {
            MaterialTheme.typography.bodyMedium
        },
        color = if (isCurrent) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = baseModifier.padding(vertical = Spacing.spacing1),
    )
}

/** Constantes del breadcrumb (separador y tag de test). */
object DSBreadcrumbDefaults {
    /** Tag de test del contenedor de la fila. */
    const val tag: String = "dsBreadcrumb"

    /** Separador entre tramos (spec §2.2 / D-050.3): chevron `›`. */
    const val SEPARATOR: String = "›"
}
