package com.edugo.kmp.settings.theme

import com.edugo.kmp.settings.model.ThemeOption
import kotlinx.coroutines.flow.StateFlow

/**
 * Servicio para gestionar la preferencia de tema del usuario.
 *
 * Provee un [StateFlow] reactivo con la preferencia actual
 * y métodos para consultarla y modificarla con persistencia.
 */
interface ThemeService {
    /**
     * Flow reactivo con la preferencia de tema actual.
     */
    val themePreference: StateFlow<ThemeOption>

    /**
     * Establece la preferencia de tema y la persiste en storage.
     */
    fun setThemePreference(option: ThemeOption)

    /**
     * Retorna la preferencia de tema actual.
     */
    fun getCurrentTheme(): ThemeOption
}
