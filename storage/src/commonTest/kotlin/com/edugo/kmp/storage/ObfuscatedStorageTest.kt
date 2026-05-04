package com.edugo.kmp.storage

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.logger.withTag
import com.russhwolf.settings.MapSettings
import kotlin.test.*

class ObfuscatedStorageTest {

    private lateinit var obfuscatedStorage: ObfuscatedStorage
    private lateinit var safeStorage: SafeEduGoStorage
    private val testLogger = NoOpLogger().withTag("EduGo.Storage.Test")

    @BeforeTest
    fun setup() {
        StorageErrorHandler.configure(testLogger)
        val storage = EduGoStorage.withSettings(MapSettings())
        safeStorage = SafeEduGoStorage(storage, validateKeys = false, logger = testLogger)
        obfuscatedStorage = ObfuscatedStorage(
            delegate = safeStorage,
            obfuscator = XorObfuscator(),
            sensitiveKeys = setOf("auth_token", "auth_user", "auth_context")
        )
    }

    @AfterTest
    fun teardown() {
        StorageErrorHandler.resetLogger()
    }

    // =========================================================================
    // ROUNDTRIP: write plain text and read back matches
    // =========================================================================

    @Test
    fun `sensitive key roundtrip preserves original value`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"
        val result = obfuscatedStorage.putStringSafe("auth_token", token)
        assertTrue(result is Result.Success)

