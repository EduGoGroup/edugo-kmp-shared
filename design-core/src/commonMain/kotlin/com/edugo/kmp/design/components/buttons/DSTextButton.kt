package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ButtonSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        leadingIcon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMedium),
            )
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
        Text(text)
        trailingIcon?.let { icon ->
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMedium),
            )
        }
    }
}

@Preview(name = "DSTextButton - Light", showBackground = true)
@Composable
fun DSTextButtonPreviewLight() {
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
