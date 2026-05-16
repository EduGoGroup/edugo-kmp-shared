package com.edugo.kmp.design.tokens

/**
 * Tabla declarativa `style -> RenderToken` para los botones que el SDUI
 * declara en `slot.style`. El backend declara el estilo semantico
 * (`filled`, `outlined`, `text`, `icon`, `destructive`); el cliente
 * decide como materializarlo via [ButtonStyleCatalog.lookup].
 *
 * Esta tabla vive en `design-core` (lo paga el design system, no el
 * SDUI engine). Agregar un style nuevo se hace aqui, no en el renderer.
 */
enum class ButtonVariant {
    ICON,
    FILLED,
    OUTLINED,
    TEXT,
    DESTRUCTIVE_OUTLINED,
}

/**
 * Rol semantico de color que el renderer resolvera contra el ColorScheme
 * activo en tiempo de render (los Color de Material3 son `@Composable`,
 * por eso no los podemos materializar en estos data classes).
 *
 * Mirror reducido de [ColorRole]; ambos enums conviven (ActionStyleTokens
 * sigue funcionando para styles del SDUI viejo y los styles primary /
 * secondary / tonal / success), mientras [ButtonStyleCatalog] resuelve
 * los styles SDUI nuevos del backend (`filled` / `outlined` / `text` /
 * `icon` / `destructive`).
 */
enum class ColorRoleHint {
    PRIMARY,
    ON_PRIMARY,
    ERROR,
    ON_ERROR,
    OUTLINE,
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
 * decision visual para los styles `filled` / `outlined` / `text` /
 * `icon` / `destructive` que el backend declara desde la Fase 3a.
 *
 * Styles desconocidos caen al fallback "text" (boton neutro, sin
 * container, sin tinte custom). Esto evita crashes si el seed introduce
 * un style nuevo no soportado por el cliente; queda visible como
 * TextButton hasta que se registre.
 */
object ButtonStyleCatalog {
    private val table: Map<String, RenderToken> = mapOf(
        "filled" to RenderToken(ButtonVariant.FILLED),
        "outlined" to RenderToken(ButtonVariant.OUTLINED),
        "text" to RenderToken(ButtonVariant.TEXT),
        "icon" to RenderToken(ButtonVariant.ICON),
        "destructive" to RenderToken(
            variant = ButtonVariant.DESTRUCTIVE_OUTLINED,
            tint = ColorRoleHint.ERROR,
        ),
    )

    /**
     * Resuelve un style SDUI a su [RenderToken]. Case-insensitive.
     * Si [style] es null o no esta registrado, retorna el token "text".
     */
    fun lookup(style: String?): RenderToken =
        table[style?.lowercase()] ?: table.getValue("text")

    /** Lista de styles soportados; util para tests de contrato. */
    val supportedStyles: Set<String> by lazy { table.keys }
}
