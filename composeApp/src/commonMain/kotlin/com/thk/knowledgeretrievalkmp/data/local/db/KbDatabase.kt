package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.db.SqlDriver
import com.thk.knowledgeretrievalkmp.db.*

fun createDatabase(driver: SqlDriver) = KbDatabase(
    driver = driver,
    DocumentAdapter = Document.Adapter(
        StatusAdapter = DocumentStatusStringAdapter
    ),
    MessageAdapter = Message.Adapter(
        RoleAdapter = MessageRoleStringAdapter
    )
)

data class ConversationWithMessages(
    val conversation: Conversation,
    val messages: List<Message>
)

data class KbWithDocuments(
    val kb: KnowledgeBase,
    val documents: List<Document>
)