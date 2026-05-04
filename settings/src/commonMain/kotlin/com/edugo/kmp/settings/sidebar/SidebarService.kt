package com.edugo.kmp.settings.sidebar

import kotlinx.coroutines.flow.StateFlow

/**
 * Servicio para gestionar el estado collapsed/expanded del sidebar.
 *
 * Provee un [StateFlow] reactivo con el estado actual
 * y métodos para alternarlo con persistencia.
 */
interface SidebarService {
    /**
     * Flow reactivo con el estado collapsed del sidebar.
     */
    val isCollapsed: StateFlow<Boolean>

    /**
     * Alterna entre collapsed y expanded, persistiendo el cambio.
     */
    fun toggleCollapsed()

    /**
     * Establece un valor específico y lo persiste.
     */
    fun setCollapsed(collapsed: Boolean)
}
