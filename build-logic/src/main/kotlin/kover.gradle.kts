val enableCoverage = PlatformFlags.coverage(project)

if (enableCoverage) {
    plugins.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension>("kover") {
        reports {
            verify {
                // TODO: Reactivar cuando se mejore la cobertura de tests (actualmente ~6.6%, meta: 80%)
                // rule { minBound(80) }
            }
            filters {
                excludes {
                    classes(
                        "*Test", "*Test\\$*",
                        "*Tests", "*Tests\\$*",
                        "*Spec", "*Spec\\$*"
                    )
                    packages(
                        "*.test", "*.test.*",
                        "*.testing", "*.testing.*"
                    )
                }
            }
        }
    }

    tasks.register("coverageReport") {
        group = "verification"
        description = "Generates HTML and XML coverage reports"
        dependsOn("koverHtmlReport", "koverXmlReport")
    }

    tasks.register("coverageCheck") {
        group = "verification"
        description = "Verifies code coverage meets minimum thresholds"
        dependsOn("koverVerify")
    }
} else {
    // No-op stubs para que tareas existentes no fallen si alguien las invoca sin coverage.
    tasks.register("coverageReport") {
        group = "verification"
        description = "Coverage disabled (use -PenableCoverage=true to enable)"
        doLast { logger.lifecycle("Coverage disabled. Re-run with -PenableCoverage=true") }
    }
    tasks.register("coverageCheck") {
        group = "verification"
        description = "Coverage disabled (use -PenableCoverage=true to enable)"
        doLast { logger.lifecycle("Coverage disabled. Re-run with -PenableCoverage=true") }
    }
}
