package com.edugo.kmp.design.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tipos de control soportados por el design system al elegir la variante visual
 * de una acción SDUI. Es un espejo local de los ControlType "tipo botón" del
 * motor SDUI; design-core no depende de sdui-engine, así que el mapeo lo hace
 * el consumidor (renderer).
 */
enum class DSControlType {
    FILLED_BUTTON,
    OUTLINED_BUTTON,
    TEXT_BUTTON,
    ICON_BUTTON,
}

/**
 * Variantes de componente del design system que el resolver puede pedir al
 * renderer. Cada variante implica un componente DS distinto (Filled, Outlined,
 * Text, Icon, Tonal).
 */
enum class DSVariant {
    FILLED,
    OUTLINED,
    TEXT,
    ICON,
    TONAL,
}

/**
 * Rol semántico de color en el ColorScheme. El resolver entrega el rol; el
 * componente lo materializa en Color vía [resolve]. Incluye tertiary y los
 * extendidos (success) por compatibilidad con styles del seed.
 */
enum class ColorRole {
    PRIMARY,
    ON_PRIMARY,
    SECONDARY,
    ON_SECONDARY,
    SECONDARY_CONTAINER,
    ON_SECONDARY_CONTAINER,
    ERROR,
    ON_ERROR,
    SURFACE,
    ON_SURFACE,
    SURFACE_VARIANT,
    ON_SURFACE_VARIANT,
    TERTIARY,
    ON_TERTIARY,
    SUCCESS,
    ON_SUCCESS,
}

/**
 * Configuración de render que el SDUI consume para pintar una acción. La
 * variante decide qué componente DS usar; tint y container se resuelven luego
 * con [ColorRole.resolve].
 */
data class ActionRenderConfig(
    val variant: DSVariant,
    val tintRole: ColorRole,
    val containerRole: ColorRole?,
)

/**
 * Devuelve la configuración de render aprobada para el par (style, controlType).
 * Cuando [style] es null o desconocido se aplica el fallback de "secondary" con
 * el [controlType] dado. Tabla declarativa: agregar un style nuevo se hace acá,
 * no en el renderer (principio "una fuente, un punto").
 */
fun actionRenderConfigFor(
    style: String?,
    controlType: DSControlType,
): ActionRenderConfig {
    // Fallback: style nulo o desconocido se trata como "secondary".
    val effectiveStyle = style?.lowercase()?.takeIf { it in KNOWN_STYLES } ?: SECONDARY
    return when (effectiveStyle) {
        PRIMARY -> primaryConfig(controlType)
        SECONDARY -> secondaryConfig(controlType)
        TONAL -> tonalConfig(controlType)
        DESTRUCTIVE -> destructiveConfig(controlType)
        SUCCESS -> successConfig(controlType)
        ICON_ONLY -> iconOnlyConfig(controlType)
        else -> secondaryConfig(controlType)
    }
}

/**
 * Resuelve el rol de color contra el ColorScheme activo. Lee MaterialTheme para
 * los roles estándar y el ExtendedColorScheme local para los semánticos extra
 * (success). Mantener este when sincronizado con el enum [ColorRole].
 */
@Composable
fun ColorRole.resolve(): Color =
    when (this) {
        ColorRole.PRIMARY -> MaterialTheme.colorScheme.primary
        ColorRole.ON_PRIMARY -> MaterialTheme.colorScheme.onPrimary
        ColorRole.SECONDARY -> MaterialTheme.colorScheme.secondary
        ColorRole.ON_SECONDARY -> MaterialTheme.colorScheme.onSecondary
        ColorRole.SECONDARY_CONTAINER -> MaterialTheme.colorScheme.secondaryContainer
        ColorRole.ON_SECONDARY_CONTAINER -> MaterialTheme.colorScheme.onSecondaryContainer
        ColorRole.ERROR -> MaterialTheme.colorScheme.error
        ColorRole.ON_ERROR -> MaterialTheme.colorScheme.onError
        ColorRole.SURFACE -> MaterialTheme.colorScheme.surface
        ColorRole.ON_SURFACE -> MaterialTheme.colorScheme.onSurface
        ColorRole.SURFACE_VARIANT -> MaterialTheme.colorScheme.surfaceVariant
        ColorRole.ON_SURFACE_VARIANT -> MaterialTheme.colorScheme.onSurfaceVariant
        ColorRole.TERTIARY -> MaterialTheme.colorScheme.tertiary
        ColorRole.ON_TERTIARY -> MaterialTheme.colorScheme.onTertiary
        ColorRole.SUCCESS -> LocalExtendedColorScheme.current.success
        ColorRole.ON_SUCCESS -> LocalExtendedColorScheme.current.onSuccess
    }

