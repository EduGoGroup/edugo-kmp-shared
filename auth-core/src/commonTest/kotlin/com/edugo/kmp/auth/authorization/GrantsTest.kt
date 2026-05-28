package com.edugo.kmp.auth.authorization

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Paridad 1:1 con `auth.EvaluateGrants` (Go).
 * Reglas: deny precedence, default deny.
 */
class GrantsTest {
    private val json = Json { encodeDefaults = true }

    // ==================== Default deny ====================

    @Test
    fun `empty grants denies everything`() {
        val g = Grants.EMPTY
        assertFalse(g.evaluate("users.read"))
        assertFalse(g.evaluate("anything"))
        assertFalse(g.evaluate(""))
    }

    // ==================== Allow simple ====================

    @Test
    fun `simple allow grants exact request`() {
        val g = Grants(allow = listOf("users.read"))
        assertTrue(g.evaluate("users.read"))
        assertFalse(g.evaluate("users.write"))
    }

    // ==================== Deny precedence ====================

    @Test
    fun `deny precedence blocks specific request inside subtree allow`() {
        val g = Grants(allow = listOf("users.*"), deny = listOf("users.write"))
        assertTrue(g.evaluate("users.read"))
        assertFalse(g.evaluate("users.write"))
    }

    @Test
    fun `deny wildcard overrides specific allow`() {
        val g = Grants(allow = listOf("users.write"), deny = listOf("users.*"))
        assertFalse(g.evaluate("users.write"))
        assertFalse(g.evaluate("users.read"))
    }

    // ==================== Wildcard allow global ====================

    @Test
    fun `allow star with specific deny`() {
        val g = Grants(allow = listOf("*"), deny = listOf("users.write"))
        assertTrue(g.evaluate("users.read"))
        assertTrue(g.evaluate("anything.else"))
        assertFalse(g.evaluate("users.write"))
    }

    // ==================== Serialización ====================

    @Test
    fun `serializes to canonical JSON shape`() {
        val g = Grants(allow = listOf("a"), deny = listOf("b"))
        val encoded = json.encodeToString(Grants.serializer(), g)
        assertEquals("""{"allow":["a"],"deny":["b"]}""", encoded)
    }

    @Test
    fun `round-trip JSON preserves allow and deny`() {
        val original =
            Grants(
                allow = listOf("users.*", "materials.read"),
                deny = listOf("users.write"),
            )
        val encoded = json.encodeToString(Grants.serializer(), original)
        val decoded = json.decodeFromString(Grants.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `decodes empty grants from JSON`() {
        val decoded = json.decodeFromString(Grants.serializer(), """{"allow":[],"deny":[]}""")
        assertEquals(Grants.EMPTY, decoded)
        assertFalse(decoded.evaluate("users.read"))
    }
}
