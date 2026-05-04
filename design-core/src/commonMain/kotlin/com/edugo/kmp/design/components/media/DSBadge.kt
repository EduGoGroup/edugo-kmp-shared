package com.edugo.kmp.design.components.media

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    content: @Composable () -> Unit,
) {
    BadgedBox(
        badge = {
            if (count != null && count > 0) {
                Badge { Text(if (count > 99) "99+" else count.toString()) }
            } else if (count == null) {
                Badge()
            }
        },
        modifier = modifier,
        content = { content() },
    )
}

@Preview(name = "DSBadge - Light", showBackground = true)
@Composable
fun DSBadgePreviewLight() {
    DSTheme {
        Surface {
            Row {
                DSBadge(count = 3) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                }
                Spacer(Modifier.width(Spacing.spacing4))
                DSBadge(count = null) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                }
            }
        }
    }
}

@Preview(name = "DSBadge - Dark", showBackground = true)
@Composable
private fun DSBadgePreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Row {
                DSBadge(count = 3) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                }
                Spacer(Modifier.width(Spacing.spacing4))
                DSBadge(count = null) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                }
            }
        }
    }
}
