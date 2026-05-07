package com.edugo.kmp.config

/**
 * DSL declarativo para describir casos parametrizados de detección de
 * variables de entorno. Define **qué se espera**; la plataforma aporta
 * `EnvVarSource` para inyectar el valor.
 *
 * Ejemplo:
 *
 * ```
 * val matrix = EnvVarMatrix.build {
 *     resolves(AppEnvVar.APP_ENVIRONMENT, "STAGING", to = Environment.STAGING)
 *     missing(AppEnvVar.APP_ENVIRONMENT)
 *     invalid(AppEnvVar.APP_ENVIRONMENT, "FOO")
 * }
 * ```
 */
internal class EnvVarMatrix private constructor(
    val cases: List<Case>
) {
    data class Case(
        val variable: AppEnvVar,
        val rawValue: String?,
        val expected: Outcome
    )

    sealed interface Outcome {
        /** El detector debe resolver la variable al [Environment] dado. */
        data class ResolvesTo(val env: Environment) : Outcome

        /** El detector debe fallar con mensaje accionable de "missing". */
        data object FailsWithMissing : Outcome

        /** El detector debe fallar con mensaje accionable de "invalid". */
        data class FailsWithInvalid(val echoedValue: String) : Outcome
    }

    class Builder {
        private val cases = mutableListOf<Case>()

        fun resolves(variable: AppEnvVar, raw: String, to: Environment): Builder {
            cases += Case(variable, raw, Outcome.ResolvesTo(to))
            return this
        }

        fun missing(variable: AppEnvVar): Builder {
            cases += Case(variable, null, Outcome.FailsWithMissing)
            return this
        }

        fun invalid(variable: AppEnvVar, raw: String): Builder {
            cases += Case(variable, raw, Outcome.FailsWithInvalid(raw))
            return this
        }

        fun build() = EnvVarMatrix(cases.toList())
    }

    companion object {
        fun build(block: Builder.() -> Unit): EnvVarMatrix =
            Builder().apply(block).build()
    }
}
