package com.edugo.kmp.settings.model

/**
 * Opciones de tema disponibles en la aplicación.
 */
enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        val DEFAULT: ThemeOption = SYSTEM

        fun fromString(value: String): ThemeOption {
            return entries.firstOrNull { it.name == value } ?: DEFAULT
        }
    }
}
