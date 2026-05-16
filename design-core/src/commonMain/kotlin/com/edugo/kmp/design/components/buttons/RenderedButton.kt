package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.tokens.ButtonVariant
import com.edugo.kmp.design.tokens.ColorRoleHint
import com.edugo.kmp.design.tokens.RenderToken

/**
 * Renderiza un boton SDUI segun el [RenderToken] resuelto por
 * `ButtonStyleCatalog.lookup(style)`. Una sola entrada para el SDUI:
 * el renderer no decide variante, solo aplica el token.
 *
 * Comportamiento:
 * - [RenderToken.variant] == ICON -> [IconButton] con [icon].
 * - FILLED -> [DSFilledButton] con icono opcional + label.
 * - OUTLINED -> [DSOutlinedButton] con icono opcional + label.
 * - TEXT -> [DSTextButton] con icono opcional + label.
 * - DESTRUCTIVE_OUTLINED -> [DSOutlinedButton] con tint error + icono
 *   opcional + label.
 *
 * Si [icon] y [label] son ambos null, no renderiza nada (degradado
 * silencioso, no crash). Esto cubre el caso de un slot mal sembrado.
 *
 * [contentDescriptionFallback] se usa cuando [label] es null para
 * cumplir accesibilidad (lectores de pantalla). El caller puede pasar
 * el `slot.id` u otro identificador legible.
 */
@Composable
fun RenderedButton(
    token: RenderToken,
    icon: ImageVector?,
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescriptionFallback: String? = null,
) {
    if (icon == null && label == null) return

    val tint = token.tint.resolve()
    val contentDescription = label ?: contentDescriptionFallback

    when (token.variant) {
        ButtonVariant.ICON -> {
            IconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(Sizes.iconLarge),
                        tint = if (tint != Color.Unspecified) tint else LocalContentColor.current,
                    )
                }
            }
        }
        ButtonVariant.FILLED -> {
            DSFilledButton(
                text = label.orEmpty(),
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = icon,
                contentColor = tint,
            )
        }
        ButtonVariant.OUTLINED -> {
            DSOutlinedButton(
                text = label.orEmpty(),
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = icon,
                contentColor = tint,
            )
        }
        ButtonVariant.TEXT -> {
            DSTextButton(
                text = label.orEmpty(),
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = icon,
                contentColor = tint,
            )
        }
        ButtonVariant.DESTRUCTIVE_OUTLINED -> {
            DSOutlinedButton(
                text = label.orEmpty(),
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = icon,
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Resuelve el rol de color a un [Color] real del ColorScheme. `null` ->
 * [Color.Unspecified] (cae al default de Material3 para esa variante).
 */
@Composable
private fun ColorRoleHint?.resolve(): Color =
    when (this) {
        null -> Color.Unspecified
        ColorRoleHint.PRIMARY -> MaterialTheme.colorScheme.primary
        ColorRoleHint.ON_PRIMARY -> MaterialTheme.colorScheme.onPrimary
        ColorRoleHint.ERROR -> MaterialTheme.colorScheme.error
        ColorRoleHint.ON_ERROR -> MaterialTheme.colorScheme.onError
        ColorRoleHint.OUTLINE -> MaterialTheme.colorScheme.outline
    }

