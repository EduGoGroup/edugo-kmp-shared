package com.edugo.kmp.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(databaseName: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
        NativeSqliteDriver(schema, databaseName)
}
