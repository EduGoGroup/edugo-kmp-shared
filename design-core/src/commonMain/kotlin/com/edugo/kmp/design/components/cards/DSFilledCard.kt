package com.edugo.kmp.design.components.cards

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.tokens.CardSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSFilledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(CardSpacing.internalPadding),
            ) {
                content()
            }
        }
    } else {
        Card(modifier = modifier) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(CardSpacing.internalPadding),
            ) {
                content()
            }
        }
    }
}

@Preview(name = "DSFilledCard - Light", showBackground = true)
@Composable
fun DSFilledCardPreviewLight() {
    DSTheme {
        Surface {
            DSFilledCard {
                Text("Tarjeta filled")
                Text("Contenido de ejemplo")
            }
        }
    }
}

@Preview(name = "DSFilledCard - Dark", showBackground = true)
@Composable
private fun DSFilledCardPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSFilledCard {
                Text("Tarjeta filled")
                Text("Contenido de ejemplo")
            }
        }
    }
}
