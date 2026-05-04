package com.edugo.kmp.database

class InMemoryAppDataStore : AppDataStore {

    private val dataCache = mutableMapOf<String, CacheEntry>()
    private val screenCache = mutableMapOf<String, ScreenCacheEntry>()
    private val syncStore = mutableMapOf<String, String>()
    private val mutationQueue = mutableListOf<MutationEntry>()

    // ── Data Cache ──────────────────────────────────────────

    override suspend fun getCache(cacheKey: String): CacheEntry? = dataCache[cacheKey]

    override suspend fun putCache(cacheKey: String, dataJson: String, cachedAtMillis: Long) {
        dataCache[cacheKey] = CacheEntry(cacheKey, dataJson, cachedAtMillis)
    }

    override suspend fun removeCache(cacheKey: String) {
        dataCache.remove(cacheKey)
    }

    override suspend fun clearAllCache() {
        dataCache.clear()
    }

    // ── Screen Cache ────────────────────────────────────────

    override suspend fun getScreenCache(screenKey: String): ScreenCacheEntry? =
        screenCache[screenKey]

    override suspend fun putScreenCache(
        screenKey: String,
        definitionJson: String,
        cachedAtMillis: Long,
        version: Int,
    ) {
        screenCache[screenKey] =
            ScreenCacheEntry(screenKey, definitionJson, cachedAtMillis, version)
    }

    override suspend fun removeScreenCache(screenKey: String) {
        screenCache.remove(screenKey)
    }

    override suspend fun clearAllScreenCache() {
        screenCache.clear()
    }

    // ── Sync Store ──────────────────────────────────────────

    override suspend fun getSyncValue(key: String): String? = syncStore[key]

    override suspend fun putSyncValue(key: String, value: String) {
        syncStore[key] = value
    }

    override suspend fun removeSyncValue(key: String) {
        syncStore.remove(key)
    }

    override suspend fun getAllSyncKeys(): List<String> = syncStore.keys.toList()

    override suspend fun clearAllSync() {
        syncStore.clear()
    }

    // ── Mutation Queue ──────────────────────────────────────

    override suspend fun getAllMutations(): List<MutationEntry> =
        mutationQueue.sortedBy { it.createdAt }

    override suspend fun getFirstPending(): MutationEntry? =
        mutationQueue
            .filter { it.status == "PENDING" }
            .minByOrNull { it.createdAt }

    override suspend fun insertMutation(mutation: MutationEntry) {
        mutationQueue.removeAll { it.id == mutation.id }
        mutationQueue.add(mutation)
    }

    override suspend fun updateMutationStatus(id: String, status: String, retryCount: Int) {
        val index = mutationQueue.indexOfFirst { it.id == id }
        if (index >= 0) {
            mutationQueue[index] =
                mutationQueue[index].copy(status = status, retryCount = retryCount)
        }
    }

    override suspend fun removeMutation(id: String) {
        mutationQueue.removeAll { it.id == id }
    }

    override suspend fun hasDuplicate(endpoint: String, method: String, bodyJson: String): Boolean =
        mutationQueue.any {
            it.endpoint == endpoint && it.method == method && it.bodyJson == bodyJson && it.status == "PENDING"
        }

    override suspend fun clearAllMutations() {
        mutationQueue.clear()
    }
}
