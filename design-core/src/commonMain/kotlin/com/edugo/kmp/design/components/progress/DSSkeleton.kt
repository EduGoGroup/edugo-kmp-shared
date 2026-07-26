package com.edugo.kmp.design.components.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.AnimationDuration
import com.edugo.kmp.design.tokens.ComponentShapes
import com.edugo.kmp.design.tokens.CornerRadius
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Barra de skeleton: la primitiva base del estado **loading** del DS (spec §7, D-051.3).
 *
 * El patrón es "misma geometría que el contenido final": nunca un spinner a pantalla completa
 * (salvo primera carga de app), sino bloques con el alto/radio reales de lo que va a llegar.
 * Para las piezas ya especificadas (tarjeta de lista, círculo de avatar, tile de KPI) usa las
 * primitivas de `DSSkeletonPrimitives.kt`, que se construyen sobre esta.
 *
 * El shimmer es un barrido lineal de [AnimationDuration.extraLong3] (1.2s) que se lee en fase de
 * dibujo, no de composición: la animación no recompone nada.
 */
@Composable
fun DSSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = DSSkeletonDefaults.genericBarHeight,
    cornerRadius: Dp = CornerRadius.extraSmall,
    widthFraction: Float = 1f,
    color: Color = Color.Unspecified,
) {
    DSSkeletonSurface(
        modifier =
            modifier
                .fillMaxWidth(widthFraction)
                .height(height),
        shape = RoundedCornerShape(cornerRadius),
        color = if (color.isSpecified) color else DSSkeletonDefaults.barColor,
    )
}

/**
 * Superficie con shimmer compartida por todas las primitivas de skeleton.
 *
 * `internal` a propósito: el vocabulario público son [DSSkeleton] y las primitivas con
 * geometría de spec, no una caja libre.
 */
@Composable
internal fun DSSkeletonSurface(
    modifier: Modifier,
    shape: Shape,
    color: Color = DSSkeletonDefaults.barColor,
) {
    val transition = rememberInfiniteTransition(label = "dsSkeleton")
    val progress =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = DSSkeletonDefaults.shimmerDurationMillis,
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "dsSkeletonShimmer",
        )
    val highlight = DSSkeletonDefaults.highlightColor

    Box(
        modifier =
            modifier
                .clip(shape)
                .drawBehind {
                    drawRect(color)
                    val sweep = size.width
                    val start = (progress.value * 2f - 1f) * sweep
                    drawRect(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(color, highlight, color),
                                start = Offset(start, 0f),
                                end = Offset(start + sweep, 0f),
                            ),
                    )
                },
    )
}

/**
 * Umbral anti-parpadeo del skeleton (spec §7, D-051.3): el loading **solo se pinta si la carga
 * supera 300ms**. Devuelve `true` cuando [visible] sigue activo pasados [delayMillis].
 *
 * El umbral vive aquí (no en cada consumidor) para que ninguna pantalla lo reinvente:
 *
 * ```
 * if (rememberDelayedVisible(uiState.isLoading)) {
 *     DSSkeletonList(count = 5)
 * }
 * ```
 */
@Composable
fun rememberDelayedVisible(
    visible: Boolean,
    delayMillis: Long = DSSkeletonDefaults.appearanceDelayMillis,
): Boolean {
    var elapsed by remember { mutableStateOf(false) }
    LaunchedEffect(visible, delayMillis) {
        if (visible) {
            delay(delayMillis)
            elapsed = true
        } else {
            elapsed = false
        }
    }
    return visible && elapsed
}

/**
 * Geometría y tiempos canónicos del skeleton (spec §7, D-051.3). Cualquier skeleton del
 * ecosistema (`ListSkeleton`, `FormSkeleton`, `DashboardSkeleton` de kmp-screens) lee de aquí:
 * cero medidas mágicas repartidas por los renderers.
 */
object DSSkeletonDefaults {
    /** Alto de la barra genérica de [DSSkeleton] (compatibilidad con el skeleton pre-051). */
    val genericBarHeight: Dp = Spacing.spacing5

    /** Bloque de tarjeta de lista en loading: mismo alto que la fila cómoda real (72dp). */
    val cardHeight: Dp = Sizes.listRowMinHeight

    /** Radio del bloque de tarjeta: el mismo `ComponentShapes.card` (20dp) de `DSListRow`. */
    val cardCornerRadius: Dp = ComponentShapes.card

    /** Círculo del leading: `Sizes.Avatar.large` (40dp), el avatar real de la fila. */
    val circleSize: Dp = Sizes.Avatar.large

    /** Tile de KPI del dashboard: 96dp (`Sizes.kpiTileHeight`). */
    val kpiTileHeight: Dp = Sizes.kpiTileHeight

    // excepción deuda-051: 14dp / 12dp son altos de barra de texto fijados por spec §7; no hay
    // token de tipografía en Dp y redondear al vecino de `Spacing` (16/12) rompería la relación
    // visual 14>12 entre título y subtítulo.
    val primaryBarHeight: Dp = 14.dp
    val secondaryBarHeight: Dp = 12.dp

    // excepción deuda-051: radio 6dp de las barras de texto (spec §7); cae entre
    // `CornerRadius.extraSmall` (4) y `CornerRadius.small` (8), sin token MD3 equivalente.
    val barCornerRadius: Dp = 6.dp

    /** Ancho relativo de la barra de título (60% del bloque de texto). */
    const val primaryBarFraction: Float = 0.6f

    /** Ancho relativo de la barra de subtítulo (40% del bloque de texto). */
    const val secondaryBarFraction: Float = 0.4f

    /** Máximo de repeticiones de un skeleton de lista (spec §7): más pierde sentido informativo. */
    const val maxRepetitions: Int = 6

    /** Ciclo del shimmer: 1.2s (spec §7) — `AnimationDuration.extraLong3`. */
    val shimmerDurationMillis: Int = AnimationDuration.extraLong3.toInt()

    /** Umbral de aparición: 300ms (spec §7) — `AnimationDuration.medium2`. */
    val appearanceDelayMillis: Long = AnimationDuration.medium2

    /** Fondo del bloque de tarjeta/tile: `surfaceContainer`, como la tarjeta real (spec §5.1). */
    val blockColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    /** Relleno de círculo y barras de texto: `surfaceContainerHigh` (spec §7). */
    val barColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    /** Cresta del barrido del shimmer. */
    val highlightColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
}

// --- Previews ---

@Preview(name = "DSSkeleton - Light", showBackground = true)
@Composable
fun DSSkeletonPreviewLight() {
    DSTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Spacing.spacing4),
                verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                DSSkeleton()
                DSSkeleton(
                    height = DSSkeletonDefaults.primaryBarHeight,
                    cornerRadius = DSSkeletonDefaults.barCornerRadius,
                    widthFraction = DSSkeletonDefaults.primaryBarFraction,
                )
                DSSkeleton(
                    height = DSSkeletonDefaults.secondaryBarHeight,
                    cornerRadius = DSSkeletonDefaults.barCornerRadius,
                    widthFraction = DSSkeletonDefaults.secondaryBarFraction,
                )
            }
        }
    }
}

@Preview(name = "DSSkeleton - Dark", showBackground = true)
@Composable
private fun DSSkeletonPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSkeleton(modifier = Modifier.padding(Spacing.spacing4))
        }
    }
}
