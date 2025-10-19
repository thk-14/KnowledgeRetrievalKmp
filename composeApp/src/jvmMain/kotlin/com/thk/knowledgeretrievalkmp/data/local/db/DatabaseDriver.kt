package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.thk.knowledgeretrievalkmp.db.KbDatabase
import java.io.File

actual suspend fun createDriver(): SqlDriver {
    val userHome = System.getProperty("user.home")
    val dbFile = File(userHome, "KbDatabase.db")
    val newDb = !dbFile.exists()

    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (newDb) {
        KbDatabase.Schema.create(driver).await()
    }
    return driver
}