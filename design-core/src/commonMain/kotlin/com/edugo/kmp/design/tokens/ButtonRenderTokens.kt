package com.edugo.kmp.design.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tabla declarativa `style -> RenderToken` para los botones que el SDUI
 * declara en `slot.style`. El backend declara el estilo semantico
 * (`filled`, `outlined`, `text`, `icon`, `tonal`, `destructive`); el cliente
 * decide como materializarlo via [ButtonStyleCatalog.lookup].
 *
 * Esta tabla vive en `design-core` (lo paga el design system, no el
 * SDUI engine). Agregar un style nuevo se hace aqui, no en el renderer.
 */
enum class ButtonVariant {
    ICON,
    FILLED,
    OUTLINED,
    TONAL,
    TEXT,
    DESTRUCTIVE_OUTLINED,
}

/**
 * Rol semantico de color que el renderer resolvera contra el ColorScheme
 * activo en tiempo de render (los Color de Material3 son `@Composable`,
 * por eso no los podemos materializar en estos data classes).
 *
 * Incluye los roles M3 estandar usados por el renderer y los semanticos
 * extendidos (success) materializados via [LocalExtendedColorScheme].
 */
enum class ColorRoleHint {
    PRIMARY,
    ON_PRIMARY,
    ERROR,
    ON_ERROR,
    OUTLINE,
    SUCCESS,
    ON_SUCCESS,
    ON_SURFACE_VARIANT,
}

enum class ShapeRole {
    SMALL,
    MEDIUM,
    LARGE,
    FULL,
}

/**
 * Token de render para un boton SDUI.
 *
 * - [variant]: que componente DS materializar.
 * - [tint]: rol de color para el content (icono/texto). `null` deja el
 *   default de Material3 para esa variante.
 * - [shape]: rol de shape. `MEDIUM` por defecto.
 */
data class RenderToken(
    val variant: ButtonVariant,
    val tint: ColorRoleHint? = null,
    val shape: ShapeRole = ShapeRole.MEDIUM,
)

/**
 * Tabla declarativa de styles SDUI -> RenderToken. La unica fuente de
 * decision visual para los styles que el backend declara desde la Fase 3a.
 *
 * Vocabulario nuevo: `filled` / `outlined` / `text` / `icon` / `tonal` /
 * `destructive`. Backward-compat con el vocabulario legacy del seed:
 * `primary` / `secondary` / `success` / `icon-only` (tonal y destructive
 * coinciden en ambos vocabularios).
 *
 * Styles desconocidos caen al fallback "text" (boton neutro, sin
 * container, sin tinte custom). Esto evita crashes si el seed introduce
 * un style nuevo no soportado por el cliente; queda visible como
 * TextButton hasta que se registre.
 */
object ButtonStyleCatalog {
    private val table: Map<String, RenderToken> =
        mapOf(
            // Vocabulario nuevo (Fase 3a+)
            "filled" to RenderToken(ButtonVariant.FILLED),
            "outlined" to RenderToken(ButtonVariant.OUTLINED),
            "text" to RenderToken(ButtonVariant.TEXT),
            "icon" to RenderToken(ButtonVariant.ICON),
            "tonal" to RenderToken(ButtonVariant.TONAL),
            // `destructive` rinde siempre como outlined rojo, independiente del controlType del slot.
            // Es cambio deliberado respecto al esquema legacy (que daba FILLED rojo solido para
            // controlType=FILLED_BUTTON): alineado con la guia M3 moderna sobre acciones destructivas.
            "destructive" to
                RenderToken(
                    variant = ButtonVariant.DESTRUCTIVE_OUTLINED,
                    tint = ColorRoleHint.ERROR,
                ),
            // Backward-compat: vocabulario legacy del seed
            "primary" to RenderToken(ButtonVariant.FILLED),
            "secondary" to RenderToken(ButtonVariant.OUTLINED),
            "success" to RenderToken(ButtonVariant.FILLED, tint = ColorRoleHint.ON_SUCCESS),
            "icon-only" to RenderToken(ButtonVariant.ICON, tint = ColorRoleHint.ON_SURFACE_VARIANT),
        )

    /**
     * Resuelve un style SDUI a su [RenderToken]. Case-insensitive.
     * Si [style] es null o no esta registrado, retorna el token "text".
     */
    fun lookup(style: String?): RenderToken = table[style?.lowercase()] ?: table.getValue("text")

    /** Lista de styles soportados; util para tests de contrato. */
    val supportedStyles: Set<String> by lazy { table.keys }
}

/**
 * Resuelve el rol de color a un [Color] real del ColorScheme. `null` ->
 * [Color.Unspecified] (cae al default de Material3 para esa variante).
 *
 * Vive junto a los tokens para que cualquier consumidor del design-core
 * que tenga un [ColorRoleHint] pueda materializarlo sin replicar el
 * `when`. SDUI engine y SlotRenderer en `kmp-screens` la consumen igual.
 */
@Composable
fun ColorRoleHint?.resolve(): Color =
    when (this) {
        null -> Color.Unspecified
        ColorRoleHint.PRIMARY -> MaterialTheme.colorScheme.primary
        ColorRoleHint.ON_PRIMARY -> MaterialTheme.colorScheme.onPrimary
        ColorRoleHint.ERROR -> MaterialTheme.colorScheme.error
        ColorRoleHint.ON_ERROR -> MaterialTheme.colorScheme.onError
        ColorRoleHint.OUTLINE -> MaterialTheme.colorScheme.outline
        ColorRoleHint.SUCCESS -> LocalExtendedColorScheme.current.success
        ColorRoleHint.ON_SUCCESS -> LocalExtendedColorScheme.current.onSuccess
        ColorRoleHint.ON_SURFACE_VARIANT -> MaterialTheme.colorScheme.onSurfaceVariant
    }
