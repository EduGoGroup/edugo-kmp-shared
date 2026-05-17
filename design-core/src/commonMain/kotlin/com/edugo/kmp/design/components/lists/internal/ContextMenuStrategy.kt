package com.edugo.kmp.design.components.lists.internal

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import com.edugo.kmp.design.components.lists.RowAction
import com.edugo.kmp.design.platform.PlatformDetector
import com.edugo.kmp.design.platform.PlatformType

@Composable
internal fun ContextMenuStrategy(
    actions: List<RowAction>,
    onAction: (RowAction) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchorOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val platform = PlatformDetector.current
    val isTouch = platform == PlatformType.ANDROID || platform == PlatformType.IOS
    val isPointer = !isTouch

    val gestureModifier = when {
        // Long-press cooperativo: `awaitLongPressOrCancellation` NO consume el
        // down inicial, así el `SwipeToDismissBox` padre puede arrancar el drag
        // horizontal sin pelearse por el gesto. Si el usuario mueve el dedo
        // antes del timeout, la espera se cancela y el swipe procede normal.
        isTouch -> Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val longPress = awaitLongPressOrCancellation(down.id)
                if (longPress != null) {
                    anchorOffset = longPress.position
                    menuOpen = true
                }
            }
        }
        else -> Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                        anchorOffset = event.changes.firstOrNull()?.position ?: Offset.Zero
                        menuOpen = true
                    }
                }
            }
        }
    }

    // En desktop/web sin mouse, Shift+F10 o la tecla Menu abren el menú
    // contextual (no hay long-press y el right-click no es accesible).
    val keyboardModifier = if (isPointer) {
        Modifier
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val triggers = event.key == Key.Menu ||
                    (event.isShiftPressed && event.key == Key.F10)
                if (triggers) {
                    anchorOffset = Offset.Zero
                    menuOpen = true
                    true
                } else false
            }
    } else Modifier

    val (normal, destructive) = actions.partition { !it.destructive }

    Box(modifier = gestureModifier.then(keyboardModifier)) {
        content(Modifier)
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            offset = with(density) {
                DpOffset(
                    x = anchorOffset.x.toDp(),
                    y = anchorOffset.y.toDp(),
                )
            },
        ) {
            normal.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    leadingIcon = { Icon(action.icon, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        onAction(action)
                    },
                )
            }
            if (destructive.isNotEmpty() && normal.isNotEmpty()) {
                HorizontalDivider()
            }
            destructive.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label, color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            action.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuOpen = false
                        onAction(action)
                    },
                )
            }
        }
    }
}
