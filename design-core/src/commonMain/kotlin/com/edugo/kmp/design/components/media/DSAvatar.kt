package com.edugo.kmp.design.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSAvatar(
    modifier: Modifier = Modifier,
    size: Dp = Sizes.Avatar.large,
    initials: String? = null,
    icon: ImageVector? = null,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        when {
            initials != null -> {
                Text(
                    text = initials.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size / 2),
                )
            }
        }
    }
}

@Preview(name = "DSAvatar - Light", showBackground = true)
@Composable
fun DSAvatarPreviewLight() {
    DSTheme {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DSAvatar(initials = "JP")
                Spacer(Modifier.width(Spacing.spacing3))
                DSAvatar(icon = Icons.Filled.Person)
            }
        }
    }
}

@Preview(name = "DSAvatar - Dark", showBackground = true)
@Composable
private fun DSAvatarPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DSAvatar(initials = "JP")
                Spacer(Modifier.width(Spacing.spacing3))
                DSAvatar(icon = Icons.Filled.Person)
            }
        }
    }
}
