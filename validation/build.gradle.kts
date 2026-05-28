plugins {
    id("kmp.logic.core")
    id("kmp.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":foundation"))
            }
        }
    }
}
