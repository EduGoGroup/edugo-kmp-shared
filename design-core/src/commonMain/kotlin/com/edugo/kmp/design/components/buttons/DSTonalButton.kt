package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ButtonSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
) {
    val defaults = ButtonDefaults.filledTonalButtonColors()
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (containerColor.isSpecified) containerColor else defaults.containerColor,
            contentColor = if (contentColor.isSpecified) contentColor else defaults.contentColor,
        ),
    ) {
        leadingIcon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMedium),
            )
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
        Text(text, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
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

@Preview(name = "DSTonalButton - Light", showBackground = true)
@Composable
private fun DSTonalButtonPreview() {
    DSTheme {
        DSTonalButton(
            text = "Tonal Button",
            onClick = {},
        )
    }
}

@Preview(name = "DSTonalButton - Dark", showBackground = true)
@Composable
private fun DSTonalButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSTonalButton(
            text = "Tonal Button",
            onClick = {},
        )
    }
}
