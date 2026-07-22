package com.edugo.kmp.design.components.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.AnimationDuration
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Fila expandible del design system EduGo.
 *
 * Envuelve [DSListRow] (la fila canónica del DS: `Card` tonal `surfaceContainer`
 * con borde, slots de leading/headline/supporting/trailing) y reemplaza su slot
 * `trailing` por un chevron animado que rota 180 grados al alternar `expanded`.
 *
 * El click sobre toda la fila dispara [onToggle]; el contenido revelado se
 * renderiza debajo dentro de un `AnimatedVisibility`.
 */
@Composable
fun DSExpandableListItem(
    headlineContent: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    expandedContent: @Composable () -> Unit = {},
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
        label = "chevron_rotation",
    )

    Column(modifier = modifier) {
        DSListRow(
            headlineText = headlineContent,
            supportingText = supportingContent,
            leading = leadingContent,
            trailing = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    modifier = Modifier.rotate(rotationAngle),
                )
            },
            onClick = onToggle,
        )

        AnimatedVisibility(
            visible = expanded,
            enter =
                expandVertically(
                    animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
                ),
            exit =
                shrinkVertically(
                    animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
                ),
        ) {
            expandedContent()
        }
    }
}

// --- Previews ---

@Preview
@Composable
private fun DSExpandableListItemCollapsedPreview() {
    DSTheme {
        Surface {
            DSExpandableListItem(
                headlineContent = "Notificaciones",
                supportingContent = "Configurar alertas y sonidos",
                expanded = false,
                onToggle = {},
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun DSExpandableListItemExpandedPreview() {
    DSTheme {
        Surface {
            DSExpandableListItem(
                headlineContent = "Notificaciones",
                supportingContent = "Configurar alertas y sonidos",
                expanded = true,
                onToggle = {},
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                    )
                },
            ) {
                Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                    DSListRow(
                        headlineText = "Push",
                        trailing = { Text("Activado") },
                    )
                    DSListRow(
                        headlineText = "Email",
                        trailing = { Text("Desactivado") },
                    )
                    DSListRow(
                        headlineText = "Sonido",
                        trailing = { Text("Activado") },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DSExpandableListItemDarkPreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column {
                DSExpandableListItem(
                    headlineContent = "Apariencia",
                    expanded = true,
                    onToggle = {},
                ) {
                    Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                        DSListRow(headlineText = "Tema: Oscuro")
                        DSListRow(headlineText = "Tamano de texto: Normal")
                    }
                }
                DSExpandableListItem(
                    headlineContent = "Privacidad",
                    supportingContent = "Permisos y seguridad",
                    expanded = false,
                    onToggle = {},
                )
            }
        }
    }
}
