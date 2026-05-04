package com.edugo.kmp.design.components.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSDivider(
    modifier: Modifier = Modifier,
    inset: Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = inset),
    )
}

@Composable
fun DSVerticalDivider(modifier: Modifier = Modifier) {
    VerticalDivider(modifier = modifier)
}

@Composable
fun DSInsetDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = Spacing.spacing4),
    )
}

@Preview(name = "DSDivider - Light", showBackground = true)
@Composable
fun DSDividerPreviewLight() {
    DSTheme {
        Surface {
            Column {
                Text("Horizontal")
                DSDivider()
                Spacer(Modifier.height(Spacing.spacing3))
                Text("Inset")
                DSInsetDivider()
                Spacer(Modifier.height(Spacing.spacing3))
                Row {
                    Text("Vertical")
                    Spacer(Modifier.width(Spacing.spacing3))
                    DSVerticalDivider(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Preview(name = "DSDivider - Dark", showBackground = true)
@Composable
private fun DSDividerPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column {
                Text("Horizontal")
                DSDivider()
                Spacer(Modifier.height(Spacing.spacing3))
                Text("Inset")
                DSInsetDivider()
                Spacer(Modifier.height(Spacing.spacing3))
                Row {
                    Text("Vertical")
                    Spacer(Modifier.width(Spacing.spacing3))
                    DSVerticalDivider(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
