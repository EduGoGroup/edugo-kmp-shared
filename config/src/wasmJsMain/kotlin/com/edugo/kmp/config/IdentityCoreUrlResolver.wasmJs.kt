package com.edugo.kmp.config

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        try {
            if (typeof document === 'undefined') return '';
            var meta = document.querySelector('meta[name="identity-core-base-url"]');
            return meta ? (meta.getAttribute('content') || '') : '';
        } catch (e) { console.error('[IdentityCoreUrlResolver] meta[name=identity-core-base-url] read failed:', e); return ''; }
    }"""
)
private external fun readMetaIdentityCoreUrl(): String

// La web lee `<meta name="identity-core-base-url">`, que el build inyecta en
// `index.html` con la ruta del proxy del mismo origen (HU-DEMO.3). Sin ella,
// devuelve null y manda el JSON del ambiente.
internal actual fun readPlatformIdentityCoreUrl(): String? =
    readMetaIdentityCoreUrl().trim().ifEmpty { null }
