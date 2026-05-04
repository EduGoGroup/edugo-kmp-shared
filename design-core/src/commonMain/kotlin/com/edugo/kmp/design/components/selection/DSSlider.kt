package com.edugo.kmp.design.components.selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                modifier = Modifier.padding(bottom = Spacing.spacing1),
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}

@Preview(name = "DSSlider - Light", showBackground = true)
@Composable
fun DSSliderPreviewLight() {
    var value by remember { mutableStateOf(0.4f) }
    DSTheme {
        Surface {
            DSSlider(
                value = value,
                onValueChange = { value = it },
                label = "Progreso",
            )
        }
    }
}

@Preview(name = "DSSlider - Dark", showBackground = true)
@Composable
private fun DSSliderPreviewDark() {
    var value by remember { mutableStateOf(0.4f) }
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSlider(
                value = value,
                onValueChange = { value = it },
                label = "Progreso",
            )
        }
    }
}
