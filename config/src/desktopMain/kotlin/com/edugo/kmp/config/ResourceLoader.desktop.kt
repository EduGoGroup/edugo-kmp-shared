package com.edugo.kmp.config

/**
 * Desktop implementation: loads from classpath resources, falls back to DefaultConfigs.
 */
internal actual fun loadResourceAsString(path: String): String? {
    try {
        val stream = object {}.javaClass.classLoader?.getResourceAsStream(path)
        if (stream != null) {
            return stream.bufferedReader().use { it.readText() }
        }
    } catch (ex: Exception) {
        println("Error loading resource at $path: ${ex.message}")
        ex.printStackTrace()
        // Resource not found, fall through to fallback
    }

    return DefaultConfigs.get(path)
}
