import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Centralized validation/reporting for platform flags used across build-logic convention plugins.
 */
object PlatformFlagsValidator {
    private const val VALIDATION_MARKER = "com.edugo.platformFlags.validationDone"

    fun validateAndReportFlags(project: Project) {
        val rootProject = project.rootProject
        val extraProperties = rootProject.extensions.extraProperties

        if (extraProperties.has(VALIDATION_MARKER)) {
            return
        }

        extraProperties.set(VALIDATION_MARKER, true)

        warnAboutUnknownFlags(rootProject)
        validateFlagValues(rootProject)
        reportPlatformStatus(rootProject)
    }

    private fun warnAboutUnknownFlags(project: Project) {
        val unknownFlags = project.properties.keys
            .filter { it.startsWith("enable") }
            .filter { it !in PlatformFlags.allowedFlags }
            .sorted()

        if (unknownFlags.isEmpty()) return

        project.logger.warn(
            buildString {
                appendLine("[PlatformFlags] Se detectaron flags desconocidos: ${unknownFlags.joinToString(", ")}")
                append("[PlatformFlags] Flags permitidos: ${PlatformFlags.allowedFlags.sorted().joinToString(", ")}")
            }
        )
    }

    private fun validateFlagValues(project: Project) {
        PlatformFlags.allowedFlags.sorted().forEach { flag ->
            val rawValue = project.findProperty(flag)?.toString()?.trim()

            if (rawValue.isNullOrEmpty()) return@forEach
            if (rawValue.lowercase().toBooleanStrictOrNull() != null) return@forEach

            throw GradleException(
                "[PlatformFlags] Valor inválido para '$flag': '$rawValue'. Usa 'true' o 'false'."
            )
        }
    }

    private fun reportPlatformStatus(project: Project) {
        val androidEnabled = PlatformFlags.android(project)
        val webEnabled = PlatformFlags.web(project)
        val iosEnabled = PlatformFlags.ios(project)
        val coverageEnabled = PlatformFlags.coverage(project)

        project.logger.lifecycle(
            buildString {
                appendLine("[PlatformFlags] Estado del build:")
                appendLine("  • Desktop/Common: activo siempre")
                appendLine("  • Android: ${statusLabel(androidEnabled)} (${PlatformFlags.ENABLE_ANDROID}=${androidEnabled})")
                appendLine("  • Web: ${statusLabel(webEnabled)} (${PlatformFlags.ENABLE_WEB}=${webEnabled})")
                appendLine("  • iOS: ${statusLabel(iosEnabled)} (${PlatformFlags.ENABLE_IOS}=${iosEnabled})")
                appendLine("  • Coverage: ${statusLabel(coverageEnabled)} (${PlatformFlags.ENABLE_COVERAGE}=${coverageEnabled})")
                appendLine("  • Para incluir plataformas extra:")
                appendLine("      - make android | make web | make ios | make validate | make build-all-platforms | make ci")
                appendLine("      - ./gradlew :platforms:mobile:androidApp:assembleDebug -PenableAndroid=true")
                appendLine("      - ./gradlew :platforms:web:app:wasmJsBrowserDistribution -PenableWeb=true")
                append("      - ./gradlew :platforms:mobile:app:linkDebugFrameworkIosSimulatorArm64 -PenableIos=true")
            }
        )
    }

    private fun statusLabel(enabled: Boolean): String = if (enabled) "habilitado" else "deshabilitado"
}


