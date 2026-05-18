package com.edugo.kmp.design.components.buttons

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentColor: Color = Color.Unspecified,
) {
    val defaults = ButtonDefaults.outlinedButtonColors()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = if (contentColor.isSpecified) contentColor else defaults.contentColor,
            ),
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

@Preview
@Composable
private fun DSOutlinedButtonPreview() {
    DSTheme { DSOutlinedButton(text = "Outlined Button", onClick = {}) }
}

@Preview
@Composable
private fun DSOutlinedButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) { DSOutlinedButton(text = "Outlined Button", onClick = {}) }
}
