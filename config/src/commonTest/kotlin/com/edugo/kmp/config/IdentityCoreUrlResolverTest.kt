package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Orden de [IdentityCoreUrlResolver.resolve]: el valor de la plataforma, si
 * existe, gana al del JSON; sin él, manda el JSON; un valor vacío no cuenta.
 *
 * El valor de la plataforma se inyecta por parámetro, así que la prueba no
 * depende del `actual` de cada plataforma.
 */
internal class IdentityCoreUrlResolverTest {

    @Test
    fun platformValueWinsOverJson() {
        val resolved = IdentityCoreUrlResolver.resolve(
            appConfigWithCore(JSON_URL),
            platformValue = "/identity-core",
        )

        assertEquals("/identity-core", resolved)
    }

    @Test
    fun absentPlatformValueFallsBackToJson() {
        val resolved = IdentityCoreUrlResolver.resolve(appConfigWithCore(JSON_URL), platformValue = null)

        assertEquals(JSON_URL, resolved)
    }

    @Test
    fun emptyPlatformValueDoesNotCount() {
        val resolved = IdentityCoreUrlResolver.resolve(appConfigWithCore(JSON_URL), platformValue = "")

        assertEquals(JSON_URL, resolved)
    }

    @Test
    fun blankPlatformValueDoesNotCount() {
        val resolved = IdentityCoreUrlResolver.resolve(appConfigWithCore(JSON_URL), platformValue = "   ")

        assertEquals(JSON_URL, resolved)
    }

    @Test
    fun platformValueIsTrimmed() {
        val resolved = IdentityCoreUrlResolver.resolve(
            appConfigWithCore(JSON_URL),
            platformValue = "  /identity-core  ",
        )

        assertEquals("/identity-core", resolved)
    }

    @Test
    fun emptyEverywhereGivesEmpty() {
        val resolved = IdentityCoreUrlResolver.resolve(appConfigWithCore(""), platformValue = null)

        assertEquals("", resolved)
    }

    private fun appConfigWithCore(identityCoreBaseUrl: String): AppConfig = AppConfigImpl(
        environmentName = "STAGING",
        network = NetworkConfigImpl(timeout = 30000L, webPort = 8080, debugMode = false),
        behavior = BehaviorConfigImpl(mockMode = false),
        api = ApiConfigImpl(
            identityBaseUrl = "https://identity.test",
            academicBaseUrl = "https://academic.test",
            learningBaseUrl = "https://learning.test",
            platformBaseUrl = "https://platform.test",
            identityCoreBaseUrl = identityCoreBaseUrl,
        ),
        telemetry = TelemetryConfigImpl(otelEndpoint = ""),
    )

    private companion object {
        const val JSON_URL = "https://identity-core.test"
    }
}
