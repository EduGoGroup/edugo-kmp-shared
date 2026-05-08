/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage.reset

import com.edugo.kmp.telemetry.AnalyticsRecorder
import com.edugo.kmp.telemetry.CrashRecorder
import com.edugo.kmp.telemetry.Telemetry
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResetClientStateUseCaseTest {

    private val capturedEvents = mutableListOf<Pair<String, Map<String, Any?>>>()
    private val capturedCrashLogs = mutableListOf<String>()

    private val testTelemetry = Telemetry(
        analytics = object : AnalyticsRecorder {
            override fun trackEvent(name: String, properties: Map<String, Any?>) {
                capturedEvents += name to properties
            }
            override fun trackScreen(screenName: String, properties: Map<String, Any?>) = Unit
            override fun setUserProperty(name: String, value: String?) = Unit
            override fun setUserId(userId: String?) = Unit
        },
        crash = object : CrashRecorder {
            override fun recordException(throwable: Throwable, context: Map<String, String>) = Unit
            override fun log(message: String) {
                capturedCrashLogs += message
            }
            override fun setUserId(userId: String?) = Unit
        }
    )

    @AfterTest
    fun reset() {
        capturedEvents.clear()
        capturedCrashLogs.clear()
    }

    @Test
    fun `invoke clears every store and emits one event per store`() = runTest {
        val storeA = FakeStore(id = "auth.tokens")
        val storeB = FakeStore(id = "sdui.bundle")
        val storeC = FakeStore(id = "app.settings")

        val useCase = ResetClientStateUseCase(
            stores = listOf(storeA, storeB, storeC),
            telemetry = testTelemetry,
        )

        useCase()

        assertTrue(storeA.cleared)
        assertTrue(storeB.cleared)
        assertTrue(storeC.cleared)

        assertEquals(3, capturedEvents.size)
        assertEquals(ResetClientStateUseCase.EVENT_NAME, capturedEvents[0].first)
        assertEquals(mapOf("store" to "auth.tokens"), capturedEvents[0].second)
        assertEquals(mapOf("store" to "sdui.bundle"), capturedEvents[1].second)
        assertEquals(mapOf("store" to "app.settings"), capturedEvents[2].second)
        assertTrue(capturedCrashLogs.isEmpty())
    }

    @Test
    fun `invoke continues with remaining stores when one throws`() = runTest {
        val storeA = FakeStore(id = "auth.tokens")
        val storeB = FakeStore(id = "sdui.bundle", throwOnClear = RuntimeException("disk full"))
        val storeC = FakeStore(id = "app.settings")

        val useCase = ResetClientStateUseCase(
            stores = listOf(storeA, storeB, storeC),
            telemetry = testTelemetry,
        )

        useCase()

        assertTrue(storeA.cleared)
        assertTrue(storeB.cleared, "FakeStore marks cleared=true before throwing — expected by contract")
        assertTrue(storeC.cleared)

        // Solo A y C emiten success; B emite crash log.
        assertEquals(2, capturedEvents.size)
        assertEquals(mapOf("store" to "auth.tokens"), capturedEvents[0].second)
        assertEquals(mapOf("store" to "app.settings"), capturedEvents[1].second)

        assertEquals(1, capturedCrashLogs.size)
        assertTrue(capturedCrashLogs[0].contains("sdui.bundle"))
        assertTrue(capturedCrashLogs[0].contains("disk full"))
    }

    @Test
    fun `invoke is no-op when stores list is empty`() = runTest {
        val useCase = ResetClientStateUseCase(
            stores = emptyList(),
            telemetry = testTelemetry,
        )

        useCase()

        assertTrue(capturedEvents.isEmpty())
        assertTrue(capturedCrashLogs.isEmpty())
    }

    @Test
    fun `event name is the documented constant`() {
        assertEquals("client_state_reset", ResetClientStateUseCase.EVENT_NAME)
    }

    private class FakeStore(
        override val id: String,
        private val throwOnClear: Throwable? = null,
    ) : ClientStateStore {
        var cleared: Boolean = false
            private set

        override suspend fun clear() {
            cleared = true
            throwOnClear?.let { throw it }
        }
    }
}
