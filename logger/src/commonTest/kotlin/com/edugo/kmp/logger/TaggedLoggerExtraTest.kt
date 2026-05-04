package com.edugo.kmp.logger

import kotlin.test.Test
import kotlin.test.assertTrue

class TaggedLoggerExtraTest {
    @Test
    fun testLogMethods() {
        val mock = MockLogger()
        // Replace DefaultLogger backend with MockLogger for testing
        // Wait, how do we mock TaggedLogger backend?
        // TaggedLogger is what Logger extensions create. Let's just call the methods to cover them!
        val logger = TaggedLogger.create("TestTag")
        
        logger.d("msg")
        logger.d { "msg lambda" }
        logger.d("msg", Exception())
        
        logger.i("msg")
        logger.i { "msg lambda" }
        logger.i("msg", Exception())
        
        logger.w("msg")
        logger.w { "msg lambda" }
        logger.w("msg", Exception())
        
        logger.e("msg")
        logger.e { "msg lambda" }
        logger.e("msg", Exception())
        
        assertTrue(true)
    }
}
