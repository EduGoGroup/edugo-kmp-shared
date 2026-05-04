package com.edugo.kmp.design.components.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSPlainTooltip(
    tooltipText: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip { Text(tooltipText) }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSRichTooltip(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(title) },
                action =
                    if (actionText != null && onAction != null) {
                        {
                            TextButton(onClick = onAction) { Text(actionText) }
                        }
                    } else {
                        null
                    },
            ) {
                Text(text)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTooltip - Light", showBackground = true)
@Composable
fun DSTooltipPreviewLight() {
    DSTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                DSPlainTooltip(
                    tooltipText = "Tooltip simple",
                ) {
                    TextButton(onClick = {}) { Text("Hover aqui") }
                }
                Spacer(Modifier.height(Spacing.spacing4))
                DSRichTooltip(
                    title = "Titulo",
                    text = "Descripcion del tooltip",
                    actionText = "Accion",
                    onAction = {},
                ) {
                    TextButton(onClick = {}) { Text("Tooltip rico") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTooltip - Dark", showBackground = true)
@Composable
private fun DSTooltipPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                DSPlainTooltip(
                    tooltipText = "Tooltip simple",
                ) {
                    TextButton(onClick = {}) { Text("Hover aqui") }
                }
                Spacer(Modifier.height(Spacing.spacing4))
                DSRichTooltip(
                    title = "Titulo",
                    text = "Descripcion del tooltip",
                    actionText = "Accion",
                    onAction = {},
                ) {
                    TextButton(onClick = {}) { Text("Tooltip rico") }
                }
            }
        }
    }
}
