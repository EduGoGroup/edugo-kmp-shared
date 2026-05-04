package com.edugo.kmp.auth.authorization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionCheckerImplTest {
    // Test implementations using data classes (enums can't override 'name')
    private data class TestRole(
        override val name: String,
        override val displayName: String,
        override val level: Int,
    ) : Role

    private data class TestPermission(
        override val name: String,
        override val resource: String,
        override val action: String,
    ) : Permission

    // Roles
    private val viewer = TestRole("VIEWER", "Viewer", 10)
    private val editor = TestRole("EDITOR", "Editor", 20)
    private val admin = TestRole("ADMIN", "Admin", 30)

    // Permissions
    private val viewContent = TestPermission("VIEW_CONTENT", "content", "view")
    private val editContent = TestPermission("EDIT_CONTENT", "content", "edit")
    private val deleteContent = TestPermission("DELETE_CONTENT", "content", "delete")
    private val manageUsers = TestPermission("MANAGE_USERS", "users", "manage")
    private val viewReports = TestPermission("VIEW_REPORTS", "reports", "view")

    private val allPermissions = setOf(viewContent, editContent, deleteContent, manageUsers, viewReports)

    private val checker =
        PermissionCheckerImpl<TestRole, TestPermission> { role ->
            when (role) {
                viewer -> setOf(viewContent)
                editor -> setOf(viewContent, editContent, viewReports)
                admin -> allPermissions
                else -> emptySet()
            }
        }

    // ==================== hasPermission ====================

    @Test
    fun `hasPermission returns true when role has permission`() {
        assertTrue(checker.hasPermission(viewer, viewContent))
    }

    @Test
    fun `hasPermission returns false when role lacks permission`() {
        assertFalse(checker.hasPermission(viewer, editContent))
    }

    @Test
    fun `hasPermission admin has all permissions`() {
        allPermissions.forEach { permission ->
            assertTrue(
                checker.hasPermission(admin, permission),
                "Admin should have permission: ${permission.name}",
            )
        }
    }

    @Test
    fun `hasPermission editor has view and edit but not manage`() {
        assertTrue(checker.hasPermission(editor, viewContent))
        assertTrue(checker.hasPermission(editor, editContent))
        assertFalse(checker.hasPermission(editor, manageUsers))
        assertFalse(checker.hasPermission(editor, deleteContent))
    }

    // ==================== hasAnyPermission ====================

    @Test
    fun `hasAnyPermission returns true when role has at least one`() {
        assertTrue(
            checker.hasAnyPermission(viewer, setOf(viewContent, editContent)),
        )
    }

    @Test
    fun `hasAnyPermission returns false when role has none`() {
        assertFalse(
            checker.hasAnyPermission(viewer, setOf(editContent, deleteContent)),
        )
    }

    @Test
    fun `hasAnyPermission with empty set returns false`() {
        assertFalse(
            checker.hasAnyPermission(admin, emptySet()),
        )
    }

    // ==================== hasAllPermissions ====================

    @Test
    fun `hasAllPermissions returns true when role has all`() {
        assertTrue(
            checker.hasAllPermissions(editor, setOf(viewContent, editContent)),
        )
    }

    @Test
    fun `hasAllPermissions returns false when role lacks one`() {
        assertFalse(
            checker.hasAllPermissions(editor, setOf(viewContent, deleteContent)),
        )
    }

    @Test
    fun `hasAllPermissions with empty set returns true`() {
        assertTrue(
            checker.hasAllPermissions(viewer, emptySet()),
        )
    }

    @Test
    fun `hasAllPermissions admin has all permissions`() {
        assertTrue(
            checker.hasAllPermissions(admin, allPermissions),
        )
    }

    // ==================== getEffectivePermissions ====================

    @Test
    fun `getEffectivePermissions returns correct permissions for viewer`() {
        val permissions = checker.getEffectivePermissions(viewer)

        assertEquals(1, permissions.size)
        assertTrue(permissions.contains(viewContent))
    }

    @Test
    fun `getEffectivePermissions returns correct permissions for editor`() {
        val permissions = checker.getEffectivePermissions(editor)

        assertEquals(3, permissions.size)
        assertTrue(permissions.contains(viewContent))
        assertTrue(permissions.contains(editContent))
        assertTrue(permissions.contains(viewReports))
    }

    @Test
    fun `getEffectivePermissions returns all permissions for admin`() {
        val permissions = checker.getEffectivePermissions(admin)

        assertEquals(allPermissions.size, permissions.size)
    }

    // ==================== Permission interface properties ====================

    @Test
    fun `permission has correct resource and action`() {
        assertEquals("content", editContent.resource)
        assertEquals("edit", editContent.action)
        assertEquals("EDIT_CONTENT", editContent.name)
    }

    // ==================== Role interface properties ====================

    @Test
    fun `role has correct properties`() {
        assertEquals("EDITOR", editor.name)
        assertEquals("Editor", editor.displayName)
        assertEquals(20, editor.level)
    }
}
