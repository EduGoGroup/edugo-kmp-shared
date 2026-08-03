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

    // Overrides de Compose (ver config/detekt/detekt.yml). Sin esto, el 62% de los hallazgos
    // son falsos positivos de convención del framework.
    val sharedDetektConfig = rootProject.file("config/detekt/detekt.yml")
    if (sharedDetektConfig.exists()) {
        config.setFrom(sharedDetektConfig)
    }

    // Grandfathering explícito de la deuda que había ACUMULADA mientras el gate no analizaba nada
    // (deuda 059). No es «apagar el rojo»: el baseline congela lo conocido y deja que CUALQUIER
    // hallazgo nuevo salga en rojo, que es lo que hace útil al gate desde hoy. Lo congelado son
    // 77 findings reales (59 design-core + 18 auth-core) — el desglose y el plan de saldo están en
    // la ficha de la deuda 059. Regenerar el baseline para tapar findings nuevos es justo lo que
    // pudrió el de ui-kmp (deuda 052): no se hace.
    val moduleBaseline = layout.projectDirectory.file("detekt-baseline.xml").asFile
    if (moduleBaseline.exists()) {
        baseline = moduleBaseline
    }

    // Deuda 059: sin esto, detekt busca `src/main/kotlin` —que en un módulo KMP no existe— y la
    // tarea sale `NO-SOURCE`: verde estructural sin analizar ni un archivo. Era un gate fantasma
    // en los 15 módulos. Se apuntan los source sets KMP que existan en cada módulo (la lista se
    // filtra por existencia, así que un módulo sin iOS o sin Web no rompe).
    source.setFrom(
        listOf(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/desktopMain/kotlin",
            "src/jvmMain/kotlin",
            "src/iosMain/kotlin",
            "src/wasmJsMain/kotlin",
            "src/commonTest/kotlin",
            "src/desktopTest/kotlin",
        ).map { layout.projectDirectory.dir(it) }.filter { it.asFile.exists() },
    )
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
