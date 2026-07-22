package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Elevation
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ComponentShapes
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Canonical list row of the EduGo design system.
 *
 * Rendered as a real card: a `Card` with a tonal `surfaceContainer` background,
 * a 1dp `outlineVariant` border and no shadow (level0) — its identity is tonal +
 * border, not elevation (spec §4/§5.1). Inside, a `Row` with an optional leading
 * slot, a column for headline (`bodyLarge`, `onSurface`) + supporting text
 * (`bodyMedium`, `onSurfaceVariant` sin alpha) and a trailing slot that defaults
 * to a chevron-right icon. Clickable rows surface the MD3 interaction state layers
 * (hover/press/focus) over the container and swap the border for a 2dp `primary`
 * focus ring.
 *
 * The component is intentionally ignorant of any data schema; it only consumes
 * composable slots. For the common text-only case, see the convenience
 * overload that accepts `headlineText` / `supportingText` strings.
 */
@Composable
fun DSListRow(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supporting: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val baseModifier =
        Modifier
            .testTag(DSListRowDefaults.tag)
            .then(modifier)
            .fillMaxWidth()
            .let { m ->
                if (contentDescription != null) {
                    m.semantics { this.contentDescription = contentDescription }
                } else {
                    m
                }
            }

    // El foco de teclado se señala con un anillo 2dp `primary` que reemplaza el
    // borde fino (spec §5.1); solo las filas clicables reciben foco.
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val border =
        if (focused) {
            BorderStroke(Sizes.borderFocus, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(Sizes.borderThin, DSListRowDefaults.borderColor)
        }
    val shape = RoundedCornerShape(ComponentShapes.card)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = baseModifier,
            colors = DSListRowDefaults.cardColors(),
            shape = shape,
            elevation = DSListRowDefaults.flatElevation(),
            border = border,
            interactionSource = interactionSource,
        ) {
            DSListRowContent(leading, headline, supporting, trailing)
        }
    } else {
        Card(
            modifier = baseModifier,
            colors = DSListRowDefaults.cardColors(),
            shape = shape,
            elevation = DSListRowDefaults.flatElevation(),
            border = border,
        ) {
            DSListRowContent(leading, headline, supporting, trailing)
        }
    }
}

/**
 * Convenience overload of [DSListRow] that accepts plain strings for headline
 * and supporting text. Delegates to the slot-based overload.
 *
 * Use the slot overload when you need richer typography, badges, or any
 * non-text content.
 */
@Composable
fun DSListRow(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) = DSListRow(
    headline = {
        Text(
            text = headlineText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
    },
    supporting =
        supportingText?.let {
            {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    leading = leading,
    trailing = trailing,
    onClick = onClick,
    contentDescription = contentDescription,
    modifier = modifier,
)

/**
 * Internal row layout shared by both [DSListRow] overloads: leading slot, a
 * column with headline (`bodyLarge`) + supporting text (`bodyMedium`,
 * `onSurfaceVariant`) and a trailing slot defaulting to the chevron.
 */
@Composable
private fun DSListRowContent(
    leading: (@Composable () -> Unit)?,
    headline: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Sizes.listRowMinHeight)
                .padding(DSListRowDefaults.padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
    ) {
        if (leading != null) {
            leading()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                headline()
            }
            if (supporting != null) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = DSListRowDefaults.supportingColor,
                    ),
                ) {
                    supporting()
                }
            }
        }
        (trailing ?: DSListRowDefaults.chevron()).invoke()
    }
}

/**
 * Visual tokens and reusable slot factories for [DSListRow].
 *
 * Specialised renderers (e.g. SDUI list renderers) can reuse these to keep
 * the look-and-feel aligned with the canonical row without duplicating
 * magic numbers.
 *
 * Estados dependientes del llamador (spec §5.1): la fila **seleccionada** usa
 * `secondaryContainer` + borde `outline` + título `onSecondaryContainer`, y la
 * **deshabilitada** atenúa el contenido a `StateLayer.disabled` (38%). Ambos
 * requieren intención del consumidor (selección / enabled) que llega con el
 * rework de selectores; se documentan aquí para que ese cableado use estos roles
 * y no valores mágicos. Los estados hover / press / foco los resuelve el state
 * layer MD3 del `Card` clicable + el anillo de foco del propio componente.
 */
