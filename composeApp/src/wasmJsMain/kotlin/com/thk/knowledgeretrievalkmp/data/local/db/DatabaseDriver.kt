package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.thk.knowledgeretrievalkmp.db.KbDatabase
import org.w3c.dom.Worker

actual suspend fun createDriver(): SqlDriver {
    val driver = WebWorkerDriver(
        Worker(
            js("new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url)")
        )
    )
    KbDatabase.Schema.create(driver).await()
    return driver
}