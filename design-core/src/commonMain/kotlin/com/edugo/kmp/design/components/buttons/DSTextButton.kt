package com.edugo.kmp.design.components.buttons

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentColor: Color = Color.Unspecified,
) {
    val defaults = ButtonDefaults.textButtonColors()
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = if (contentColor.isSpecified) contentColor else defaults.contentColor,
            ),
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

@Preview(name = "DSTextButton - Light", showBackground = true)
@Composable
private fun DSTextButtonPreviewLight() {
    DSTheme {
        Surface {
            DSTextButton(text = "Ejemplo", onClick = {})
        }
    }
}

@Preview(name = "DSTextButton - Dark", showBackground = true)
@Composable
private fun DSTextButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSTextButton(text = "Ejemplo", onClick = {})
        }
    }
}
