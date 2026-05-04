package com.edugo.kmp.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

expect class DriverFactory {
    fun createDriver(databaseName: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver
}
