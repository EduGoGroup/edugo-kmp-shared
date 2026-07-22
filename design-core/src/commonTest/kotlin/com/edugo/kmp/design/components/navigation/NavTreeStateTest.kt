package com.edugo.kmp.design.components.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests de la derivación de estado pura del árbol (plan 049 F2, tarea 2.5/2.6).
 * Cubre casos límite: profundidad 3+, sección con 10 hijos, ruta activa y toggle.
 */
class NavTreeStateTest {

    // Administración › Usuarios › (Listado, Editar, Roles) + Auditoría; Panel.
    private val deep = listOf(
        NavTreeNode(
            key = "admin", label = "Administración",
            children = listOf(
                NavTreeNode(
                    key = "users", label = "Usuarios",
                    children = listOf(
                        NavTreeNode(key = "users-list", label = "Listado"),
                        NavTreeNode(key = "users-edit", label = "Editar"),
                        NavTreeNode(key = "users-roles", label = "Roles"),
                    ),
                ),
                NavTreeNode(key = "audit", label = "Auditoría"),
            ),
        ),
        NavTreeNode(key = "dashboard", label = "Panel"),
    )

    private val tenChildren = listOf(
        NavTreeNode(
            key = "reports", label = "Reportes",
            children = (1..10).map { NavTreeNode(key = "report-$it", label = "Reporte $it") },
        ),
    )

    @Test
    fun toggleExpandedAddsWhenAbsent() {
        assertEquals(setOf("a"), toggleExpanded(emptySet(), "a"))
    }

    @Test
    fun toggleExpandedRemovesWhenPresent() {
        assertEquals(setOf("b"), toggleExpanded(setOf("a", "b"), "a"))
    }

    @Test
    fun activePathReturnsFullChainDepth3() {
        assertEquals(listOf("admin", "users", "users-edit"), deep.activePath("users-edit"))
    }

    @Test
    fun activePathEmptyForUnknownOrNull() {
        assertTrue(deep.activePath("nope").isEmpty())
        assertTrue(deep.activePath(null).isEmpty())
    }

    @Test
    fun ancestorKeysExcludeActiveItself() {
        assertEquals(setOf("admin", "users"), deep.ancestorKeysOf("users-edit"))
    }

    @Test
    fun ancestorKeysEmptyForRootActive() {
        assertTrue(deep.ancestorKeysOf("dashboard").isEmpty())
    }

    @Test
    fun effectiveExpandedRevealsActiveBranchEvenWhenCollapsed() {
        // El usuario no expandió nada, pero la rama del activo se revela.
        assertEquals(setOf("admin", "users"), deep.effectiveExpandedKeys(emptySet(), "users-edit"))
    }

    @Test
    fun flattenHidesChildrenOfCollapsedBranch() {
        val rows = deep.flattenVisibleRows(expandedKeys = emptySet(), activeKey = null)
        val keys = rows.map { it.node.key }
        assertEquals(listOf("admin", "dashboard"), keys)
    }

    @Test
    fun flattenRevealsActivePathWithCorrectFlags() {
        val rows = deep.flattenVisibleRows(expandedKeys = emptySet(), activeKey = "users-edit")
        val keys = rows.map { it.node.key }
        // admin y users revelados (ancestros del activo), audit visible (hijo de admin).
        assertEquals(listOf("admin", "users", "users-list", "users-edit", "users-roles", "audit", "dashboard"), keys)

        val admin = rows.first { it.node.key == "admin" }
        assertTrue(admin.isOnActivePath)
        assertTrue(admin.isExpanded)
        assertFalse(admin.isActive)

        val active = rows.first { it.node.key == "users-edit" }
        assertTrue(active.isActive)
        assertFalse(active.isOnActivePath)
        assertFalse(active.hasChildren)
    }

    @Test
    fun flattenAssignsDepthPerLevel() {
        val rows = deep.flattenVisibleRows(expandedKeys = setOf("admin", "users"), activeKey = null)
        assertEquals(0, rows.first { it.node.key == "admin" }.depth)
        assertEquals(1, rows.first { it.node.key == "users" }.depth)
        assertEquals(2, rows.first { it.node.key == "users-edit" }.depth)
    }

    @Test
    fun tenChildrenAllVisibleWhenExpandedNoOverflowPartition() {
        val rows = tenChildren.flattenVisibleRows(expandedKeys = setOf("reports"), activeKey = null)
        val childRows = rows.filter { it.depth == 1 }
        // Los 10 hijos presentes (scroll natural, sin "Más").
        assertEquals(10, childRows.size)
        assertEquals((1..10).map { "report-$it" }, childRows.map { it.node.key })
    }

    @Test
    fun branchRowExposesHasChildrenLeafDoesNot() {
        val rows = deep.flattenVisibleRows(expandedKeys = setOf("admin"), activeKey = null)
        assertTrue(rows.first { it.node.key == "admin" }.hasChildren)
        assertFalse(rows.first { it.node.key == "audit" }.hasChildren)
    }

    // --- containsKey (plan 050 F1, resaltado del riel colapsado) -------------------------

    @Test
    fun containsKeyMatchesSelf() {
        assertTrue(deep.first { it.key == "admin" }.containsKey("admin"))
    }

    @Test
    fun containsKeyMatchesDeepDescendant() {
        // "admin" contiene al nieto "users-edit" (activo profundo → resalta la sección L1).
        assertTrue(deep.first { it.key == "admin" }.containsKey("users-edit"))
    }

    @Test
    fun containsKeyFalseForForeignKeyAndNull() {
        val admin = deep.first { it.key == "admin" }
        assertFalse(admin.containsKey("dashboard"))
        assertFalse(admin.containsKey(null))
    }
}
