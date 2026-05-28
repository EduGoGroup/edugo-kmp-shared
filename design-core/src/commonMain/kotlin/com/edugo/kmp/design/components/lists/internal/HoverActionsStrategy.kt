package com.edugo.kmp.design.components.lists.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.lists.RowAction

@Composable
internal fun HoverActionsStrategy(
    actions: List<RowAction>,
    onAction: (RowAction) -> Unit,
    modifier: Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    // hovered/focused state is local to this wrapper — content lambda is not in its
    // read scope, so hover changes do not recompose the row content.
    var hovered by remember { mutableStateOf(false) }
    var focusWithin by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .onFocusChanged { focusWithin = it.hasFocus }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> hovered = true
                                PointerEventType.Exit -> hovered = false
                            }
                        }
                    }
                },
    ) {
        content(Modifier.fillMaxWidth())

        // Visible también cuando algún elemento del row tiene foco — usuarios
        // solo-teclado (sin mouse) no disparan hover y quedarían sin acceso.
        AnimatedVisibility(
            visible = hovered || focusWithin,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = Spacing.spacing3),
            enter = fadeIn(animationSpec = tween(durationMillis = 120, easing = EaseInOut)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120, easing = EaseInOut)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1)) {
                actions.forEach { action ->
                    DSIconButton(
                        icon = action.icon,
                        contentDescription = action.label,
                        onClick = { onAction(action) },
                    )
                }
            }
        }
    }
}
