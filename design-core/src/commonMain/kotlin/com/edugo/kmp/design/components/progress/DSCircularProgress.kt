package com.edugo.kmp.design.components.progress

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Indicador circular, determinado si llega [progress] e indeterminado si no.
 *
 * [size] es el diámetro. Se aplica por dentro del [modifier] del que llama, así que un
 * `Modifier.size(...)` explícito sigue mandando.
 */
@Composable
fun DSCircularProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    size: Dp = Sizes.progressLarge,
) {
    val sized = modifier.size(size)
    if (progress != null) {
        CircularProgressIndicator(
            progress = progress,
            modifier = sized,
        )
    } else {
        CircularProgressIndicator(modifier = sized)
    }
}

@Preview(name = "DSCircularProgress - Light", showBackground = true)
@Composable
fun DSCircularProgressPreviewLight() {
    DSTheme {
        Surface {
            DSCircularProgress()
        }
    }
}

@Preview(name = "DSCircularProgress - Dark", showBackground = true)
@Composable
private fun DSCircularProgressPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSCircularProgress()
        }
    }
}
