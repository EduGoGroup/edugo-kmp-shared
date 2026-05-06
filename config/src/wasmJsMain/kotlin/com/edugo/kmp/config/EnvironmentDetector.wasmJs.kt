package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try { return (typeof window !== 'undefined' && window.__APP_ENVIRONMENT__) ? String(window.__APP_ENVIRONMENT__) : ''; }
        catch (e) { console.error('[EnvironmentDetector] window.__APP_ENVIRONMENT__ read failed:', e); return ''; }
    }"""
)
private external fun readWindowEnvironment(): String

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try {
            if (typeof document === 'undefined') return '';
            var meta = document.querySelector('meta[name="app-environment"]');
            return meta ? (meta.getAttribute('content') || '') : '';
        } catch (e) { console.error('[EnvironmentDetector] meta[name=app-environment] read failed:', e); return ''; }
    }"""
)
private external fun readMetaEnvironment(): String

// Order: window.__APP_ENVIRONMENT__ (runtime injection) → <meta name="app-environment">
// (build-time injection in index.html, Fase 2). If neither is present with a
// non-blank value, fail with an actionable message. The build-time bridge from
// `-Penv=` to `BUILD_ENVIRONMENT` is performed in Web/Main.kt (callsite).
internal actual fun detectPlatformEnvironment(): Environment {
    val windowValue = readWindowEnvironment().trim()
    if (windowValue.isNotEmpty()) {
        return Environment.fromString(windowValue)
            ?: environmentInvalidError("Web", windowValue)
    }

    val metaValue = readMetaEnvironment().trim()
    if (metaValue.isNotEmpty()) {
        return Environment.fromString(metaValue)
            ?: environmentInvalidError("Web", metaValue)
    }

    environmentMissingError("Web")
}