private const val PRIMARY = "primary"
private const val SECONDARY = "secondary"
private const val TONAL = "tonal"
private const val DESTRUCTIVE = "destructive"
private const val SUCCESS = "success"
private const val ICON_ONLY = "icon-only"

private val KNOWN_STYLES = setOf(PRIMARY, SECONDARY, TONAL, DESTRUCTIVE, SUCCESS, ICON_ONLY)

private fun primaryConfig(controlType: DSControlType): ActionRenderConfig =
    when (controlType) {
        DSControlType.ICON_BUTTON ->
            ActionRenderConfig(DSVariant.ICON, ColorRole.PRIMARY, containerRole = null)
        // FILLED_BUTTON y default: botón sólido primario.
        else ->
            ActionRenderConfig(DSVariant.FILLED, ColorRole.ON_PRIMARY, ColorRole.PRIMARY)
    }

private fun secondaryConfig(controlType: DSControlType): ActionRenderConfig =
    when (controlType) {
        DSControlType.ICON_BUTTON ->
            ActionRenderConfig(DSVariant.ICON, ColorRole.ON_SURFACE_VARIANT, containerRole = null)
        // OUTLINED_BUTTON y default: contorno con tinte primario.
        else ->
            ActionRenderConfig(DSVariant.OUTLINED, ColorRole.PRIMARY, containerRole = null)
    }

private fun tonalConfig(controlType: DSControlType): ActionRenderConfig =
    when (controlType) {
        DSControlType.ICON_BUTTON ->
            ActionRenderConfig(DSVariant.ICON, ColorRole.ON_SECONDARY_CONTAINER, containerRole = null)
        // FILLED_BUTTON y default: variante TONAL con container suave.
        else ->
            ActionRenderConfig(DSVariant.TONAL, ColorRole.ON_SECONDARY_CONTAINER, ColorRole.SECONDARY_CONTAINER)
    }

private fun destructiveConfig(controlType: DSControlType): ActionRenderConfig =
    when (controlType) {
        DSControlType.ICON_BUTTON ->
            ActionRenderConfig(DSVariant.ICON, ColorRole.ERROR, containerRole = null)
        DSControlType.FILLED_BUTTON ->
            ActionRenderConfig(DSVariant.FILLED, ColorRole.ON_ERROR, ColorRole.ERROR)
        // OUTLINED_BUTTON y default: contorno con tinte error.
        else ->
            ActionRenderConfig(DSVariant.OUTLINED, ColorRole.ERROR, containerRole = null)
    }

private fun successConfig(controlType: DSControlType): ActionRenderConfig =
    when (controlType) {
        DSControlType.ICON_BUTTON ->
            ActionRenderConfig(DSVariant.ICON, ColorRole.SUCCESS, containerRole = null)
        // FILLED_BUTTON y default: sólido sobre el verde semántico del extended scheme.
        else ->
            ActionRenderConfig(DSVariant.FILLED, ColorRole.ON_SUCCESS, ColorRole.SUCCESS)
    }

private fun iconOnlyConfig(controlType: DSControlType): ActionRenderConfig =
    // icon-only solo tiene sentido como ICON; cualquier controlType se renderiza como ICON neutro.
    ActionRenderConfig(DSVariant.ICON, ColorRole.ON_SURFACE_VARIANT, containerRole = null)
