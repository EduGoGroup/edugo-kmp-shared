import groovy.json.JsonSlurper

plugins {
    id("kmp.android")
    id("kmp.publish")
}

val enableAndroid = findProperty("enableAndroid")?.toString()?.toBoolean() ?: false

// =============================================================================
// Code-gen de AppConfig por ambiente
// =============================================================================
// Los archivos `src/commonMain/resources/config/*.json` son la fuente de verdad
// versionada de la configuración por ambiente. En vez de leerlos en runtime
// (que falla en iOS porque `commonMain/resources` no se empaca en .framework/.app),
// los compilamos en build-time a un `object GeneratedConfigs` con constantes
// `AppConfig`. Resultado: cero I/O en runtime, comportamiento idéntico en
// Android/iOS/Desktop/Web, y validators (`require` de TelemetryConfig, init de
// AppConfigImpl) ejecutan en build-time si los JSON están mal — fail loud.
// =============================================================================
val configResourcesDir = layout.projectDirectory.dir("src/commonMain/resources/config")
val generatedConfigsDir = layout.buildDirectory.dir("generated/source/config/commonMain/kotlin")

val generateAppConfigs by tasks.registering {
    group = "build"
    description = "Genera GeneratedConfigs.kt a partir de los JSON en commonMain/resources/config/."

    inputs.dir(configResourcesDir).withPropertyName("configJsons")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(generatedConfigsDir).withPropertyName("generatedConfigsDir")

    // Capturas locales para no referenciar el script desde el `doLast` (requisito
    // de la configuration cache: el action no puede serializar al Project ni a
    // top-level helpers del script).
    val inputDirFile = configResourcesDir.asFile
    val outputDirProvider = generatedConfigsDir

    // Modo mixto del frontend: `-PlocalApis=<csv>` (p.ej. "academic,learning")
    // reescribe SOLO las baseURLs de esas APIs a `http://localhost:<puerto>` en el
    // entorno activo (`-Penv`), dejando el resto como el entorno base (STAGING/GCP).
    // La regla y el mapeo nombre→puerto son los mismos que
    // `com.edugo.kmp.config.LocalApiOverride` (fuente de verdad en commonMain); el
    // test `LocalApiOverridePortMapTest` guarda que ambos no divergan. Capturamos
    // las props en la fase de configuración para no romper la configuration cache.
    val localApisRaw = (findProperty("localApis")?.toString()).orEmpty()
    val activeEnvRaw = (findProperty("env")?.toString()).orEmpty()
    // Mapeo nombre-de-API → puerto local (= LocalApiOverride.ports).
    val localApiPorts = mapOf(
        "identity" to 8070,
        "academic" to 8060,
        "learning" to 8065,
        "platform" to 8075,
    )
    inputs.property("localApis", localApisRaw)
    inputs.property("activeEnv", activeEnvRaw)

    doLast {
        // Parseo + validación del CSV `localApis` (fail-loud ante nombres
        // desconocidos, igual que el contrato de config).
        val requestedApis = localApisRaw.split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        val unknownApis = requestedApis.filterNot { localApiPorts.containsKey(it) }
        if (unknownApis.isNotEmpty()) {
            throw GradleException(
                "-PlocalApis contiene APIs desconocidas: ${unknownApis.joinToString(", ")}. " +
                    "Valores aceptados: ${localApiPorts.keys.sorted().joinToString(", ")}.",
            )
        }
        val localApis = requestedApis.toSet()
        // Enum constant del entorno activo (DEV, DEV_LAN, STAGING, PROD) que recibe
        // el override; si no se pasó `-Penv`, no se aplica a ningún entorno.
        val activeEnvConst = activeEnvRaw.trim().uppercase().replace('-', '_')

        val outDir = outputDirProvider.get().asFile.resolve("com/edugo/kmp/config")
        outDir.mkdirs()
        val outFile = outDir.resolve("GeneratedConfigs.kt")

        fun kotlinStringLiteral(s: String): String {
            val escaped = s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\$", "\\\$")
            return "\"" + escaped + "\""
        }

        // Orden determinista: case-insensitive sobre el filename.
        val jsonFiles = inputDirFile.listFiles { f ->
            f.isFile && f.name.endsWith(".json")
        }?.sortedBy { it.name.lowercase() } ?: emptyList()

        if (jsonFiles.isEmpty()) {
            throw GradleException(
                "No se encontraron archivos config/*.json en ${inputDirFile.absolutePath}."
            )
        }

        val slurper = JsonSlurper()
        val entries = mutableListOf<Pair<String, String>>()

        jsonFiles.forEach { jsonFile ->
            @Suppress("UNCHECKED_CAST")
            val parsed = slurper.parse(jsonFile) as? Map<String, Any?>
                ?: throw GradleException("JSON inválido o no-objeto en ${jsonFile.name}.")

            val envName = parsed["environmentName"]?.toString()
                ?: throw GradleException("Falta 'environmentName' en ${jsonFile.name}.")

            // Enum constant name: DEV, DEV_LAN, STAGING, PROD (uppercase + underscore).
            val constName = envName.trim().uppercase().replace('-', '_')

            @Suppress("UNCHECKED_CAST")
            val network = (parsed["network"] as? Map<String, Any?>)
                ?: throw GradleException("Falta bloque 'network' en ${jsonFile.name}.")
            @Suppress("UNCHECKED_CAST")
            val behavior = (parsed["behavior"] as? Map<String, Any?>)
                ?: throw GradleException("Falta bloque 'behavior' en ${jsonFile.name}.")
            @Suppress("UNCHECKED_CAST")
            val api = (parsed["api"] as? Map<String, Any?>)
                ?: throw GradleException("Falta bloque 'api' en ${jsonFile.name}.")
            @Suppress("UNCHECKED_CAST")
            val telemetry = (parsed["telemetry"] as? Map<String, Any?>) ?: emptyMap()

            val timeoutMs = (network["timeout"] as? Number)?.toLong()
                ?: throw GradleException("Falta 'network.timeout' (Long) en ${jsonFile.name}.")
            val webPort = (network["webPort"] as? Number)?.toInt()
                ?: throw GradleException("Falta 'network.webPort' (Int) en ${jsonFile.name}.")
            val debugMode = network["debugMode"] as? Boolean
                ?: throw GradleException("Falta 'network.debugMode' (Boolean) en ${jsonFile.name}.")

            val mockMode = behavior["mockMode"] as? Boolean ?: false
            val testUserSelectorEnabled = behavior["testUserSelectorEnabled"] as? Boolean ?: false

            val identityBaseUrlRaw = api["identityBaseUrl"]?.toString()
                ?: throw GradleException("Falta 'api.identityBaseUrl' en ${jsonFile.name}.")
            val academicBaseUrlRaw = api["academicBaseUrl"]?.toString()
                ?: throw GradleException("Falta 'api.academicBaseUrl' en ${jsonFile.name}.")
            val learningBaseUrlRaw = api["learningBaseUrl"]?.toString()
                ?: throw GradleException("Falta 'api.learningBaseUrl' en ${jsonFile.name}.")
            val platformBaseUrlRaw = api["platformBaseUrl"]?.toString()
                ?: throw GradleException("Falta 'api.platformBaseUrl' en ${jsonFile.name}.")

            // Override del modo mixto: solo en el entorno activo (`-Penv`) y solo
            // para las APIs listadas en `-PlocalApis`. El resto queda intacto.
            val overrideThisEnv = localApis.isNotEmpty() && constName == activeEnvConst
            fun localUrlFor(apiName: String): String =
                "http://localhost:${localApiPorts.getValue(apiName)}"
            fun mixedUrl(apiName: String, baseUrl: String): String =
                if (overrideThisEnv && apiName in localApis) localUrlFor(apiName) else baseUrl

            val identityBaseUrl = mixedUrl("identity", identityBaseUrlRaw)
            val academicBaseUrl = mixedUrl("academic", academicBaseUrlRaw)
            val learningBaseUrl = mixedUrl("learning", learningBaseUrlRaw)
            val platformBaseUrl = mixedUrl("platform", platformBaseUrlRaw)

            val otelEndpoint = telemetry["otelEndpoint"]?.toString() ?: ""

            val literal = buildString {
                append("    val ").append(constName).append(": AppConfig = AppConfigImpl(\n")
                append("        environmentName = ").append(kotlinStringLiteral(envName)).append(",\n")
                append("        network = NetworkConfigImpl(\n")
                append("            timeout = ").append(timeoutMs).append("L,\n")
                append("            webPort = ").append(webPort).append(",\n")
                append("            debugMode = ").append(debugMode).append(",\n")
                append("        ),\n")
                append("        behavior = BehaviorConfigImpl(\n")
                append("            mockMode = ").append(mockMode).append(",\n")
                append("            testUserSelectorEnabled = ").append(testUserSelectorEnabled).append(",\n")
                append("        ),\n")
                append("        api = ApiConfigImpl(\n")
                append("            identityBaseUrl = ").append(kotlinStringLiteral(identityBaseUrl)).append(",\n")
                append("            academicBaseUrl = ").append(kotlinStringLiteral(academicBaseUrl)).append(",\n")
                append("            learningBaseUrl = ").append(kotlinStringLiteral(learningBaseUrl)).append(",\n")
                append("            platformBaseUrl = ").append(kotlinStringLiteral(platformBaseUrl)).append(",\n")
                append("        ),\n")
                append("        telemetry = TelemetryConfigImpl(\n")
                append("            otelEndpoint = ").append(kotlinStringLiteral(otelEndpoint)).append(",\n")
                append("        ),\n")
                append("    )")
            }
            entries += constName to literal
        }

        val body = buildString {
            append("// AUTOGENERADO por la task `generateAppConfigs` (config/build.gradle.kts).\n")
            append("// NO EDITAR A MANO. La fuente de verdad son los JSON en\n")
            append("// src/commonMain/resources/config/.\n")
            append("package com.edugo.kmp.config\n\n")
            append("/**\n")
            append(" * Configuraciones por ambiente, compiladas en build-time a partir de los JSON\n")
            append(" * en `commonMain/resources/config/`. Consumido por [ConfigLoader.load].\n")
            append(" *\n")
            append(" * Sin I/O en runtime: la misma constante se sirve en Android, iOS, Desktop y Web.\n")
            append(" */\n")
            append("internal object GeneratedConfigs {\n")
            entries.forEachIndexed { idx, (_, literal) ->
                append(literal)
                if (idx != entries.lastIndex) append("\n\n") else append("\n")
            }
            append("}\n")
        }

        outFile.writeText(body)
    }
}

kotlin {
    // Fase 4 — `expect class EnvVarSource` (beta en Kotlin 2.x). El flag
    // está estabilizado en cuanto a contrato de uso; suprimimos el warning
    // para mantener limpio el output de compilación.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    if (enableAndroid) {
        (this as ExtensionAware).extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
            namespace = "com.edugo.kmp.config"
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateAppConfigs.map { it.outputs.files })
            dependencies {
                api(project(":foundation"))
                implementation(project(":core"))
            }
        }

        // Fase 4 — source set intermedio que comparte el `actual EnvVarSource`
        // entre Desktop y Android (ambos JVM, ambos detectores leen
        // `System.getProperty("app.environment")`).
        val commonTest by getting
        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }
        val desktopTest by getting {
            dependsOn(jvmCommonTest)
        }
        if (enableAndroid) {
            val androidHostTest by getting {
                dependsOn(jvmCommonTest)
            }
        }
    }
}
