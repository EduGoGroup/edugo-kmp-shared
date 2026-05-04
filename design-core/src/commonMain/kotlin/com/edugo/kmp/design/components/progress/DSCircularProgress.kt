package com.edugo.kmp.design.components.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSCircularProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    size: Dp = Sizes.progressLarge,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = progress,
            modifier = modifier,
        )
    } else {
        CircularProgressIndicator(modifier = modifier)
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
