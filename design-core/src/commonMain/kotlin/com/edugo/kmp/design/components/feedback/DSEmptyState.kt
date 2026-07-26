package com.edugo.kmp.design.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.edugo.kmp.design.ContentWidth
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSTonalButton
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Estado **vacío** canónico del DS — patrón "Mensajería" generalizado (spec §7, D-051.3).
 *
 * Anatomía fija: icono 48dp `onSurfaceVariant` (o [illustration] cuando exista, §11) + título
 * `titleMedium` `onSurface` + explicación `bodyMedium` `onSurfaceVariant` + acción primaria
 * **tonal** opcional. Todo centrado y acotado a `ContentWidth.emptyState` (400dp).
 *
 * Regla de contenido: prohibido el guion "—" como estado vacío; siempre título + explicación
 * que digan qué falta y qué hacer (§9).
 *
 * Con [isError] `true` el mismo esqueleto sirve de estado de **error**: icono y acción pasan al
 * rol `error`/`errorContainer` (spec §7). Para el caso habitual usa el preset [DSErrorState].
 *
 * @param icon icono de fallback, 48dp. Se ignora si se pasa [illustration].
 * @param illustration slot de ilustración de marca (§11: lineal 2 tonos, 96-120dp). Los assets
 *   se producen fuera del plan 051; el slot ya queda disponible para cablearlos sin tocar la API.
 */
@Composable
fun DSEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    illustration: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
) {
    val accentColor =
        if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.spacing8),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = ContentWidth.emptyState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (illustration != null) {
                illustration()
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.iconXXLarge),
                    tint = accentColor,
                )
            }

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            if (description != null) {
                Spacer(Modifier.height(Spacing.spacing2))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(Spacing.spacing6))
                DSTonalButton(
                    text = actionLabel,
                    onClick = onAction,
                    containerColor =
                        if (isError) MaterialTheme.colorScheme.errorContainer else Color.Unspecified,
                    contentColor =
                        if (isError) MaterialTheme.colorScheme.onErrorContainer else Color.Unspecified,
                )
            }
        }
    }
}

/**
 * Estado de **error** (spec §7, D-051.3): el mismo esqueleto de [DSEmptyState] con tinte `error`
 * y una acción de reintento.
 *
 * Solo para errores que impiden ver el contenido. Un error de red NO destructivo (hay datos en
 * caché) no usa esta pantalla: se degrada a `StaleDataBanner` o snackbar.
 *
 * [retryLabel] no tiene default a propósito: los literales de UI se localizan en el consumidor,
 * design-core no fija copy.
 */
@Composable
fun DSErrorState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    illustration: (@Composable () -> Unit)? = null,
) = DSEmptyState(
    icon = icon,
    title = title,
    modifier = modifier,
    description = description,
    actionLabel = retryLabel,
    onAction = onRetry,
    illustration = illustration,
    isError = true,
)

// --- Previews ---

@Preview
@Composable
private fun DSEmptyStateBasicPreview() {
    DSTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Search,
                title = "Sin resultados",
                description = "No encontramos resultados para tu busqueda. Intenta con otros terminos.",
                actionLabel = "Limpiar filtros",
                onAction = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateNoActionPreview() {
    DSTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Info,
                title = "Aun no tienes cursos",
                description = "Cuando te inscribas en un curso aparecera aqui.",
            )
        }
    }
}

@Preview
@Composable
private fun DSErrorStatePreview() {
    DSTheme {
        Surface {
            DSErrorState(
                icon = Icons.Default.CloudOff,
                title = "No pudimos cargar los datos",
                description = "Revisa tu conexion e intenta nuevamente.",
                retryLabel = "Reintentar",
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateIllustrationPreview() {
    DSTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Info,
                title = "Bandeja vacia",
                description = "Cuando recibas un mensaje aparecera aqui.",
                illustration = {
                    // Placeholder del slot §11 hasta que existan los assets de ilustracion.
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(Sizes.iconMassive),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Search,
                title = "Sin resultados",
                description = "No encontramos resultados para tu busqueda.",
                actionLabel = "Limpiar filtros",
                onAction = {},
            )
        }
    }
}
