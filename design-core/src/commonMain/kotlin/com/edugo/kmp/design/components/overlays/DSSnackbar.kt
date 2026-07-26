package com.edugo.kmp.design.components.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ScreenDuration
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Snackbar con estilos consistentes segun el tipo de mensaje.
 * Usa colores semanticos del design system para cada variante.
 *
 * Es el canal de **exito** del patron de estados (spec §7, D-051.3): 4s, texto verbo + objeto
 * ("Usuario guardado"), nunca un dialogo de confirmacion. Tambien absorbe los errores de red NO
 * destructivos, que no deben escalar a pantalla de error.
 *
 * Para mostrarlo desde un flujo (guardar / eliminar) usa [showDSSnackbar] sobre el
 * `SnackbarHostState` del Scaffold + [DSSnackbarHost] como `snackbarHost`.
 *
 * Este composable solo PINTA: la duracion en pantalla la gobierna el `SnackbarHostState`
 * (via `DSSnackbarVisuals.duration` / [DSSnackbarDefaults.duration]), por eso no recibe
 * `duration` — un parametro aqui seria inerte.
 */
@Composable
fun DSSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val containerColor =
        when (messageType) {
            MessageType.INFO -> SemanticColors.infoContainer()
            MessageType.SUCCESS -> SemanticColors.successContainer()
            MessageType.WARNING -> SemanticColors.warningContainer()
            MessageType.ERROR -> SemanticColors.errorContainer()
        }

    val contentColor =
        when (messageType) {
            MessageType.INFO -> SemanticColors.onInfoContainer()
            MessageType.SUCCESS -> SemanticColors.onSuccessContainer()
            MessageType.WARNING -> SemanticColors.onWarningContainer()
            MessageType.ERROR -> SemanticColors.onErrorContainer()
        }

    Snackbar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        dismissAction =
            if (onDismiss != null) {
                {
                    TextButton(onClick = onDismiss) {
                        Text("X", color = contentColor)
                    }
                }
            } else {
                null
            },
        action =
            if (actionLabel != null && onAction != null) {
                {
                    TextButton(onClick = onAction) {
                        Text(actionLabel, color = contentColor)
                    }
                }
            } else {
                null
            },
    ) {
        Text(message)
    }
}

/**
 * `SnackbarVisuals` del DS: transporta el [messageType] a traves del `SnackbarHostState`, que en
 * Material3 solo conoce message/actionLabel/duration.
 *
 * Sin esto, [DSSnackbarHost] tendria que pintar todos los snackbars de una pantalla con el mismo
 * tono; con esto, cada emision elige el suyo (exito al guardar, error al fallar la red).
 */
class DSSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = DSSnackbarDefaults.duration,
    val messageType: MessageType = MessageType.INFO,
) : SnackbarVisuals

/**
 * Muestra un snackbar del DS. Atajo para el estado de **exito** de los flujos guardar/eliminar
 * (spec §7): `hostState.showDSSnackbar("Usuario guardado")`.
 *
 * Microcopy obligatorio (§9): **verbo + objeto**, sentence case, sin jerga tecnica.
 * La duracion por defecto es `SnackbarDuration.Short` = 4000ms = [ScreenDuration.snackbar].
 */
suspend fun SnackbarHostState.showDSSnackbar(
    message: String,
    messageType: MessageType = MessageType.SUCCESS,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = DSSnackbarDefaults.duration,
): SnackbarResult =
    showSnackbar(
        DSSnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration,
            messageType = messageType,
        ),
    )

/**
 * Host para mostrar snackbars con el estilo del design system.
 *
 * Si la emision usa [DSSnackbarVisuals] se respeta su `messageType`; si es un
 * `showSnackbar(String)` plano se cae a [messageType] (compatibilidad con los call sites previos).
 */
@Composable
fun DSSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.INFO,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        val visuals = snackbarData.visuals
        DSSnackbar(
            message = visuals.message,
            messageType = (visuals as? DSSnackbarVisuals)?.messageType ?: messageType,
            actionLabel = visuals.actionLabel,
            onAction = { snackbarData.performAction() },
        )
    }
}

/**
 * Tiempos canonicos del snackbar del DS (spec §7, D-051.3).
 */
object DSSnackbarDefaults {
    /** 4s: el tiempo del snackbar de confirmacion. Alias de [ScreenDuration.snackbar]. */
    const val durationMillis: Long = ScreenDuration.snackbar

    /**
     * Equivalente Material3 de [durationMillis]: `SnackbarDuration.Short` dura 4000ms, por lo que
     * el host no necesita temporizador propio.
     */
    val duration: SnackbarDuration = SnackbarDuration.Short
}

// --- Previews ---

@Preview
@Composable
private fun DSSnackbarInfoPreview() {
    DSTheme {
        Surface {
            DSSnackbar(
                message = "Informacion actualizada correctamente",
                messageType = MessageType.INFO,
                actionLabel = "Ver",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarSuccessPreview() {
    DSTheme {
        Surface {
            DSSnackbar(
                message = "Guardado exitosamente",
                messageType = MessageType.SUCCESS,
                actionLabel = "Deshacer",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarWarningPreview() {
    DSTheme {
        Surface {
            DSSnackbar(
                message = "Conexion inestable",
                messageType = MessageType.WARNING,
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarErrorPreview() {
    DSTheme {
        Surface {
            DSSnackbar(
                message = "Error al guardar los cambios",
                messageType = MessageType.ERROR,
                actionLabel = "Reintentar",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarAllVariantsPreview() {
    DSTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                MessageType.entries.forEach { type ->
                    DSSnackbar(
                        message = "Mensaje de tipo ${type.name}",
                        messageType = type,
                        actionLabel = "Accion",
                        onAction = {},
                    )
                    Spacer(Modifier.height(Spacing.spacing2))
                }
            }
        }
    }
}

@Preview
@Composable
private fun DSSnackbarAllVariantsDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                MessageType.entries.forEach { type ->
                    DSSnackbar(
                        message = "Mensaje de tipo ${type.name}",
                        messageType = type,
                        actionLabel = "Accion",
                        onAction = {},
                    )
                    Spacer(Modifier.height(Spacing.spacing2))
                }
            }
        }
    }
}
