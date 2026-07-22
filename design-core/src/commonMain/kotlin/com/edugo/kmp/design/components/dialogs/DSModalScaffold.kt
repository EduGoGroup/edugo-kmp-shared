package com.edugo.kmp.design.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.DialogDefaults
import com.edugo.kmp.design.Elevation
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.Shapes
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Andamiaje común de modales según spec §5.6 (D-048.6). Un solo patrón de modal para todo el
 * design system: muere la dualidad "tarjeta sin X" vs "hoja con X".
 *
 * Anatomía:
 * - Ancho `min([DialogDefaults.maxWidth], 100% − 2·[DialogDefaults.windowMargin])`.
 * - Alto máximo `viewport − 2·[DialogDefaults.windowMargin]`; **el cuerpo nunca recorta**: si el
 *   contenido no cabe, scrollea dentro de ese alto.
 * - Header sticky de [DialogDefaults.headerHeight] con título `titleLarge` + botón de cierre (X)
 *   **siempre visible** (`showClose`).
 * - Cuerpo scrolleable con indicador (divisores que aparecen cuando hay más contenido arriba/abajo).
 * - Footer sticky de [DialogDefaults.footerHeight] para las acciones.
 * - Scrim [DialogDefaults.scrimOpacity] aportado por `Dialog` (sin cambios respecto a MD3).
 */
@Composable
fun DSModalScaffold(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    showClose: Boolean = true,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    footer: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = properties) {
        Box(
            modifier = Modifier.fillMaxSize().padding(DialogDefaults.windowMargin),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = modifier.widthIn(max = DialogDefaults.maxWidth).fillMaxWidth(),
                shape = Shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = Elevation.dialog,
            ) {
                BoxWithConstraints {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
                        ModalHeader(
                            title = title,
                            leadingIcon = leadingIcon,
                            showClose = showClose,
                            onDismiss = onDismiss,
                        )
                        if (scrollState.canScrollBackward) HorizontalDivider()
                        Column(
                            modifier =
                                Modifier
                                    .weight(weight = 1f, fill = false)
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(
                                        horizontal = Spacing.spacing6,
                                        vertical = Spacing.spacing4,
                                    ),
                            content = body,
                        )
                        if (footer != null) {
                            if (scrollState.canScrollForward) HorizontalDivider()
                            ModalFooter(content = footer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalHeader(
    title: String,
    leadingIcon: @Composable (() -> Unit)?,
    showClose: Boolean,
    onDismiss: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(DialogDefaults.headerHeight)
                .padding(start = Spacing.spacing6, end = Spacing.spacing2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
    ) {
        leadingIcon?.invoke()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (showClose) {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
private fun ModalFooter(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(DialogDefaults.footerHeight)
                .padding(horizontal = Spacing.spacing6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2, Alignment.End),
        content = content,
    )
}

@Preview(name = "DSModalScaffold - Light", showBackground = true)
@Composable
private fun DSModalScaffoldPreviewLight() {
    DSTheme {
        DSModalScaffold(
            title = "Título del modal",
            onDismiss = {},
            footer = {
                Text("Cancelar")
                Text("Aceptar")
            },
        ) {
            Text("Contenido del cuerpo del modal.")
        }
    }
}

@Preview(name = "DSModalScaffold - Dark", showBackground = true)
@Composable
private fun DSModalScaffoldPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        DSModalScaffold(
            title = "Título del modal",
            onDismiss = {},
            footer = {
                Text("Cancelar")
                Text("Aceptar")
            },
        ) {
            Text("Contenido del cuerpo del modal.")
        }
    }
}
