package com.edugo.kmp.config

internal const val ENV_VAR_NAME = "APP_ENVIRONMENT"

private val VALID_VALUES: String
    get() = Environment.entries.joinToString(", ") { it.name }

private val MISSING_HINT: String =
    """
      - Desktop:  -Dapp.environment=DEV  o  export APP_ENVIRONMENT=DEV
      - Android:  Gradle property -Penv=DEV  (run config tipo GradleRunConfiguration)
      - iOS:      Xcode scheme → Environment Variables → APP_ENVIRONMENT=DEV
      - Web:      Gradle property -Penv=DEV  (run config tipo GradleRunConfiguration)
    """.trimIndent()

internal fun environmentMissingError(platform: String): Nothing {
    error(
        buildString {
            append(ENV_VAR_NAME).append(" no definido (plataforma: ").append(platform).append("). ")
            append("Define la variable así:\n")
            append(MISSING_HINT).append('\n')
            append("Valores válidos: ").append(VALID_VALUES)
        }
    )
}

internal fun environmentInvalidError(platform: String, rawValue: String): Nothing {
    error(
        "$ENV_VAR_NAME=\"$rawValue\" no es un valor válido (plataforma: $platform). " +
            "Valores válidos: $VALID_VALUES"
    )
}
