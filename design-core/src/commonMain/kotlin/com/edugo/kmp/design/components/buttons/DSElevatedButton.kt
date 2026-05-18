package com.edugo.kmp.design.components.buttons

import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSElevatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

@Preview
@Composable
private fun DSElevatedButtonPreview() {
    DSTheme {
        Surface {
            DSElevatedButton(text = "Elevated Button", onClick = {})
        }
    }
}

@Preview
@Composable
private fun DSElevatedButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSElevatedButton(text = "Elevated Button", onClick = {})
        }
    }
}
