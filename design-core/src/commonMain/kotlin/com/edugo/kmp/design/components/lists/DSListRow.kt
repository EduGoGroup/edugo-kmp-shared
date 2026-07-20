package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ComponentShapes
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Canonical list row of the EduGo design system.
 *
 * Visually equivalent to the master-detail `DetailItemCard`: a `Card` with
 * `surfaceVariant` background, internal `Row` with optional leading slot,
 * a column for headline + supporting text, and a trailing slot that defaults
 * to a chevron-right icon.
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
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .let { m ->
                if (contentDescription != null) {
                    m.semantics { this.contentDescription = contentDescription }
                } else {
                    m
                }
            }

    Card(
        modifier = baseModifier,
        colors = CardDefaults.cardColors(containerColor = DSListRowDefaults.containerColor),
        // Cards pasan por ComponentShapes (D-046.3): card = largeIncreased.
        shape = RoundedCornerShape(ComponentShapes.card),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
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
                    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
                        supporting()
                    }
                }
            }
            (trailing ?: DSListRowDefaults.chevron()).invoke()
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    supporting =
        supportingText?.let {
            {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
 * Visual tokens and reusable slot factories for [DSListRow].
 *
 * Specialised renderers (e.g. SDUI list renderers) can reuse these to keep
 * the look-and-feel aligned with the canonical row without duplicating
 * magic numbers.
 */
object DSListRowDefaults {
    const val tag: String = "dsListRow"

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val contentColor: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val padding: PaddingValues
        @Composable get() = PaddingValues(Spacing.spacing3)

    /**
     * Default trailing slot: a chevron-right icon at 24dp with
     * `onSurfaceVariant` tint at 50% opacity.
     */
    fun chevron(): @Composable () -> Unit =
        {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "chevron-default",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp),
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
