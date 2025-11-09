package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.thk.knowledgeretrievalkmp.data.local.db.ConversationWithMessages
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocuments
import com.thk.knowledgeretrievalkmp.db.Conversation
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.db.Message

@Stable
data class UiConversation(
    val conversation: MutableState<Conversation> = mutableStateOf(
        Conversation(
            ConversationId = "",
            UserId = "",
            Name = "",
            IsActive = false
        )
    ),
    val messages: SnapshotStateList<Message> = mutableStateListOf()
)

@Stable
data class UiKnowledgeBase(
    val kb: MutableState<KnowledgeBase> = mutableStateOf(
        KnowledgeBase(
            KbId = "",
            UserId = "",
            Name = "",
            Description = "",
            CreatedAt = "",
            UpdatedAt = "",
            IsActive = false,
            DocumentCount = 0
        )
    ),
    val documents: SnapshotStateList<Document> = mutableStateListOf()
)

fun ConversationWithMessages.toUiConversation() = UiConversation(
    conversation = mutableStateOf(this.conversation),
    messages = this.messages.toMutableStateList()
)

fun KbWithDocuments.toUiKnowledgeBase() = UiKnowledgeBase(
    kb = mutableStateOf(this.kb),
    documents = this.documents.toMutableStateList()
)

enum class FileExtension(val extension: String) {
    PDF("pdf"),
    DOCX("docx"),
    TXT("txt")
}