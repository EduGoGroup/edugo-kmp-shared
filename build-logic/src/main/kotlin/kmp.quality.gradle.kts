import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

// Configuración detekt
detekt {
    toolVersion = "1.23.7"
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    parallel = true
    ignoreFailures = false
    basePath = rootDir.absolutePath
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(false)
        md.required.set(false)
    }
    jvmTarget = "21"
}

// Configuración ktlint
extensions.configure<KtlintExtension>("ktlint") {
    version.set("1.3.1")
    android.set(false)
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/build/**", "**/generated/**")
    }
}
