package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class DSIconButtonVariant { STANDARD, FILLED, TONAL, OUTLINED }

@Composable
fun DSIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: DSIconButtonVariant = DSIconButtonVariant.STANDARD,
    tint: Color = Color.Unspecified,
) {
    val iconContent: @Composable () -> Unit = {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.iconLarge),
            tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
        )
    }

    when (variant) {
        DSIconButtonVariant.STANDARD ->
            IconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = iconContent,
            )
        DSIconButtonVariant.FILLED ->
            FilledIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = iconContent,
            )
        DSIconButtonVariant.TONAL ->
            FilledTonalIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = iconContent,
            )
        DSIconButtonVariant.OUTLINED ->
            OutlinedIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = iconContent,
            )
    }
}

@Preview
@Composable
private fun DSIconButtonPreview() {
    DSTheme {
        DSIconButton(
            icon = Icons.Default.Favorite,
            contentDescription = "Favorite",
            onClick = {},
            variant = DSIconButtonVariant.FILLED,
        )
    }
}

@Preview
@Composable
private fun DSIconButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSIconButton(
            icon = Icons.Default.Favorite,
            contentDescription = "Favorite",
            onClick = {},
            variant = DSIconButtonVariant.FILLED,
        )
    }
}
