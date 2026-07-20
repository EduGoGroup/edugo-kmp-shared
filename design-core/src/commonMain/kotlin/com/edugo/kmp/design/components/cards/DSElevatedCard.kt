package com.edugo.kmp.design.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.tokens.CardSpacing
import com.edugo.kmp.design.tokens.ComponentShapes
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Cards pasan por ComponentShapes (D-046.11): card = largeIncreased.
    val cardShape = RoundedCornerShape(ComponentShapes.card)

    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = cardShape,
        ) {
            Column(modifier = Modifier.padding(CardSpacing.internalPadding)) {
                content()
            }
        }
    } else {
        ElevatedCard(modifier = modifier, shape = cardShape) {
            Column(modifier = Modifier.padding(CardSpacing.internalPadding)) {
                content()
            }
        }
    }
}

@Preview(name = "DSElevatedCard - Light", showBackground = true)
@Composable
fun DSElevatedCardPreviewLight() {
    DSTheme {
        Surface {
            DSElevatedCard {
                Text("Tarjeta elevada")
                Text("Contenido de ejemplo")
            }
        }
    }
}

@Preview(name = "DSElevatedCard - Dark", showBackground = true)
@Composable
private fun DSElevatedCardPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSElevatedCard {
                Text("Tarjeta elevada")
                Text("Contenido de ejemplo")
            }
        }
    }
}
