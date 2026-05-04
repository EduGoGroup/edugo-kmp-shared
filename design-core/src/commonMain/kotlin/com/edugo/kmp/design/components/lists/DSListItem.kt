package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSListItem(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    overlineText: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(headlineText) },
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        supportingContent = supportingText?.let { { Text(it) } },
        overlineContent = overlineText?.let { { Text(it) } },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    )
}

@Preview(name = "DSListItem - Light", showBackground = true)
@Composable
fun DSListItemPreviewLight() {
    DSTheme {
        Surface {
            DSListItem(
                headlineText = "Elemento",
                supportingText = "Detalle adicional",
                overlineText = "Seccion",
            )
        }
    }
}

@Preview(name = "DSListItem - Dark", showBackground = true)
@Composable
private fun DSListItemPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSListItem(
                headlineText = "Elemento",
                supportingText = "Detalle adicional",
                overlineText = "Seccion",
            )
        }
    }
}
