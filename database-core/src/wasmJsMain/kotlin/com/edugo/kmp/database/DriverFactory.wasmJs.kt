package com.edugo.kmp.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

actual class DriverFactory {
    actual fun createDriver(databaseName: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
        error("DriverFactory is not supported on wasmJs. Use EduGoDatabaseFactory.inMemory() for the web target.")
}
