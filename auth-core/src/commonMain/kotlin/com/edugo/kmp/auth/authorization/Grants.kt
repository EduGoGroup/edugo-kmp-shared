package com.edugo.kmp.auth.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format D2: lista de patterns `allow` + lista `deny`.
 * Mirror Kotlin de `auth.Grants` (Go).
 *
 * Serialización JSON canónica: `{"allow":[...],"deny":[...]}`.
 */
@Serializable
data class Grants(
    @SerialName("allow") val allow: List<String> = emptyList(),
    @SerialName("deny") val deny: List<String> = emptyList(),
) {
    /**
     * Evalúa con deny precedence: si algún `deny` matchea → `false`.
     * Si no, `true` sólo si algún `allow` matchea. Default deny
     * (sin allow → `false`). Mirror de `auth.EvaluateGrants`.
     */
    fun evaluate(request: String): Boolean {
        if (deny.any { PermissionMatcher.matches(it, request) }) return false
        return allow.any { PermissionMatcher.matches(it, request) }
    }

    companion object {
        val EMPTY: Grants = Grants()
    }
}
