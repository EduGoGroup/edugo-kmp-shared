# design-core

Módulo central del **design system neutro** de la familia `edugo-kmp-shared` (Compose Multiplatform). Contiene tokens, tipografía baseline, theme parametrizable y la familia `DS*` de componentes wrapper sobre Material 3 — todos sin marca EduGo.

## Propósito

`design-core` es la base reutilizable del design system: cualquier app KMP (de EduGo o de terceros) puede consumirlo y aplicar su propia paleta sobre el `DSTheme` sin tener que duplicar componentes. Los símbolos branded (paleta concreta EduGo, logo `DSBrand`, theme `EduGoTheme`) viven en la capa producto `:kmp-design` del repo principal `edugo-ui-kmp`, no aquí.

## Contenido

- **Tokens neutros:** `Spacing`, `Sizes`, `Alpha`, `Durations`, `Radius`, `Elevation`, `MessageType`, `SemanticColors`.
- **Tokens en `tokens/`:** `ColorTokens` (paleta MD3 baseline), `ExtendedColorScheme`, `MotionTokens`, `OpacityTokens`, `ResponsiveTokens`, `ShapeTokens`, `SpacingTokens`, `ZIndexTokens`.
- **Tipografía baseline:** `BaseTypography` (Material 3 baseline con `FontFamily.Default`).
- **Theme parametrizable:** `DSTheme(colorScheme, typography, extendedColors, content)`.
- **Componentes `DS*`:** botones (8), cards (3), dialogs (4 incl. `DSAlertDialog` raíz), inputs (4), lists (3), media (3), navigation (5), overlays (5), progress (3), selection (5), feedback (1).
- **Extensiones de plataforma:** `platform/PlatformThemeExtensions` (expect/actual para Android, Desktop, iOS, wasmJs).

## Política de tematización

`DSTheme` es el punto de extensión: una app proporciona su propio `ColorScheme` (claro/oscuro) y opcionalmente una `Typography` y un `ExtendedColorScheme`. Los componentes `DS*` no consultan colores hardcoded — todo viene del `MaterialTheme` que `DSTheme` provee. La regla es: **si quieres cambiar la marca, cambia el `colorScheme` que pasas a `DSTheme`**, no toques los componentes.

## Instalación

`design-core` se publica como parte del BOM `edugo-kmp-bom` (publicación efectiva en Fase 7). Para consumirlo:

```kotlin
// build.gradle.kts
dependencies {
    implementation(project.dependencies.platform(libs.edugo.kmp.bom))
    api(libs.edugo.kmp.design.core)
}
```

## Ejemplo mínimo

```kotlin
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.components.buttons.DSFilledButton
import androidx.compose.material3.lightColorScheme

@Composable
fun App() {
    DSTheme(colorScheme = lightColorScheme()) {
        DSFilledButton(text = "Aceptar", onClick = { /* ... */ })
    }
}
```

## Referencia

El design system central (especificaciones de tokens, jerarquía de componentes, guías de uso) vive en `Documentation/GuideDesign/Design/KMP/`. Cualquier divergencia entre este código y la spec se documenta en `docs/architecture/kmp-shared-extraction-v2/phase-6-ui-design-extraction/divergencias.md`.
