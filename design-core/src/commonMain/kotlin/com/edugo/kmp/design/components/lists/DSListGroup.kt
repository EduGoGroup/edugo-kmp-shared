package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.media.DSDivider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSListGroup(
    modifier: Modifier = Modifier,
    header: String? = null,
    showDividers: Boolean = true,
    items: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        if (header != null) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.padding(
                        horizontal = Spacing.spacing4,
                        vertical = Spacing.spacing2,
                    ),
            )
        }
        items()
        if (showDividers) {
            DSDivider()
        }
    }
}

@Preview(name = "DSListGroup - Light", showBackground = true)
@Composable
fun DSListGroupPreviewLight() {
    DSTheme {
        Surface {
            DSListGroup(header = "Grupo") {
                DSListItem(
                    headlineText = "Elemento 1",
                    supportingText = "Detalle 1",
                )
                DSListItem(
                    headlineText = "Elemento 2",
                    supportingText = "Detalle 2",
                )
            }
        }
    }
}

@Preview(name = "DSListGroup - Dark", showBackground = true)
@Composable
private fun DSListGroupPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSListGroup(header = "Grupo") {
                DSListItem(
                    headlineText = "Elemento 1",
                    supportingText = "Detalle 1",
                )
                DSListItem(
                    headlineText = "Elemento 2",
                    supportingText = "Detalle 2",
                )
            }
        }
    }
}
