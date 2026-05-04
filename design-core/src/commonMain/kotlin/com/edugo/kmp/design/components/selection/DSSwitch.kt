package com.edugo.kmp.design.components.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
fun DSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(vertical = Spacing.spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Text(text = label, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Preview(name = "DSSwitch - Light", showBackground = true)
@Composable
fun DSSwitchPreviewLight() {
    var checked by remember { mutableStateOf(true) }
    DSTheme {
        Surface {
            DSSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Notificaciones",
            )
        }
    }
}

@Preview(name = "DSSwitch - Dark", showBackground = true)
@Composable
private fun DSSwitchPreviewDark() {
    var checked by remember { mutableStateOf(true) }
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                label = "Notificaciones",
            )
        }
    }
}
