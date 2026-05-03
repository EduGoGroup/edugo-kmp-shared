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
