package com.edugo.kmp.design.platform

/**
 * Represents the platform type for adapting the UI.
 */
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP,
    WEB,
}

/**
 * Window size class for adaptive layouts, based on Material Design guidelines.
 */
enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

/**
 * Provides the current platform type at runtime.
 *
 * Vive en `design-core` (no en `sdui-engine`) porque componentes DS agnósticos
 * al motor SDUI también necesitan adaptarse por plataforma (ej.
 * `DSAdaptiveActionsHost`). `sdui-engine` ya depende de `design-core`; subirlo
 * aquí rompe el ciclo que aparecería si `design-core` tuviera que importar
 * desde `sdui-engine`.
 *
 * Cada entrypoint de plataforma (Android Application.onCreate, iOS app init,
 * Desktop main, Web main) debe setear `PlatformDetector.current` antes de
 * pintar la primera pantalla.
 */
object PlatformDetector {
    var current: PlatformType = PlatformType.ANDROID
}
