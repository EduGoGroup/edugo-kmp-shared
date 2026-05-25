plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Plugins KMP/Android/Compose (mismas versiones que edugo-ui-kmp)
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
    implementation("com.android.tools.build:gradle:9.2.1")
    implementation("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:1.10.3")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.3.20")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.8")
    implementation("app.cash.sqldelight:gradle-plugin:2.1.0")

    // DA-17: calidad estática shared-only
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:12.1.1")
}
