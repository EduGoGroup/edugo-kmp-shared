package com.edugo.kmp.database

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(databaseName: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
        AndroidSqliteDriver(schema, context, databaseName)
}
