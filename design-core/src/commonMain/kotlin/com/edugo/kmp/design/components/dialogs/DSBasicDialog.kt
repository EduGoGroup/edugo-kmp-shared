package com.edugo.kmp.design.components.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Diálogo básico (título + texto + acciones) sobre el andamiaje común [DSModalScaffold]:
 * header sticky con cierre siempre visible, cuerpo scrolleable que nunca recorta y footer sticky
 * (spec §5.6, D-048.6). API pública sin cambios respecto a versiones previas.
 */
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
    DSModalScaffold(
        title = title,
        onDismiss = onDismissRequest,
        modifier = modifier,
        leadingIcon = icon,
        footer = {
            if (dismissText != null) {
                TextButton(onClick = {
                    onDismiss?.invoke()
                    onDismissRequest()
                }) {
                    Text(dismissText)
                }
            }
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(confirmText)
            }
        },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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
