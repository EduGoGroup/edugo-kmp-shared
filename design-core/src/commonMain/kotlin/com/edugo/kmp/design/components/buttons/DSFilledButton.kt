package com.edugo.kmp.design.components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
) {
    val defaults = ButtonDefaults.buttonColors()
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (containerColor.isSpecified) containerColor else defaults.containerColor,
                contentColor = if (contentColor.isSpecified) contentColor else defaults.contentColor,
            ),
    ) {
        ButtonContent(text, leadingIcon, trailingIcon, loading)
    }
}

@Preview
@Composable
private fun DSFilledButtonPreview() {
    DSTheme {
        Surface {
            DSFilledButton(
                text = "Filled Button",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSFilledButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSFilledButton(
                text = "Filled Button",
                onClick = {},
            )
        }
    }
}
