package com.edugo.kmp.auth.config

import com.edugo.kmp.config.Environment
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthConfigTest {
    @Test
    fun demoUsesTheSameAuthConfigAsStaging() {
        assertEquals(
            AuthConfig.forEnvironment(Environment.STAGING),
            AuthConfig.forEnvironment(Environment.DEMO),
        )
    }
}
