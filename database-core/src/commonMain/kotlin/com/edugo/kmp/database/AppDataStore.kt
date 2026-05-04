package com.edugo.kmp.database

interface AppDataStore {
    // Data Cache (CachedDataLoader)
    suspend fun getCache(cacheKey: String): CacheEntry?
    suspend fun putCache(cacheKey: String, dataJson: String, cachedAtMillis: Long)
    suspend fun removeCache(cacheKey: String)
    suspend fun clearAllCache()

    // Screen Cache (CachedScreenLoader)
    suspend fun getScreenCache(screenKey: String): ScreenCacheEntry?
    suspend fun putScreenCache(
        screenKey: String,
        definitionJson: String,
        cachedAtMillis: Long,
        version: Int
    )

    suspend fun removeScreenCache(screenKey: String)
    suspend fun clearAllScreenCache()

    // Sync Store (LocalSyncStore)
    suspend fun getSyncValue(key: String): String?
    suspend fun putSyncValue(key: String, value: String)
    suspend fun removeSyncValue(key: String)
    suspend fun getAllSyncKeys(): List<String>
    suspend fun clearAllSync()

    // Mutation Queue (MutationQueue)
    suspend fun getAllMutations(): List<MutationEntry>
    suspend fun getFirstPending(): MutationEntry?
    suspend fun insertMutation(mutation: MutationEntry)
    suspend fun updateMutationStatus(id: String, status: String, retryCount: Int)
    suspend fun removeMutation(id: String)
    suspend fun hasDuplicate(endpoint: String, method: String, bodyJson: String): Boolean
    suspend fun clearAllMutations()
}

data class CacheEntry(
    val cacheKey: String,
    val dataJson: String,
    val cachedAtMillis: Long,
)

data class ScreenCacheEntry(
    val screenKey: String,
    val definitionJson: String,
    val cachedAtMillis: Long,
    val version: Int,
)

data class MutationEntry(
    val id: String,
    val endpoint: String,
    val method: String,
    val bodyJson: String,
    val createdAt: Long,
    val retryCount: Int,
    val maxRetries: Int,
    val status: String,
    val entityUpdatedAt: String?,
)
