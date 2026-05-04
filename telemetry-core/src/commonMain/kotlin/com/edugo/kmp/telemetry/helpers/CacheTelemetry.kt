package com.edugo.kmp.telemetry.helpers

import com.edugo.kmp.telemetry.MetricNames
import com.edugo.kmp.telemetry.Telemetry

/**
 * Registra un cache hit.
 *
 * @param cacheType Tipo de cache (memory, disk, etc.).
 * @param key Clave consultada.
 */
public fun Telemetry.recordCacheHit(cacheType: String, key: String) {
    val labels = mapOf("cache_type" to cacheType, "key" to key)
    metrics.counter(MetricNames.CACHE_HITS_TOTAL, labels = labels)
    analytics.trackEvent("cache_hit", mapOf("cache_type" to cacheType, "key" to key))
}

/**
 * Registra un cache miss.
 *
 * @param cacheType Tipo de cache (memory, disk, etc.).
 * @param key Clave consultada.
 */
public fun Telemetry.recordCacheMiss(cacheType: String, key: String) {
    val labels = mapOf("cache_type" to cacheType, "key" to key)
    metrics.counter(MetricNames.CACHE_MISSES_TOTAL, labels = labels)
    analytics.trackEvent("cache_miss", mapOf("cache_type" to cacheType, "key" to key))
}
