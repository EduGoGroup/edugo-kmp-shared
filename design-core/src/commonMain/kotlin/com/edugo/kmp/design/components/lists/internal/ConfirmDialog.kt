package com.edugo.kmp.design.components.lists.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.components.DSAlertDialog
import com.edugo.kmp.design.components.lists.RowAction

internal class ConfirmDialogState {
    private var pendingAction: RowAction? by mutableStateOf(null)
    private var pendingConfirm: (() -> Unit)? = null

    val isVisible: Boolean get() = pendingAction != null
    val action: RowAction? get() = pendingAction

    fun request(
        action: RowAction,
        onConfirm: () -> Unit,
    ) {
        pendingAction = action
        pendingConfirm = onConfirm
    }

    fun confirm() {
        pendingConfirm?.invoke()
        dismiss()
    }

    fun dismiss() {
        pendingAction = null
        pendingConfirm = null
    }
}

@Composable
internal fun rememberConfirmDialogState(): ConfirmDialogState = remember { ConfirmDialogState() }

/**
 * Confirmación de una acción de fila. Delega en [DSAlertDialog], por lo que hereda el andamiaje
 * §5.6 (D-048.6): botón de cierre siempre visible y cuerpo que nunca recorta. Al ser un confirm
 * de dos líneas no necesita más andamiaje que ese; el modal se renderiza compacto sin scroll.
 */
@Composable
internal fun ConfirmDialog(state: ConfirmDialogState) {
    val action = state.action ?: return
    DSAlertDialog(
        title = "¿Confirmar acción?",
        message = "¿Estás seguro de que deseas ${action.label.lowercase()}?",
        type = if (action.destructive) MessageType.ERROR else MessageType.WARNING,
        confirmText = "Confirmar",
        dismissText = "Cancelar",
        onConfirm = { state.confirm() },
        onDismiss = { state.dismiss() },
    )
}
