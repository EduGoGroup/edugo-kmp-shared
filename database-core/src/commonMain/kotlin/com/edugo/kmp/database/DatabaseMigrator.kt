package com.edugo.kmp.database

import com.edugo.kmp.storage.SafeEduGoStorage
import com.edugo.kmp.telemetry.Telemetry
import kotlinx.serialization.json.Json

class DatabaseMigrator(
    private val storage: SafeEduGoStorage,
    private val dataStore: AppDataStore,
    private val telemetry: Telemetry = Telemetry.Noop,
) {
    companion object {
        private const val MIGRATION_DONE_KEY = "db.migration.v1.done"
    }

    suspend fun migrateIfNeeded() {
        if (storage.getBooleanSafe(MIGRATION_DONE_KEY)) return

        migrateDataCache()
        migrateSyncStore()
        migrateScreenCache()
        migrateMutationQueue()

        storage.putBooleanSafe(MIGRATION_DONE_KEY, true)
    }

    private suspend fun migrateDataCache() {
        // Data cache entries use keys like "data.cache.<hashCode>"
        // These are transient and will be re-fetched from the server.
        // No migration needed — they'll repopulate naturally.
    }

    private suspend fun migrateSyncStore() {
        val syncKeys = listOf(
            "sync.menu", "sync.permissions", "sync.contexts",
            "sync.hashes", "sync.synced_at", "sync.screen_keys",
        )

        for (key in syncKeys) {
            val value = storage.getStringSafe(key)
            if (value.isNotBlank()) {
                dataStore.putSyncValue(key, value)
            }
        }

        // Migrate individual screen entries
        val screenKeysJson = storage.getStringSafe("sync.screen_keys")
        if (screenKeysJson.isNotBlank()) {
            try {
                val keys = Json.decodeFromString<List<String>>(screenKeysJson)
                for (screenKey in keys) {
                    val screenJson = storage.getStringSafe("sync.screen.$screenKey")
                    if (screenJson.isNotBlank()) {
                        dataStore.putSyncValue("sync.screen.$screenKey", screenJson)
                    }
                }
            } catch (e: Exception) {
                telemetry.crash.recordException(
                    e,
                    mapOf(
                        "operation" to "migrateSyncStore",
                        "module" to "database",
                        "context" to "screen_keys_parse",
                        "edugo.feature" to "db_migration",
                        "error.kind" to "swallowed",
                    ),
                )
                // Ignore malformed screen keys (already logged)
            }
        }
    }

    private suspend fun migrateScreenCache() {
        // Screen cache entries use keys like "screen.cache.<screenKey>"
        // These are transient and will re-populate from the sync bundle or server.
        // No migration needed.
    }

    private suspend fun migrateMutationQueue() {
        val data = storage.getStringSafe("offline.queue.mutations")
        if (data.isBlank()) return

        try {
            val mutations = Json { ignoreUnknownKeys = true }
                .decodeFromString<List<SerializableMutation>>(data)
            for (m in mutations) {
                dataStore.insertMutation(
                    MutationEntry(
                        id = m.id,
                        endpoint = m.endpoint,
                        method = m.method,
                        bodyJson = m.body.toString(),
                        createdAt = m.createdAt,
                        retryCount = m.retryCount,
                        maxRetries = m.maxRetries,
                        status = m.status,
                        entityUpdatedAt = m.entityUpdatedAt,
                    )
                )
            }
        } catch (e: Exception) {
            telemetry.crash.recordException(
                e,
                mapOf(
                    "operation" to "migrateMutationQueue",
                    "module" to "database",
                    "context" to "mutation_queue_parse",
                    "edugo.feature" to "db_migration",
                    "error.kind" to "swallowed",
                ),
            )
            // Ignore malformed mutation data (already logged)
        }
    }
}

@kotlinx.serialization.Serializable
internal data class SerializableMutation(
    val id: String,
    val endpoint: String,
    val method: String,
    val body: kotlinx.serialization.json.JsonObject,
    val createdAt: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val status: String = "PENDING",
    val entityUpdatedAt: String? = null,
)
