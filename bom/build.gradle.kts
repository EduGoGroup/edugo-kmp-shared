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

// Versión unificada: todos los constraints apuntan a la versión del build
// (`project.version` = -Pversion), de modo que cada corte atómico pinea los 15
// módulos a la MISMA versión y el grafo del BOM cierra por construcción.
// (Antes se leía versions.properties por-módulo, lo que producía referencias
// fantasma como foundation:0.1.3 cuando un módulo se publicaba solo.)
//
// Nota sobre :crypto y :secure-storage (mensajería, ADR 0029): son módulos móvil-only
// (android/ios/jvm, SIN variante wasm — ver kmp.webSupported=false en settings.gradle.kts).
// El BOM solo PINEA su versión; no fuerza variantes. Un consumidor web (wasmJs) no debe
// referenciarlos: el smoke-resolve de wasmJs del CI no los lista por ser móvil-only.
dependencies {
    constraints {
        // Foundation (Fase 2)
        api("com.edugo.kmp:foundation:${project.version}")
        api("com.edugo.kmp:core:${project.version}")
        api("com.edugo.kmp:validation:${project.version}")
        // Infraestructura (Fase 3)
        api("com.edugo.kmp:logger:${project.version}")
        api("com.edugo.kmp:config:${project.version}")
        api("com.edugo.kmp:storage:${project.version}")
        api("com.edugo.kmp:settings:${project.version}")
        api("com.edugo.kmp:telemetry-core:${project.version}")
        // Datos & red (Fase 4)
        api("com.edugo.kmp:network:${project.version}")
        api("com.edugo.kmp:database-core:${project.version}")
        // Auth (Fase 5) — DA-12
        api("com.edugo.kmp:auth-core:${project.version}")
        // UI (Fase 6)
        api("com.edugo.kmp:design-core:${project.version}")
        api("com.edugo.kmp:resources-core:${project.version}")
        // Mensajería (plan 025 / ADR 0029) — móvil-only (android/ios/jvm, sin wasm)
        api("com.edugo.kmp:crypto:${project.version}")
        api("com.edugo.kmp:secure-storage:${project.version}")
    }
}
