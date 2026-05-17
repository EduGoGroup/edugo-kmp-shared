package com.edugo.kmp.design.components.lists

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.components.lists.internal.ConfirmDialog
import com.edugo.kmp.design.components.lists.internal.ContextMenuStrategy
import com.edugo.kmp.design.components.lists.internal.HoverActionsStrategy
import com.edugo.kmp.design.components.lists.internal.SwipeActionsStrategy
import com.edugo.kmp.design.components.lists.internal.rememberConfirmDialogState
import com.edugo.kmp.design.platform.PlatformDetector
import com.edugo.kmp.design.platform.PlatformType

@Composable
fun DSAdaptiveActionsHost(
    actions: List<RowAction>,
    modifier: Modifier = Modifier,
    onAction: (RowAction) -> Unit = { it.onInvoke() },
    content: @Composable (rowModifier: Modifier) -> Unit,
) {
    if (actions.isEmpty()) {
        content(modifier)
        return
    }

    // Nota: la estabilidad de `actions` se garantiza aguas arriba (ver
    // ListPatternRenderer / ZoneRenderer, que memoizan la lista por item).
    // Aquí no se memoiza adicionalmente porque `RowAction` contiene lambdas
    // y el `remember(actions)` quedaría inerte si el caller no estabiliza.
    val confirmDialog = rememberConfirmDialogState()

    val onIntercepted: (RowAction) -> Unit = { action ->
        if (action.destructive) {
            confirmDialog.request(action) { onAction(action) }
        } else {
            onAction(action)
        }
    }

    val withContextMenu: @Composable (Modifier) -> Unit = { rowMod ->
        ContextMenuStrategy(
            actions = actions,
            onAction = onIntercepted,
        ) { menuModifier ->
            content(rowMod.then(menuModifier))
        }
    }

    when (PlatformDetector.current) {
        PlatformType.ANDROID, PlatformType.IOS -> SwipeActionsStrategy(
            actions = actions,
            onAction = onIntercepted,
            modifier = modifier,
        ) { swipeModifier ->
            withContextMenu(swipeModifier)
        }
        PlatformType.DESKTOP, PlatformType.WEB -> HoverActionsStrategy(
            actions = actions,
            onAction = onIntercepted,
            modifier = modifier,
        ) { hoverModifier ->
            withContextMenu(hoverModifier)
        }
    }

    ConfirmDialog(state = confirmDialog)
}
