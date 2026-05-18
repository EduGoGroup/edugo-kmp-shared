package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ButtonSpacing

/**
 * Layout interno compartido por los DS*Button.
 * Pinta:  [leadingIcon | progressIndicator] · Text(ellipsis) · [trailingIcon]
 * Spacers entre íconos y texto solo cuando aplica.
 */
@Composable
internal fun ButtonContent(
    text: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    loading: Boolean = false,
) {
    when {
        loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.iconMedium),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
        leadingIcon != null -> {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(Sizes.iconMedium))
            Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        }
    }
    Text(text, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
    if (trailingIcon != null) {
        Spacer(Modifier.width(ButtonSpacing.iconSpacing))
        Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(Sizes.iconMedium))
    }
}
