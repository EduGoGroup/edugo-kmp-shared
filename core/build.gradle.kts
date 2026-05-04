plugins {
    id("kmp.logic.core")
    // P43/P44: sin maven-publish en Fases 0-6.
}

version = "0.1.0-DEV"

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":foundation"))   // composite-build local resuelve el subproyecto
            }
        }
    }
}
