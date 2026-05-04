package com.edugo.kmp.auth.authorization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoleHierarchyTest {
    // Test implementation of Role
    private data class TestRole(
        override val name: String,
        override val displayName: String,
        override val level: Int,
    ) : Role

    private val viewer = TestRole("VIEWER", "Viewer", 10)
    private val editor = TestRole("EDITOR", "Editor", 20)
    private val admin = TestRole("ADMIN", "Admin", 30)
    private val superAdmin = TestRole("SUPER_ADMIN", "Super Admin", 40)

    private val hierarchy = RoleHierarchy(listOf(viewer, editor, admin, superAdmin))

    // ==================== allRoles ====================

    @Test
    fun `allRoles returns roles sorted by level ascending`() {
        val roles = hierarchy.allRoles()

        assertEquals(4, roles.size)
        assertEquals(viewer, roles[0])
        assertEquals(editor, roles[1])
        assertEquals(admin, roles[2])
        assertEquals(superAdmin, roles[3])
    }

    // ==================== getRolesAtLeast ====================

    @Test
    fun `getRolesAtLeast returns roles with level greater or equal`() {
        val roles = hierarchy.getRolesAtLeast(editor)

        assertEquals(3, roles.size)
        assertTrue(roles.contains(editor))
        assertTrue(roles.contains(admin))
        assertTrue(roles.contains(superAdmin))
        assertFalse(roles.contains(viewer))
    }

    @Test
    fun `getRolesAtLeast with highest role returns only that role`() {
        val roles = hierarchy.getRolesAtLeast(superAdmin)

        assertEquals(1, roles.size)
        assertEquals(superAdmin, roles[0])
    }

    @Test
    fun `getRolesAtLeast with lowest role returns all roles`() {
        val roles = hierarchy.getRolesAtLeast(viewer)

        assertEquals(4, roles.size)
    }

    // ==================== getRolesAtMost ====================

    @Test
    fun `getRolesAtMost returns roles with level less or equal`() {
        val roles = hierarchy.getRolesAtMost(editor)

        assertEquals(2, roles.size)
        assertTrue(roles.contains(viewer))
        assertTrue(roles.contains(editor))
        assertFalse(roles.contains(admin))
    }

    @Test
    fun `getRolesAtMost with lowest role returns only that role`() {
        val roles = hierarchy.getRolesAtMost(viewer)

        assertEquals(1, roles.size)
        assertEquals(viewer, roles[0])
    }

    // ==================== getHighestRole / getLowestRole ====================

    @Test
    fun `getHighestRole returns role with highest level`() {
        val highest =
            hierarchy.getHighestRole(
                listOf(viewer, admin, editor),
            )

        assertEquals(admin, highest)
    }

    @Test
    fun `getHighestRole with empty list returns null`() {
        val highest = hierarchy.getHighestRole(emptyList())

        assertNull(highest)
    }

    @Test
    fun `getLowestRole returns role with lowest level`() {
        val lowest =
            hierarchy.getLowestRole(
                listOf(admin, viewer, editor),
            )

        assertEquals(viewer, lowest)
    }

    @Test
    fun `getLowestRole with empty list returns null`() {
        val lowest = hierarchy.getLowestRole(emptyList())

        assertNull(lowest)
    }

    @Test
    fun `getHighestRole with single role returns that role`() {
        val highest = hierarchy.getHighestRole(listOf(editor))

        assertEquals(editor, highest)
    }

    // ==================== isHigherThan / isAtLeast ====================

    @Test
    fun `isHigherThan returns true when roleA has higher level`() {
        assertTrue(hierarchy.isHigherThan(admin, editor))
    }

    @Test
    fun `isHigherThan returns false when levels are equal`() {
        assertFalse(hierarchy.isHigherThan(admin, admin))
    }

    @Test
    fun `isHigherThan returns false when roleA has lower level`() {
        assertFalse(hierarchy.isHigherThan(viewer, admin))
    }

    @Test
    fun `isAtLeast returns true when roleA has higher level`() {
        assertTrue(hierarchy.isAtLeast(admin, editor))
    }

    @Test
    fun `isAtLeast returns true when levels are equal`() {
        assertTrue(hierarchy.isAtLeast(admin, admin))
    }

    @Test
    fun `isAtLeast returns false when roleA has lower level`() {
        assertFalse(hierarchy.isAtLeast(viewer, admin))
    }

    // ==================== findByName ====================

    @Test
    fun `findByName returns role when exists`() {
        val role = hierarchy.findByName("ADMIN")

        assertEquals(admin, role)
    }

    @Test
    fun `findByName returns null when not exists`() {
        val role = hierarchy.findByName("NONEXISTENT")

        assertNull(role)
    }

    @Test
    fun `findByName is case sensitive`() {
        val role = hierarchy.findByName("admin")

        assertNull(role)
    }
}
