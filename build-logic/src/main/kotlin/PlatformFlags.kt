import org.gradle.api.Project

object PlatformFlags {
    const val ENABLE_ANDROID = "enableAndroid"
    const val ENABLE_WEB = "enableWeb"
    const val ENABLE_IOS = "enableIos"
    const val ENABLE_COVERAGE = "enableCoverage"

    val allowedFlags = setOf(
        ENABLE_ANDROID,
        ENABLE_WEB,
        ENABLE_IOS,
        ENABLE_COVERAGE,
    )

    private fun raw(project: Project, flag: String): String? =
        project.findProperty(flag)?.toString()?.trim()

    private fun booleanFlag(project: Project, flag: String): Boolean {
        val value = raw(project, flag)?.takeIf { it.isNotEmpty() } ?: return false
        return value.lowercase().toBooleanStrictOrNull()
            ?: error("[PlatformFlags] Valor inválido para '$flag': '$value'. Usa 'true' o 'false'.")
    }

    fun android(project: Project): Boolean =
        booleanFlag(project, ENABLE_ANDROID)

    fun web(project: Project): Boolean =
        booleanFlag(project, ENABLE_WEB)

    fun ios(project: Project): Boolean =
        booleanFlag(project, ENABLE_IOS)

    fun coverage(project: Project): Boolean =
        booleanFlag(project, ENABLE_COVERAGE)

    /**
     * Propiedad **declarativa por módulo** que permite a un módulo renunciar al target Web (wasmJs)
     * aun cuando el corte global pida `enableWeb=true`.
     *
     * Default = `true`: salvo que el módulo declare lo contrario, hereda el comportamiento del corte
     * (si `enableWeb=true`, se añade wasmJs). Un módulo se marca con `ext["kmp.webSupported"] = false`
     * en su `build.gradle.kts` cuando su funcionalidad **no existe en web** y por tanto declarar el
     * target sería una mentira (no puede cumplir el contrato).
     *
     * Caso EduGo: la mensajería WhatsApp es **móvil-only por decisión explícita** (ADR 0029 — la web
     * pública no es custodio de llaves). Los módulos que la soportan (`:crypto` con libsodium de Ionspin,
     * que ni siquiera publica variante wasm; `:secure-storage` con Keystore/Keychain, sin equivalente
     * seguro en navegador) declaran `kmp.webSupported = false`: así su set de plataformas real es
     * android/ios/jvm, el grafo del publish atómico cierra (ya no se exige la variante wasm inexistente
     * de libsodium) y nadie hereda un target imposible de cumplir.
     */
    const val MODULE_WEB_SUPPORTED = "kmp.webSupported"

    fun webSupportedByModule(project: Project): Boolean {
        val value = project.findProperty(MODULE_WEB_SUPPORTED)?.toString()?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return true
        return value.lowercase().toBooleanStrictOrNull()
            ?: error("[PlatformFlags] Valor inválido para '$MODULE_WEB_SUPPORTED' en '${project.path}': '$value'. Usa 'true' o 'false'.")
    }

    /**
     * IntelliJ may keep stale KMP build hooks that invoke iOS binary tasks even when iOS is disabled.
     * Register no-op compatibility tasks so desktop-only workflows don't fail after toggling enableIos=false.
     */
    fun registerIosCompatibilityTasks(project: Project, enableIos: Boolean) {
        if (enableIos) return

        val iosBinaryTasks = listOf(
            "iosArm64Binaries",
            "iosArm64MainBinaries",
            "iosArm64TestBinaries",
            "iosSimulatorArm64Binaries",
            "iosSimulatorArm64MainBinaries",
            "iosSimulatorArm64TestBinaries",
            "iosX64Binaries",
            "iosX64MainBinaries",
            "iosX64TestBinaries",
        )

        iosBinaryTasks.forEach { taskName ->
            if (project.tasks.findByName(taskName) == null) {
                project.tasks.register(taskName) {
                    group = "kotlin multiplatform"
                    description = "No-op compatibility task when enableIos=false."
                }
            }
        }
    }
}
