package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Cubre la regla de override por-API del modo mixto del frontend.
 *
 * Estos tests verifican la regla pura ([LocalApiOverride]); la task Gradle
 * `generateAppConfigs` aplica la MISMA regla en build-time. El override real
 * sobre [GeneratedConfigs] se valida ejecutando esa task con
 * `-Penv=STAGING -PlocalApis=...` (evidencia de generación), no en este test:
 * `GeneratedConfigs` se hornea sin flags en CI, así que aquí no se asume override
 * aplicado a la config generada.
 */
class LocalApiOverrideTest {

    /** ApiConfig de referencia que imita a STAGING (URLs *.run.app). */
    private val stagingApi = ApiConfigImpl(
        identityBaseUrl = "https://edugo-api-identity-eckbvbu3pa-ue.a.run.app",
        academicBaseUrl = "https://edugo-api-academic-eckbvbu3pa-ue.a.run.app",
        learningBaseUrl = "https://edugo-api-learning-eckbvbu3pa-ue.a.run.app",
        platformBaseUrl = "https://edugo-api-platform-eckbvbu3pa-ue.a.run.app",
        messagingBaseUrl = "https://edugo-api-messaging-eckbvbu3pa-ue.a.run.app",
    )

    // --- Mapeo nombre→puerto (fuente de verdad, alineado con APIs Go / Makefile) ---

    @Test
    fun ports_match_go_apis() {
        assertEquals(8070, LocalApiOverride.ports["identity"])
        assertEquals(8060, LocalApiOverride.ports["academic"])
        assertEquals(8065, LocalApiOverride.ports["learning"])
        assertEquals(8075, LocalApiOverride.ports["platform"])
        assertEquals(8080, LocalApiOverride.ports["messaging"])
        assertEquals(5, LocalApiOverride.ports.size)
    }

    @Test
    fun localUrl_builds_localhost_url() {
        assertEquals("http://localhost:8060", LocalApiOverride.localUrl("academic"))
        assertEquals("http://localhost:8070", LocalApiOverride.localUrl("identity"))
    }

    // --- Parseo del CSV `-PlocalApis` ---

    @Test
    fun parse_null_or_blank_is_empty() {
        assertTrue(LocalApiOverride.parse(null).isEmpty())
        assertTrue(LocalApiOverride.parse("").isEmpty())
        assertTrue(LocalApiOverride.parse("   ").isEmpty())
    }

    @Test
    fun parse_normalizes_case_and_whitespace() {
        assertEquals(
            setOf("academic", "learning"),
            LocalApiOverride.parse(" Academic , LEARNING "),
        )
    }

    @Test
    fun parse_rejects_unknown_api() {
        val ex = assertFailsWith<IllegalArgumentException> {
            LocalApiOverride.parse("academic,bogus")
        }
        assertTrue(ex.message!!.contains("bogus"))
    }

    // --- Regla de override sobre el ApiConfig base ---

    @Test
    fun no_override_returns_base_intact() {
        val result = LocalApiOverride.applyTo(stagingApi, emptySet())
        // Sin override = mismo objeto, entorno intacto.
        assertSame(stagingApi, result)
    }

    @Test
    fun override_academic_only_redirects_academic_rest_staging() {
        val localApis = LocalApiOverride.parse("academic")
        val result = LocalApiOverride.applyTo(stagingApi, localApis)

        // Solo academic → localhost.
        assertEquals("http://localhost:8060", result.academicBaseUrl)
        // Las otras 3 quedan en STAGING (*.run.app).
        assertEquals(stagingApi.identityBaseUrl, result.identityBaseUrl)
        assertEquals(stagingApi.learningBaseUrl, result.learningBaseUrl)
        assertEquals(stagingApi.platformBaseUrl, result.platformBaseUrl)
        assertTrue(result.identityBaseUrl.contains("run.app"))
        assertTrue(result.learningBaseUrl.contains("run.app"))
        assertTrue(result.platformBaseUrl.contains("run.app"))
    }

    @Test
    fun override_multiple_apis_redirects_each() {
        val localApis = LocalApiOverride.parse("academic,learning")
        val result = LocalApiOverride.applyTo(stagingApi, localApis)

        assertEquals("http://localhost:8060", result.academicBaseUrl)
        assertEquals("http://localhost:8065", result.learningBaseUrl)
        // identity y platform siguen en STAGING.
        assertEquals(stagingApi.identityBaseUrl, result.identityBaseUrl)
        assertEquals(stagingApi.platformBaseUrl, result.platformBaseUrl)
    }

    @Test
    fun override_all_apis_redirects_all_to_localhost() {
        val localApis = LocalApiOverride.parse("identity,academic,learning,platform")
        val result = LocalApiOverride.applyTo(stagingApi, localApis)

        assertEquals("http://localhost:8070", result.identityBaseUrl)
        assertEquals("http://localhost:8060", result.academicBaseUrl)
        assertEquals("http://localhost:8065", result.learningBaseUrl)
        assertEquals("http://localhost:8075", result.platformBaseUrl)
    }
}
