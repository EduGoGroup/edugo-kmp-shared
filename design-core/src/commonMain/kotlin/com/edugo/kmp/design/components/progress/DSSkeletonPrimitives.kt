package com.edugo.kmp.design.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Círculo de skeleton para el slot leading (avatar/icono): 40dp (spec §7).
 */
@Composable
fun DSSkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = DSSkeletonDefaults.circleSize,
) {
    DSSkeletonSurface(
        modifier = modifier.size(size),
        shape = CircleShape,
    )
}

/**
 * Tarjeta de lista en loading (spec §7, D-051.3): bloque de 72dp radio 20 sobre
 * `surfaceContainer`, con círculo de 40dp + barra de título 14dp×60% y barra de subtítulo
 * 12dp×40% (radio 6, `surfaceContainerHigh`).
 *
 * Es la MISMA geometría de `DSListRow` en densidad cómoda — incluido el borde 1dp
 * `outlineVariant` — para que al llegar el dato la lista no "salte".
 *
 * @param leading si `false` se omite el círculo (listas sin avatar/icono).
 */
@Composable
fun DSSkeletonCard(
    modifier: Modifier = Modifier,
    leading: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(DSSkeletonDefaults.cardHeight)
                .background(
                    color = DSSkeletonDefaults.blockColor,
                    shape = RoundedCornerShape(DSSkeletonDefaults.cardCornerRadius),
                ).border(
                    width = Sizes.borderThin,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(DSSkeletonDefaults.cardCornerRadius),
                )
                .padding(Spacing.spacing4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
    ) {
        if (leading) {
            DSSkeletonCircle()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
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

/**
 * Lista en loading: [count] repeticiones de [DSSkeletonCard] con el mismo gap que la lista real.
 *
 * [count] se recorta a `DSSkeletonDefaults.maxRepetitions` (6, spec §7): más tarjetas fantasma no
 * aportan información y encarecen la animación.
 */
@Composable
fun DSSkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
    leading: Boolean = true,
    itemSpacing: Dp = Spacing.spacing2,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        repeat(count.coerceIn(0, DSSkeletonDefaults.maxRepetitions)) {
            DSSkeletonCard(leading = leading)
        }
    }
}

/**
 * Tile de KPI en loading (spec §7, D-051.2/D-051.3): bloque de 96dp con barra corta (label) y
 * barra ancha (cifra). Misma caja que `MetricCard` para que el dashboard no reflote al cargar.
 */
@Composable
fun DSSkeletonKpiTile(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(DSSkeletonDefaults.kpiTileHeight)
                .background(
                    color = DSSkeletonDefaults.blockColor,
                    shape = RoundedCornerShape(DSSkeletonDefaults.cardCornerRadius),
                ).border(
                    width = Sizes.borderThin,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(DSSkeletonDefaults.cardCornerRadius),
                )
                .padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2, Alignment.CenterVertically),
    ) {
        DSSkeleton(
            height = DSSkeletonDefaults.secondaryBarHeight,
            cornerRadius = DSSkeletonDefaults.barCornerRadius,
            widthFraction = DSSkeletonDefaults.secondaryBarFraction,
        )
        DSSkeleton(
            height = DSSkeletonDefaults.primaryBarHeight,
            cornerRadius = DSSkeletonDefaults.barCornerRadius,
            widthFraction = DSSkeletonDefaults.primaryBarFraction,
        )
    }
}

// --- Previews ---

@Preview(name = "DSSkeletonCard - Light", showBackground = true)
@Composable
private fun DSSkeletonCardPreview() {
    DSTheme {
        Surface {
            DSSkeletonCard(modifier = Modifier.padding(Spacing.spacing4))
        }
    }
}

@Preview(name = "DSSkeletonList - 3 items", showBackground = true)
@Composable
private fun DSSkeletonListPreview() {
    DSTheme {
        Surface {
            DSSkeletonList(modifier = Modifier.padding(Spacing.spacing4), count = 3)
        }
    }
}

@Preview(name = "DSSkeletonKpiTile - Dark", showBackground = true)
@Composable
private fun DSSkeletonKpiTilePreview() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSkeletonKpiTile(modifier = Modifier.padding(Spacing.spacing4))
        }
    }
}
