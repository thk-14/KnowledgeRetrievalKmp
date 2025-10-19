package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.db.SqlDriver
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KbDatabase
import com.thk.knowledgeretrievalkmp.db.Message

fun createDatabase(driver: SqlDriver) = KbDatabase(
    driver = driver,
    DocumentAdapter = Document.Adapter(
        StatusAdapter = DocumentStatusStringAdapter
    ),
    MessageAdapter = Message.Adapter(
        RoleAdapter = MessageRoleStringAdapter
    )
)