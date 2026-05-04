package com.edugo.kmp.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(databaseName: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
        val home = System.getProperty("user.home")
        val dbDir = File(home, ".edugo")
        dbDir.mkdirs()
        val dbFile = File(dbDir, databaseName)
        val isNew = !dbFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        if (isNew) schema.create(driver)
        return driver
    }
}