        val retrieved = obfuscatedStorage.getStringSafe("auth_token")
        assertEquals(token, retrieved)
    }

    @Test
    fun `auth_user roundtrip preserves JSON value`() {
        val userJson = """{"id":123,"name":"John Doe","email":"john@example.com"}"""
        obfuscatedStorage.putStringSafe("auth_user", userJson)

        val retrieved = obfuscatedStorage.getStringSafe("auth_user")
        assertEquals(userJson, retrieved)
    }

    @Test
    fun `auth_context roundtrip preserves complex JSON`() {
        val contextJson = """{"roleId":"r1","roleName":"admin","schoolId":"s1","permissions":["read","write"]}"""
        obfuscatedStorage.putStringSafe("auth_context", contextJson)

        val retrieved = obfuscatedStorage.getStringSafe("auth_context")
        assertEquals(contextJson, retrieved)
    }

    // =========================================================================
    // STORED VALUE IS NOT PLAIN TEXT
    // =========================================================================

    @Test
    fun `stored value for sensitive key is obfuscated`() {
        val token = "my-secret-token-12345"
        obfuscatedStorage.putStringSafe("auth_token", token)

        // Read raw value from underlying storage — should NOT be the plain token
        val rawValue = safeStorage.getStringSafe("auth_token")
        assertNotEquals(token, rawValue)
        assertTrue(rawValue.startsWith("ENC:"), "Obfuscated value should start with ENC: prefix")
    }

    @Test
    fun `stored value for auth_user is not plain text`() {
        val userJson = """{"id":1,"name":"Test"}"""
        obfuscatedStorage.putStringSafe("auth_user", userJson)

        val rawValue = safeStorage.getStringSafe("auth_user")
        assertNotEquals(userJson, rawValue)
        assertTrue(rawValue.startsWith("ENC:"))
    }

    // =========================================================================
    // NON-SENSITIVE KEYS PASS THROUGH UNCHANGED
    // =========================================================================

    @Test
    fun `non-sensitive key is stored as plain text`() {
        obfuscatedStorage.putStringSafe("app_theme", "dark")

        val rawValue = safeStorage.getStringSafe("app_theme")
        assertEquals("dark", rawValue)
    }

    @Test
    fun `non-sensitive key roundtrip works normally`() {
        obfuscatedStorage.putStringSafe("language", "es")
        assertEquals("es", obfuscatedStorage.getStringSafe("language"))
    }

    @Test
    fun `int operations pass through unchanged`() {
        obfuscatedStorage.putIntSafe("count", 42)
        assertEquals(42, obfuscatedStorage.getIntSafe("count"))
    }

    @Test
    fun `boolean operations pass through unchanged`() {
        obfuscatedStorage.putBooleanSafe("enabled", true)
        assertTrue(obfuscatedStorage.getBooleanSafe("enabled"))
    }

    // =========================================================================
    // EMPTY STRINGS
    // =========================================================================

    @Test
    fun `empty string sensitive key roundtrip`() {
        obfuscatedStorage.putStringSafe("auth_token", "")
        val retrieved = obfuscatedStorage.getStringSafe("auth_token")
        assertEquals("", retrieved)
    }

    @Test
    fun `empty string stored value has prefix`() {
        obfuscatedStorage.putStringSafe("auth_token", "")
        val rawValue = safeStorage.getStringSafe("auth_token")
        assertEquals("ENC:", rawValue)
    }

    // =========================================================================
    // SPECIAL CHARACTERS AND JSON
    // =========================================================================

    @Test
    fun `roundtrip with unicode characters`() {
        val value = "usuario: \u00e9\u00e0\u00fc\u00f1 \u2603 \u2764"
        obfuscatedStorage.putStringSafe("auth_user", value)
        assertEquals(value, obfuscatedStorage.getStringSafe("auth_user"))
    }

    @Test
    fun `roundtrip with nested JSON and special chars`() {
        val json = """{"name":"O'Brien","notes":"Line1\nLine2","tags":["a","b"]}"""
        obfuscatedStorage.putStringSafe("auth_context", json)
        assertEquals(json, obfuscatedStorage.getStringSafe("auth_context"))
    }

    @Test
    fun `roundtrip with long JWT-like token`() {
        val token = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4iLCJpYXQiOjE1MTYyMzkwMjJ9." +
            "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        obfuscatedStorage.putStringSafe("auth_token", token)
        assertEquals(token, obfuscatedStorage.getStringSafe("auth_token"))
    }

    // =========================================================================
    // MIGRATION: plain text values without prefix
    // =========================================================================

    @Test
    fun `reading pre-existing plain text value returns it as-is`() {
        // Simulate a value stored before obfuscation was enabled
        safeStorage.putStringSafe("auth_token", "plain-old-token")

        // ObfuscatedStorage should detect lack of ENC: prefix and return as-is
        val retrieved = obfuscatedStorage.getStringSafe("auth_token")
        assertEquals("plain-old-token", retrieved)
    }

    // =========================================================================
    // DEFAULT VALUES
    // =========================================================================

    @Test
    fun `getStringSafe returns default for missing sensitive key`() {
        val value = obfuscatedStorage.getStringSafe("auth_token", "fallback")
        assertEquals("fallback", value)
    }

    @Test
    fun `getStringSafe returns default for missing non-sensitive key`() {
        val value = obfuscatedStorage.getStringSafe("missing_key", "default")
        assertEquals("default", value)
    }

    // =========================================================================
    // REMOVE AND CLEAR
    // =========================================================================

    @Test
    fun `removeSafe works for sensitive key`() {
        obfuscatedStorage.putStringSafe("auth_token", "secret")
        val result = obfuscatedStorage.removeSafe("auth_token")
        assertTrue(result is Result.Success)
        assertFalse(obfuscatedStorage.containsSafe("auth_token"))
    }

    @Test
    fun `clearSafe removes all data`() {
        obfuscatedStorage.putStringSafe("auth_token", "secret")
        obfuscatedStorage.putStringSafe("app_theme", "dark")
        obfuscatedStorage.clearSafe()
        assertFalse(obfuscatedStorage.containsSafe("auth_token"))
        assertFalse(obfuscatedStorage.containsSafe("app_theme"))
    }

    // =========================================================================
    // XOR OBFUSCATOR UNIT TESTS
    // =========================================================================

    @Test
    fun `XorObfuscator obfuscate produces prefixed output`() {
        val obfuscator = XorObfuscator()
        val result = obfuscator.obfuscate("hello")
        assertTrue(result.startsWith("ENC:"))
    }

    @Test
    fun `XorObfuscator roundtrip is symmetric`() {
        val obfuscator = XorObfuscator()
        val original = "test-data-1234"
        val obfuscated = obfuscator.obfuscate(original)
        val deobfuscated = obfuscator.deobfuscate(obfuscated)
        assertEquals(original, deobfuscated)
    }

    @Test
    fun `XorObfuscator deobfuscate returns plain text without prefix`() {
        val obfuscator = XorObfuscator()
        // Value without ENC: prefix should be returned as-is
        assertEquals("plain-value", obfuscator.deobfuscate("plain-value"))
    }

    @Test
    fun `XorObfuscator handles empty string`() {
        val obfuscator = XorObfuscator()
        val obfuscated = obfuscator.obfuscate("")
        assertEquals("ENC:", obfuscated)
        assertEquals("", obfuscator.deobfuscate(obfuscated))
    }

    // =========================================================================
    // SafeEduGoStorage INTEGRATED OBFUSCATION TESTS
    // =========================================================================

    @Test
    fun `SafeEduGoStorage with obfuscator roundtrip works`() {
        val storage = EduGoStorage.withSettings(MapSettings())
        val safe = SafeEduGoStorage.wrapWithObfuscation(
            storage = storage,
            sensitiveKeys = setOf("auth_token"),
            validateKeys = false
        )

        safe.putStringSafe("auth_token", "my-secret")
        assertEquals("my-secret", safe.getStringSafe("auth_token"))
    }

    @Test
    fun `SafeEduGoStorage with obfuscator stores encrypted value`() {
        val rawStorage = EduGoStorage.withSettings(MapSettings())
        val safe = SafeEduGoStorage.wrapWithObfuscation(
            storage = rawStorage,
            sensitiveKeys = setOf("auth_token"),
            validateKeys = false
        )

        safe.putStringSafe("auth_token", "my-secret")
        // Verify raw storage has encrypted value
        val rawValue = rawStorage.getString("auth_token")
        assertNotEquals("my-secret", rawValue)
        assertTrue(rawValue.startsWith("ENC:"))
    }

    @Test
    fun `SafeEduGoStorage without obfuscator stores plain value`() {
        val rawStorage = EduGoStorage.withSettings(MapSettings())
        val safe = SafeEduGoStorage.wrap(rawStorage, validateKeys = false)

        safe.putStringSafe("auth_token", "my-secret")
        assertEquals("my-secret", rawStorage.getString("auth_token"))
    }

    @Test
    fun `SafeEduGoStorage obfuscator skips non-sensitive keys`() {
        val rawStorage = EduGoStorage.withSettings(MapSettings())
        val safe = SafeEduGoStorage.wrapWithObfuscation(
            storage = rawStorage,
            sensitiveKeys = setOf("auth_token"),
            validateKeys = false
        )

        safe.putStringSafe("app_theme", "dark")
        assertEquals("dark", rawStorage.getString("app_theme"))
    }
}