object DSListRowDefaults {
    const val tag: String = "dsListRow"

    /** Fondo tonal de la tarjeta: identidad = tonal + borde, sin sombra (spec §4/§5.1). */
    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    /** Color de contenido por defecto (título en `onSurface`, spec §5.1). */
    val contentColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    /** Color del texto secundario / subtítulo: `onSurfaceVariant` sin alpha (spec §5.1/§8). */
    val supportingColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    /** Color del borde 1dp de la tarjeta (spec §4/§5.1). */
    val borderColor: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val padding: PaddingValues
        @Composable get() = PaddingValues(Spacing.spacing4)

    /** Colores de la tarjeta: fondo tonal `surfaceContainer`, contenido `onSurface`. */
    @Composable
    fun cardColors() =
        CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        )

    /**
     * Elevación plana (level0 en todos los estados): la identidad de la tarjeta es
     * tonal + borde; la sombra se reserva a lo flotante (spec §4).
     */
    @Composable
    fun flatElevation() =
        CardDefaults.cardElevation(
            defaultElevation = Elevation.level0,
            pressedElevation = Elevation.level0,
            focusedElevation = Elevation.level0,
            hoveredElevation = Elevation.level0,
            draggedElevation = Elevation.level0,
            disabledElevation = Elevation.level0,
        )

    /**
     * Default trailing slot: a chevron-right icon at 24dp with `onSurfaceVariant`
     * tint (sin alpha — la jerarquía la da el rol de color, no la opacidad; spec §8).
     */
    fun chevron(): @Composable () -> Unit =
        {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "chevron-default",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.iconLarge),
            )
        }
}

// --- Previews ---

@Preview(name = "DSListRow - Solo headline", showBackground = true)
@Composable
private fun DSListRowHeadlineOnlyPreview() {
    DSTheme {
        Surface {
            DSListRow(headlineText = "Matemáticas avanzadas")
        }
    }
}

@Preview(name = "DSListRow - Con supporting", showBackground = true)
@Composable
private fun DSListRowWithSupportingPreview() {
    DSTheme {
        Surface {
            DSListRow(
                headlineText = "Matemáticas avanzadas",
                supportingText = "12 estudiantes • Lunes 08:00",
            )
        }
    }
}

@Preview(name = "DSListRow - Leading avatar", showBackground = true)
@Composable
private fun DSListRowWithAvatarPreview() {
    DSTheme {
        Surface {
            DSListRow(
                headlineText = "Ana López",
                supportingText = "Profesora de literatura",
                leading = {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "AL",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Preview(name = "DSListRow - Leading icon school 40dp", showBackground = true)
@Composable
private fun DSListRowWithLeadingIconPreview() {
    DSTheme {
        Surface {
            DSListRow(
                headlineText = "Colegio San Martín",
                supportingText = "Sede central",
                leading = {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape,
                                ).padding(8.dp),
                    )
                },
            )
        }
    }
}

@Preview(name = "DSListRow - Trailing chip custom", showBackground = true)
@Composable
private fun DSListRowWithTrailingChipPreview() {
    DSTheme {
        Surface {
            DSListRow(
                headlineText = "Solicitud de matrícula",
                supportingText = "Enviada el 12 de marzo",
                trailing = {
                    AssistChip(
                        onClick = {},
                        label = { Text("Pendiente") },
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                    )
                },
            )
        }
    }
}

@Preview(name = "DSListRow - Clickable + Dark", showBackground = true)
@Composable
private fun DSListRowClickableDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSListRow(
                headlineText = "Configuración",
                supportingText = "Tema, idioma, notificaciones",
                onClick = {},
            )
        }
    }
}
