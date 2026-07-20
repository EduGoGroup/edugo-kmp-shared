package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ComponentShapes
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSFloatingActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        // FABs pasan por ComponentShapes (D-046.11): fab = full (pill).
        shape = RoundedCornerShape(ComponentShapes.fab),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.iconLarge),
        )
    }
}

@Composable
fun DSSmallFloatingActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        // FABs pasan por ComponentShapes (D-046.11): fab = full (pill).
        shape = RoundedCornerShape(ComponentShapes.fab),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.iconLarge),
        )
    }
}

@Composable
fun DSExtendedFloatingActionButton(
    text: String,
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Sizes.iconLarge),
            )
        },
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        // FABs pasan por ComponentShapes (D-046.11): fab = full (pill).
        shape = RoundedCornerShape(ComponentShapes.fab),
    )
}

@Preview
@Composable
private fun DSFloatingActionButtonPreview() {
    DSTheme {
        Surface {
            DSFloatingActionButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSFloatingActionButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSFloatingActionButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSSmallFloatingActionButtonPreview() {
    DSTheme {
        Surface {
            DSSmallFloatingActionButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSSmallFloatingActionDarkButtonPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSmallFloatingActionButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSExtendedFloatingActionButtonPreview() {
    DSTheme {
        Surface {
            DSExtendedFloatingActionButton(
                text = "Add Item",
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSExtendedFloatingActionButtonDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSExtendedFloatingActionButton(
                text = "Add Item",
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
            )
        }
    }
}
