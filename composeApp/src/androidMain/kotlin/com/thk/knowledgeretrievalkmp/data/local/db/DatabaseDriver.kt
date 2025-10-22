package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.thk.knowledgeretrievalkmp.AndroidAppContainer
import com.thk.knowledgeretrievalkmp.db.KbDatabase

actual suspend fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
        schema = KbDatabase.Schema.synchronous(),
        context = AndroidAppContainer.appContext,
        name = "KbDatabase.db"
    )
}