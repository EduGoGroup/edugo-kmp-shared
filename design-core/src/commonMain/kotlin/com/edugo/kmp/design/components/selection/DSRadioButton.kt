package com.edugo.kmp.design.components.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ButtonSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = Spacing.spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
        )
        if (label != null) {
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
            Text(text = label)
        }
    }
}

@Preview(name = "DSRadioButton - Light", showBackground = true)
@Composable
fun DSRadioButtonPreviewLight() {
    var selected by remember { mutableStateOf(true) }
    DSTheme {
        Surface {
            DSRadioButton(
                selected = selected,
                onClick = { selected = !selected },
                label = "Opcion",
            )
        }
    }
}

@Preview(name = "DSRadioButton - Dark", showBackground = true)
@Composable
private fun DSRadioButtonPreviewDark() {
    var selected by remember { mutableStateOf(true) }
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSRadioButton(
                selected = selected,
                onClick = { selected = !selected },
                label = "Opcion",
            )
        }
    }
}
