package com.edugo.kmp.design.components.overlays

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Elevation
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.Shapes
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSPopup(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0),
    focusable: Boolean = true,
    content: @Composable () -> Unit,
) {
    Popup(
        alignment = alignment,
        offset = offset,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = focusable),
    ) {
        Surface(
            modifier = modifier.padding(Spacing.spacing2),
            shape = Shapes.medium,
            shadowElevation = Elevation.menu,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            content()
        }
    }
}

@Preview(name = "DSPopup - Light", showBackground = true)
@Composable
fun DSPopupPreviewLight() {
    DSTheme {
        Surface {
            DSPopup(
                onDismissRequest = {},
                alignment = Alignment.Center,
            ) {
                Text("Contenido")
            }
        }
    }
}

@Preview(name = "DSPopup - Dark", showBackground = true)
@Composable
private fun DSPopupPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSPopup(
                onDismissRequest = {},
                alignment = Alignment.Center,
            ) {
                Text("Contenido")
            }
        }
    }
}
