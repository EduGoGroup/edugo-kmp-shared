package com.edugo.kmp.design.components.feedback

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.SemanticTone
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Badge compacto de estado con tono semantico.
 *
 * Diferente de [com.edugo.kmp.design.components.selection.DSChip]:
 * - DSChip es interactivo (FilterChip/AssistChip/InputChip/SuggestionChip).
 * - DSStatusBadge es decorativo, no toma click y tipiza visualmente un
 *   estado (success/warning/danger/info/neutral) via [SemanticTone].
 *
 * El mapeo de tono a color usa siempre `MaterialTheme.colorScheme`, asi que
 * respeta tema claro/oscuro y temas custom sin shims.
 */
@Composable
fun DSStatusBadge(
    label: String,
    tone: SemanticTone,
    modifier: Modifier = Modifier,
) {
    val (container, content) = tone.colors()
    Surface(
        modifier = modifier,
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    horizontal = Spacing.spacing2,
                    vertical = Spacing.spacing1,
                ),
        )
    }
}

@Composable
private fun SemanticTone.colors(): Pair<Color, Color> =
    with(MaterialTheme.colorScheme) {
        when (this@colors) {
            SemanticTone.Neutral -> surfaceVariant to onSurfaceVariant
            SemanticTone.Success -> primaryContainer to onPrimaryContainer
            SemanticTone.Warning -> tertiaryContainer to onTertiaryContainer
            SemanticTone.Danger -> errorContainer to onErrorContainer
            SemanticTone.Info -> secondaryContainer to onSecondaryContainer
        }
    }

@Preview(name = "DSStatusBadge - Light", showBackground = true)
@Composable
private fun DSStatusBadgePreviewLight() {
    DSTheme {
        Surface {
            androidx.compose.foundation.layout.Column(
                verticalArrangement =
                    androidx.compose.foundation.layout.Arrangement
                        .spacedBy(Spacing.spacing1),
                modifier = Modifier.padding(Spacing.spacing2),
            ) {
                DSStatusBadge(label = "Neutral", tone = SemanticTone.Neutral)
                DSStatusBadge(label = "Activo", tone = SemanticTone.Success)
                DSStatusBadge(label = "Pendiente", tone = SemanticTone.Warning)
                DSStatusBadge(label = "Critico", tone = SemanticTone.Danger)
                DSStatusBadge(label = "Info", tone = SemanticTone.Info)
            }
        }
    }
}

@Preview(name = "DSStatusBadge - Dark", showBackground = true)
@Composable
private fun DSStatusBadgePreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            androidx.compose.foundation.layout.Column(
                verticalArrangement =
                    androidx.compose.foundation.layout.Arrangement
                        .spacedBy(Spacing.spacing1),
                modifier = Modifier.padding(Spacing.spacing2),
            ) {
                DSStatusBadge(label = "Neutral", tone = SemanticTone.Neutral)
                DSStatusBadge(label = "Activo", tone = SemanticTone.Success)
                DSStatusBadge(label = "Pendiente", tone = SemanticTone.Warning)
                DSStatusBadge(label = "Critico", tone = SemanticTone.Danger)
                DSStatusBadge(label = "Info", tone = SemanticTone.Info)
            }
        }
    }
}
