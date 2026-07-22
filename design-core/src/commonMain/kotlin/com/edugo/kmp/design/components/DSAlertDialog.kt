package com.edugo.kmp.design.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors
import com.edugo.kmp.design.components.dialogs.DSModalScaffold
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Dialog de alerta con estilos consistentes según el tipo de mensaje, sobre el andamiaje común
 * [DSModalScaffold]: header sticky con icono semántico + cierre siempre visible, cuerpo scrolleable
 * que nunca recorta y footer sticky (spec §5.6, D-048.6). API pública sin cambios.
 */
@Composable
fun DSAlertDialog(
    title: String,
    message: String,
    type: MessageType = MessageType.INFO,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector =
        when (type) {
            MessageType.INFO -> Icons.Default.Info
            MessageType.SUCCESS -> Icons.Default.CheckCircle
            MessageType.WARNING -> Icons.Default.Warning
            MessageType.ERROR -> Icons.Default.Error
        }

    val iconColor =
        when (type) {
            MessageType.INFO -> SemanticColors.info()
            MessageType.SUCCESS -> SemanticColors.success()
            MessageType.WARNING -> SemanticColors.warning()
            MessageType.ERROR -> SemanticColors.error()
        }

    DSModalScaffold(
        title = title,
        onDismiss = onDismiss,
        modifier = modifier,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )
        },
        footer = {
            if (dismissText != null) {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) {
                Text(confirmText)
            }
        },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "DSAlertDialog - Light")
@Composable
fun DSAlertDialogPreview() {
    DSTheme {
        DSAlertDialog(
            title = "Información",
            message = "Este es un mensaje de información.",
            type = MessageType.INFO,
            confirmText = "Aceptar",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "DSAlertDialog - Dark")
@Composable
fun DSAlertDialogDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSAlertDialog(
            title = "Información",
            message = "Este es un mensaje de información.",
            type = MessageType.INFO,
            confirmText = "Aceptar",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
