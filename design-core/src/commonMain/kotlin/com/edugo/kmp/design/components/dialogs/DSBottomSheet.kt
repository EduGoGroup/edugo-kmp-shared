package com.edugo.kmp.design.components.dialogs

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet modal del design system: punto único por el que pasan todas las hojas de la app.
 *
 * Motion (spec §8, D-051.4): la entrada desde abajo (~250ms) y el drag-to-dismiss con física
 * nativa los aporta `ModalBottomSheet`/`SheetState` de Material 3 y **no se sobrescriben** — el
 * componente no expone un `animationSpec` y su animación anclada (`AnchoredDraggable`) es
 * exactamente el "comportamiento nativo" que pide la tabla de movimiento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dragHandle: @Composable (() -> Unit)? = {
        androidx.compose.material3.BottomSheetDefaults
            .DragHandle()
    },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = dragHandle,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSBottomSheet - Light", showBackground = true)
@Composable
fun DSBottomSheetPreviewLight() {
    DSTheme {
        DSBottomSheet(onDismissRequest = {}) {
            Text(
                text = "Contenido del bottom sheet",
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSBottomSheet - Dark", showBackground = true)
@Composable
private fun DSBottomSheetPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSBottomSheet(onDismissRequest = {}) {
            Text(
                text = "Contenido del bottom sheet",
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}
