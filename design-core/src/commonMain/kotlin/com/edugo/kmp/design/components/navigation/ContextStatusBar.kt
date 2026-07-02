package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.media.DSVerticalDivider
import com.edugo.kmp.design.tokens.IconCatalog
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Barra de estado de contexto: pie informativo persistente que muestra el
 * contexto activo del usuario en formato `colegio · unidad · rol`.
 *
 * Es un componente **neutral** del design system: recibe los datos ya
 * resueltos (no accede a auth, Koin ni resources branded) y los callbacks que
 * deciden qué pasa al tocar cada segmento. Si un callback es `null` el segmento
 * correspondiente no es interactivo (p. ej. el rol, que no se cambia desde aquí).
 *
 * Decisión MD3: se usa `surfaceVariant` como fondo. Es la superficie tonal
 * pensada por Material 3 para barras informativas/contenedoras de baja
 * jerarquía; deja la barra de navegación inferior (que vive por encima con
 * elevación) como el elemento dominante, y mantiene contraste suficiente con el
 * `onSurfaceVariant` del texto en temas claro y oscuro. No se añade elevación
 * propia: este pie va al fondo de la pantalla y no debe competir visualmente con
 * la navegación.
 *
 * Estados:
 * - Si [schoolName] está en blanco y la barra es interactiva ([onSchoolClick]
 *   no nulo), el segmento colegio invita a elegir con [selectSchoolLabel].
 * - Idéntico para la unidad con [unitName]/[selectUnitLabel].
 *
 * Los íconos se resuelven vía [IconCatalog]: `school` (colegio), `layers`
 * (unidad — no existe un ícono apartment/building en el catálogo; `layers` es la
 * metáfora de sede/sección más cercana registrada) y `person` (rol).
 */
@Composable
fun ContextStatusBar(
    schoolName: String?,
    unitName: String?,
    roleLabel: String?,
    modifier: Modifier = Modifier,
    onSchoolClick: (() -> Unit)? = null,
    onUnitClick: (() -> Unit)? = null,
    // Etiquetas a mostrar cuando falta el dato (atenuadas). Neutras por defecto;
    // el consumidor branded pasa las cadenas localizadas.
    selectSchoolLabel: String = "Seleccionar colegio",
    selectUnitLabel: String = "Seleccionar unidad",
    // Descripciones de accesibilidad para los segmentos clickables.
    schoolActionLabel: String? = null,
    unitActionLabel: String? = null,
    // Descripción global de la barra (lectores de pantalla).
    contentDescription: String? = null,
) {
    val schoolIcon = IconCatalog.lookup("school", filled = false)
    val unitIcon = IconCatalog.lookup("layers", filled = false)
    val roleIcon = IconCatalog.lookup("person", filled = false)

    val rootModifier =
        modifier
            .fillMaxWidth()
            .let { base ->
                if (contentDescription != null) {
                    base.clearAndSetSemantics { this.contentDescription = contentDescription }
                } else {
                    base
                }
            }

    Surface(
        modifier = rootModifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    // El pie es el elemento MÁS BAJO de la pantalla: asume el
                    // safe-area inferior (home indicator iOS / barra de gestos
                    // Android). El fondo del Surface llega hasta el borde; solo el
                    // CONTENIDO se eleva por encima del indicador, evitando que el
                    // texto quede apretado contra el borde (antes "casi no cabía").
                    // La barra de navegación de arriba deja de reservar este inset,
                    // así se pega al pie sin el hueco muerto intermedio.
                    .navigationBarsPadding()
                    // Un poco más de alto que el mínimo táctil (48dp) para darle
                    // presencia como pie informativo persistente.
                    .heightIn(min = Spacing.spacing14)
                    .padding(horizontal = Spacing.spacing4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
            ContextSegment(
                icon = schoolIcon,
                text = schoolName?.takeIf { it.isNotBlank() } ?: selectSchoolLabel,
                emphasized = !schoolName.isNullOrBlank(),
                onClick = onSchoolClick,
                accessibilityLabel = schoolActionLabel,
                modifier = Modifier.weight(1f),
            )

            DSVerticalDivider(
                modifier = Modifier.height(Spacing.spacing5),
            )

            ContextSegment(
                icon = unitIcon,
                text = unitName?.takeIf { it.isNotBlank() } ?: selectUnitLabel,
                emphasized = !unitName.isNullOrBlank(),
                onClick = onUnitClick,
                accessibilityLabel = unitActionLabel,
                modifier = Modifier.weight(1f),
            )

            if (!roleLabel.isNullOrBlank()) {
                DSVerticalDivider(
                    modifier = Modifier.height(Spacing.spacing5),
                )
                ContextSegment(
                    icon = roleIcon,
                    text = roleLabel,
                    emphasized = true,
                    onClick = null,
                    accessibilityLabel = null,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

/**
 * Un segmento (ícono + texto) de la barra. Si [onClick] no es nulo el segmento
 * es interactivo (touch target completo de la fila). El texto se atenúa cuando
 * [emphasized] es `false` (estado "falta dato — invita a elegir").
 */
@Composable
private fun ContextSegment(
    icon: ImageVector?,
    text: String,
    emphasized: Boolean,
    onClick: (() -> Unit)?,
    accessibilityLabel: String?,
    modifier: Modifier = Modifier,
) {
    val interactiveModifier =
        if (onClick != null) {
            modifier
                .clickable(onClick = onClick)
                .let { base ->
                    if (accessibilityLabel != null) {
                        base.clearAndSetSemantics { contentDescription = accessibilityLabel }
                    } else {
                        base
                    }
                }
        } else {
            modifier
        }

    Row(
        modifier =
            interactiveModifier
                .heightIn(min = Sizes.TouchTarget.minimum)
                .padding(vertical = Spacing.spacing1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconSmall),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color =
                if (emphasized) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "ContextStatusBar - Light (completo)", showBackground = true)
@Composable
private fun ContextStatusBarPreviewFull() {
    DSTheme {
        ContextStatusBar(
            schoolName = "Colegio San Martín",
            unitName = "Sede Primaria",
            roleLabel = "Administrador",
            onSchoolClick = {},
            onUnitClick = {},
        )
    }
}

@Preview(name = "ContextStatusBar - Light (sin unidad)", showBackground = true)
@Composable
private fun ContextStatusBarPreviewMissingUnit() {
    DSTheme {
        ContextStatusBar(
            schoolName = "Colegio San Martín",
            unitName = null,
            roleLabel = "Docente",
            onSchoolClick = {},
            onUnitClick = {},
        )
    }
}

@Preview(name = "ContextStatusBar - Dark", showBackground = true)
@Composable
private fun ContextStatusBarPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        ContextStatusBar(
            schoolName = "Colegio San Martín",
            unitName = "Sede Secundaria",
            roleLabel = "Coordinador",
            onSchoolClick = {},
            onUnitClick = {},
        )
    }
}
