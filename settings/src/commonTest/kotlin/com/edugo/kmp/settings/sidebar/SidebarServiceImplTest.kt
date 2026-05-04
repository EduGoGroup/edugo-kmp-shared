package com.edugo.kmp.settings.sidebar

import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SidebarServiceImplTest {

    private fun createService(settings: MapSettings = MapSettings()): SidebarServiceImpl {
        val storage = SafeEduGoStorage.wrap(EduGoStorage.withSettings(settings))
        return SidebarServiceImpl(storage)
    }

    @Test
    fun defaultCollapsedStateIsFalse() {
        val service = createService()

        assertFalse(service.isCollapsed.value)
    }

    @Test
    fun setCollapsedUpdatesStateFlow() {
        val service = createService()

        service.setCollapsed(true)
        assertTrue(service.isCollapsed.value)

        service.setCollapsed(false)
        assertFalse(service.isCollapsed.value)
    }

    @Test
    fun toggleCollapsedFlipsCurrentState() {
        val service = createService()

        service.toggleCollapsed()
        assertTrue(service.isCollapsed.value)

        service.toggleCollapsed()
        assertFalse(service.isCollapsed.value)
    }

    @Test
    fun collapsedStateIsPersistedAcrossInstances() {
        val settings = MapSettings()

        val service1 = createService(settings)
        service1.setCollapsed(true)

        val service2 = createService(settings)
        assertTrue(service2.isCollapsed.value)
    }

    @Test
    fun invalidStoredValueFallsBackToDefaultAndResetsStorage() {
        val settings = MapSettings()
        settings.putString("app.sidebar.collapsed", "invalid")

        val service = createService(settings)

        assertFalse(service.isCollapsed.value)
        assertEquals("false", settings.getString("app.sidebar.collapsed", "missing"))
    }
}
