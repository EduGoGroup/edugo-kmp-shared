package com.edugo.kmp.design.components.progress

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSLinearProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
        )
    } else {
        LinearProgressIndicator(modifier = modifier)
    }
}

@Preview(name = "DSLinearProgress - Light", showBackground = true)
@Composable
fun DSLinearProgressPreviewLight() {
    DSTheme {
        Surface {
            DSLinearProgress()
        }
    }
}

@Preview(name = "DSLinearProgress - Dark", showBackground = true)
@Composable
private fun DSLinearProgressPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSLinearProgress()
        }
    }
}
