package com.edugo.kmp.design.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSBasicDialog(
    title: String,
    text: String,
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(confirmText)
            }
        },
        dismissButton =
            if (dismissText != null) {
                {
                    TextButton(onClick = {
                        onDismiss?.invoke()
                        onDismissRequest()
                    }) {
                        Text(dismissText)
                    }
                }
            } else {
                null
            },
        icon = icon,
        modifier = modifier,
    )
}

@Preview(name = "DSBasicDialog - Light", showBackground = true)
@Composable
fun DSBasicDialogPreviewLight() {
    DSTheme {
        DSBasicDialog(
            title = "Titulo",
            text = "Contenido del dialogo",
            confirmText = "Aceptar",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}

@Preview(name = "DSBasicDialog - Dark", showBackground = true)
@Composable
private fun DSBasicDialogPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSBasicDialog(
            title = "Titulo",
            text = "Contenido del dialogo",
            confirmText = "Aceptar",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}
