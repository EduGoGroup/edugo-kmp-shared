plugins {
    id("kmp.logic.core")
    id("kmp.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))   // composite-build local resuelve el subproyecto
            }
        }
    }
}
