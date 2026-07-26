package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing

/**
 * Densidad de [DSListRow] (spec §6, D-051.1).
 *
 * Son **dos** densidades decididas por el SISTEMA (no hay toggle de usuario en v1): el renderer
 * elige por heurística — listas largas de trabajo (usuarios, preguntas de evaluación, auditoría)
 * en Expanded/Large → [COMPACT]; todo lo demás → [COMFORTABLE].
 *
 * design-core no conoce la heurística: solo aplica la geometría de la densidad que le pasan.
 */
enum class DSListRowDensity {
    /** Fila de 72dp a dos líneas, padding `spacing4` (spec §5.1). Es la default. */
    COMFORTABLE,

    /** Fila de 56dp a una línea con subtítulo inline `·`, padding `spacing3` (spec §6). */
    COMPACT,
}

/**
 * Bloque de texto de la fila: despacha a la disposición de la [density] recibida.
 *
 * `COMFORTABLE` apila título sobre subtítulo (dos líneas); `COMPACT` los pone en la misma línea
 * separados por `·` (spec §6/§9) para que la fila quepa en 56dp.
 */
@Composable
internal fun DSListRowTexts(
    modifier: Modifier,
    density: DSListRowDensity,
    headline: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
) {
    when (density) {
        DSListRowDensity.COMFORTABLE -> ComfortableTexts(modifier, headline, supporting)
        DSListRowDensity.COMPACT -> CompactTexts(modifier, headline, supporting)
    }
}

/** Título `bodyLarge` sobre subtítulo `bodyMedium` `onSurfaceVariant` (spec §5.1). */
@Composable
private fun ComfortableTexts(
    modifier: Modifier,
    headline: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
) {
    Column(
        modifier = modifier,
        // excepción deuda-051: 2dp de interlínea título/subtítulo; fuera de la escala Spacing
        // (el vecino 4dp desbordaría el alto canónico de 72dp de la fila cómoda).
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
            headline()
        }
        if (supporting != null) {
            ProvideTextStyle(
                MaterialTheme.typography.bodyMedium.copy(
                    color = DSListRowDefaults.supportingColor,
                ),
            ) {
                supporting()
            }
        }
    }
}

/**
 * Una sola línea: `título · subtítulo`. Ambos bloques van con `weight(fill = false)` para que
 * cada uno se elipse dentro de su mitad en vez de empujar al otro fuera de la fila.
 *
 * El consumidor puede además pasar un slot ya colapsado a una línea (p. ej. el `CompactHeadline`
 * de kmp-screens); en ese caso `supporting` viene `null` y aquí solo se pinta el título.
 */
@Composable
private fun CompactTexts(
    modifier: Modifier,
    headline: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1),
    ) {
        Box(Modifier.weight(1f, fill = false)) {
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                headline()
            }
        }
        if (supporting != null) {
            Text(
                text = DSListRowDefaults.inlineSeparator,
                style = MaterialTheme.typography.bodyMedium,
                color = DSListRowDefaults.supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Box(Modifier.weight(1f, fill = false)) {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = DSListRowDefaults.supportingColor,
                    ),
                ) {
                    supporting()
                }
            }
        }
    }
}
