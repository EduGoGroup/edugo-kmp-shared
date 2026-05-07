package com.edugo.kmp.config

internal class DesktopEnvironmentDetectorContractTest : EnvironmentDetectorContractTest() {
    override val expectedPlatformLabel: String = "Desktop"
}
