package com.edugo.kmp.settings.sidebar

import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación de [SidebarService] con persistencia via [SafeEduGoStorage].
 *
 * Lee el estado guardado al inicializar y lo persiste en cada cambio.
 */
class SidebarServiceImpl(
    private val storage: SafeEduGoStorage
) : SidebarService {

    private val _isCollapsed = MutableStateFlow(loadPersistedState())

    override val isCollapsed: StateFlow<Boolean> = _isCollapsed.asStateFlow()

    override fun toggleCollapsed() {
        setCollapsed(!_isCollapsed.value)
    }

    override fun setCollapsed(collapsed: Boolean) {
        storage.putStringSafe(SIDEBAR_KEY, collapsed.toString())
        _isCollapsed.value = collapsed
    }

    private fun loadPersistedState(): Boolean {
        val defaultValue = false
        val stored = storage.getStringSafe(SIDEBAR_KEY, defaultValue.toString())
        return when (stored) {
            "true" -> true
            "false" -> false
            else -> {
                storage.putStringSafe(SIDEBAR_KEY, defaultValue.toString())
                defaultValue
            }
        }
    }

    private companion object {
        const val SIDEBAR_KEY = "app.sidebar.collapsed"
    }
}
