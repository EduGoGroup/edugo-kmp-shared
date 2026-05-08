/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.settings.reset

import com.edugo.kmp.storage.SafeEduGoStorage
import com.edugo.kmp.storage.reset.ClientStateStore

/**
 * [ClientStateStore] que limpia las 3 keys "operativas" de la app: theme, sidebar, y
 * navigation state. Las 3 viven en el mismo backing
 * (`Preferences/SharedPrefs/UserDefaults/localStorage`) que los auth tokens, pero se
 * reportan como un store lógico distinto para granularidad en Loki.
 *
 * Las keys son contractuales y estables:
 * - `app.theme.preference` — escrita por `ThemeServiceImpl` (este módulo).
 * - `app.sidebar.collapsed` — escrita por `SidebarServiceImpl` (este módulo).
 * - `nav_state` — escrita por consumidores ui (Compose `App.kt`) para persistir la
 *   navigation state. Vive aquí por agrupación lógica (es una "app setting"
 *   operativa), aunque su writer no pertenezca al shared `:settings`.
 *
 * Si las keys cambian en sus módulos owners, este store debe actualizarse en tándem.
 * Coupling justificado: el reset es por construcción dev/QA, no se publica como API.
 */
public class AppSettingsStore(
    private val storage: SafeEduGoStorage,
) : ClientStateStore {
    override val id: String = "app.settings"

    override suspend fun clear() {
        storage.removeSafe(KEY_THEME)
        storage.removeSafe(KEY_SIDEBAR)
        storage.removeSafe(KEY_NAV_STATE)
    }

    private companion object {
        const val KEY_THEME = "app.theme.preference"
        const val KEY_SIDEBAR = "app.sidebar.collapsed"
        const val KEY_NAV_STATE = "nav_state"
    }
}
