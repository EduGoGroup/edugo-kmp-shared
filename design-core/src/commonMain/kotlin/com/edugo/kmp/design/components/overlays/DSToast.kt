package com.edugo.kmp.design.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ScreenDuration
import com.edugo.kmp.design.tokens.Shapes
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = ScreenDuration.toastShort,
    alignment: Alignment = Alignment.BottomCenter,
) {
    if (visible) {
        LaunchedEffect(message) {
            delay(durationMs)
            onDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(alignment).padding(Spacing.spacing4),
        ) {
            Surface(
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            ) {
                Text(
                    text = message,
                    modifier =
                        Modifier.padding(
                            horizontal = Spacing.spacing6,
                            vertical = Spacing.spacing3,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(name = "DSToast - Light", showBackground = true)
@Composable
fun DSToastPreviewLight() {
    DSTheme {
        Surface {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                DSToast(
                    message = "Mensaje de ejemplo",
                    visible = true,
                    onDismiss = {},
                )
            }
        }
    }
}

@Preview(name = "DSToast - Dark", showBackground = true)
@Composable
private fun DSToastPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                DSToast(
                    message = "Mensaje de ejemplo",
                    visible = true,
                    onDismiss = {},
                )
            }
        }
    }
}
