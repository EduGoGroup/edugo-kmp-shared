package com.edugo.kmp.auth.authorization

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Paridad 1:1 con `auth.PermissionMatches` (Go) y `iam.permission_matches()`
 * (Postgres). Cualquier cambio aquí debe replicarse en BE y DB.
 */
class PermissionMatcherTest {
    // ==================== Wildcard global `*` ====================

    @Test
    fun `wildcard star matches arbitrary requests`() {
        assertTrue(PermissionMatcher.matches("*", "users.read"))
        assertTrue(PermissionMatcher.matches("*", "admin.system.purge"))
        assertTrue(PermissionMatcher.matches("*", "materials.write:own"))
    }

    @Test
    fun `wildcard star matches empty request as contract edge case`() {
        // Documenta el contrato: `*` matchea cualquier cosa incluyendo cadena vacía.
        assertTrue(PermissionMatcher.matches("*", ""))
    }

    // ==================== Match exacto ====================

    @Test
    fun `exact pattern matches identical request`() {
        assertTrue(PermissionMatcher.matches("users.read", "users.read"))
    }

    @Test
    fun `exact pattern does not match different action`() {
        assertFalse(PermissionMatcher.matches("users.read", "users.write"))
    }

    @Test
    fun `empty pattern matches empty request only`() {
        // pattern == request → match aunque ambos sean cadena vacía.
        assertTrue(PermissionMatcher.matches("", ""))
        assertFalse(PermissionMatcher.matches("", "users.read"))
    }

    // ==================== Subárbol `prefix.*` ====================

    @Test
    fun `subtree wildcard matches the prefix itself`() {
        assertTrue(PermissionMatcher.matches("users.*", "users"))
    }

    @Test
    fun `subtree wildcard matches one level below`() {
        assertTrue(PermissionMatcher.matches("users.*", "users.read"))
    }

    @Test
    fun `subtree wildcard matches deeper levels`() {
        assertTrue(PermissionMatcher.matches("users.*", "users.read.deep"))
    }

    @Test
    fun `subtree wildcard does not match similar-named root`() {
        assertFalse(PermissionMatcher.matches("users.*", "usersx"))
        assertFalse(PermissionMatcher.matches("users.*", "usersx.read"))
    }

    @Test
    fun `subtree wildcard does not match unrelated root`() {
        assertFalse(PermissionMatcher.matches("users.*", "other.read"))
    }

    @Test
    fun `subtree wildcard does not bleed into sibling prefixes`() {
        // `admin.*` no debe matchear `admins.read` (diferente raíz).
        assertFalse(PermissionMatcher.matches("admin.*", "admins.read"))
        assertFalse(PermissionMatcher.matches("admin.*", "administration.read"))
    }

    // ==================== Sufijo `:own` ====================

    @Test
    fun `exact pattern without own does not match request with own`() {
        assertFalse(PermissionMatcher.matches("users.read", "users.read:own"))
    }

    @Test
    fun `exact pattern with own does not match request without own`() {
        assertFalse(PermissionMatcher.matches("users.read:own", "users.read"))
    }

    @Test
    fun `subtree wildcard matches own requests because of startsWith semantics`() {
        // Contrato actual 1:1 con Go: `users.*` matchea `users.read:own`
        // porque `users.read:own` empieza con `users.`. Lo dejamos
        // explícito acá para que cualquier cambio futuro sea consciente.
        assertTrue(PermissionMatcher.matches("users.*", "users.read:own"))
    }

    // ==================== Leading wildcard `*.suffix` ====================

    @Test
    fun `leading wildcard matches single segment prefix`() {
        assertTrue(PermissionMatcher.matches("*.create", "users.create"))
    }

    @Test
    fun `leading wildcard matches multi level path`() {
        assertTrue(PermissionMatcher.matches("*.create", "academic.units.create"))
        assertTrue(PermissionMatcher.matches("*.delete", "admin.users.delete"))
    }

    @Test
    fun `leading wildcard does not match different suffix`() {
        assertFalse(PermissionMatcher.matches("*.delete", "users.create"))
    }

    @Test
    fun `leading wildcard does not match own request`() {
        // El sufijo `:own` es semánticamente distinto: `*.create` no
        // matchea `users.create:own` porque el request termina en `:own`,
        // no en `.create`.
        assertFalse(PermissionMatcher.matches("*.create", "users.create:own"))
    }

    @Test
    fun `leading wildcard does not match single segment request`() {
        // Necesita al menos un `.` antes del sufijo.
        assertFalse(PermissionMatcher.matches("*.create", "create"))
    }

    @Test
    fun `leading wildcard does not match embedded suffix without dot delimiter`() {
        assertFalse(PermissionMatcher.matches("*.create", "usercreate"))
    }

    // ==================== Wildcard medio `prefix.*.suffix` ====================

    @Test
    fun `middle wildcard matches direct child path`() {
        assertTrue(PermissionMatcher.matches("academic.*.create", "academic.units.create"))
    }

    @Test
    fun `middle wildcard matches path with extra middle segments`() {
        assertTrue(PermissionMatcher.matches("academic.*.create", "academic.units.subitems.create"))
    }

    @Test
    fun `middle wildcard does not match without middle segment`() {
        assertFalse(PermissionMatcher.matches("academic.*.create", "academic.create"))
    }

    @Test
    fun `middle wildcard does not match different prefix`() {
        assertFalse(PermissionMatcher.matches("academic.*.create", "admin.units.create"))
    }

    @Test
    fun `middle wildcard does not match different suffix`() {
        assertFalse(PermissionMatcher.matches("academic.*.create", "academic.units.delete"))
    }

    @Test
    fun `middle wildcard does not match own request`() {
        assertFalse(PermissionMatcher.matches("academic.*.create", "academic.units.create:own"))
    }
}
