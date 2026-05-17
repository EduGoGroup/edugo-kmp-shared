package com.edugo.kmp.design.components.lists.testing

import com.edugo.kmp.design.platform.PlatformDetector
import com.edugo.kmp.design.platform.PlatformType

internal object TestPlatformDetector {
    inline fun <T> withPlatform(platform: PlatformType, block: () -> T): T {
        val previous = PlatformDetector.current
        return try {
            PlatformDetector.current = platform
            block()
        } finally {
            PlatformDetector.current = previous
        }
    }
}
