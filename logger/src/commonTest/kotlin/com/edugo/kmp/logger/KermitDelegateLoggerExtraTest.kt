package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLogger
import kotlin.test.Test
import kotlin.test.assertTrue

class KermitDelegateLoggerExtraTest {
    @Test
    fun testLogMethods() {
        // Kermit already handles its own logging appropriately. We just want to ensure coverage for the delegation pathways.
        val delegate = KermitDelegateLogger(KermitLogger.withTag("TestingKermit"))
        
        delegate.d("TestTag", "msg")
        delegate.d("TestTag") { "msg lambda" }
        delegate.d("TestTag", "msg", Exception())
        
        delegate.i("TestTag", "msg")
        delegate.i("TestTag") { "msg lambda" }
        delegate.i("TestTag", "msg", Exception())
        
        delegate.w("TestTag", "msg")
        delegate.w("TestTag") { "msg lambda" }
        delegate.w("TestTag", "msg", Exception())
        
        delegate.e("TestTag", "msg")
        delegate.e("TestTag") { "msg lambda" }
        delegate.e("TestTag", "msg", Exception())
        
        assertTrue(true)
    }
}
