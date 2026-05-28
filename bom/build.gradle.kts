plugins {
    `java-platform`
    id("kmp.quality")
    id("kmp.publish")
}

javaPlatform {
    allowDependencies()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

dependencies {
    constraints {
        val targetVersion = project.version.toString()
        // Heredados de Fase 2 (3 artefactos)
        api("com.edugo.kmp:foundation:$targetVersion")
        api("com.edugo.kmp:core:$targetVersion")
        api("com.edugo.kmp:validation:$targetVersion")
        // Nuevos en Fase 3 (5 artefactos) — DA-23/P43: composite-build local
        api("com.edugo.kmp:logger:$targetVersion")
        api("com.edugo.kmp:config:$targetVersion")
        api("com.edugo.kmp:storage:$targetVersion")
        api("com.edugo.kmp:settings:$targetVersion")
        api("com.edugo.kmp:telemetry-core:$targetVersion")
        // Nuevos en Fase 4 (2 artefactos) — DA-23/P43: composite-build local
        api("com.edugo.kmp:network:$targetVersion")
        api("com.edugo.kmp:database-core:$targetVersion")
        // Nuevo en Fase 5 (1 artefacto) — DA-12: composite-build local; publicación 0.1.0 en Fase 7
        api("com.edugo.kmp:auth-core:$targetVersion")
        // Nuevos en Fase 6 (2 artefactos)
        api("com.edugo.kmp:design-core:$targetVersion")
        api("com.edugo.kmp:resources-core:$targetVersion")
    }
}
