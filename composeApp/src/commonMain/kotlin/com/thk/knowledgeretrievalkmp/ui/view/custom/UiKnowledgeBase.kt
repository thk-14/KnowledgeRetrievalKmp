package com.thk.knowledgeretrievalkmp.ui.view.custom

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.thk.knowledgeretrievalkmp.data.local.db.ConversationWithMessages
import com.thk.knowledgeretrievalkmp.data.local.db.KbWithDocumentsAndConversation
import com.thk.knowledgeretrievalkmp.db.Conversation
import com.thk.knowledgeretrievalkmp.db.Document
import com.thk.knowledgeretrievalkmp.db.KnowledgeBase
import com.thk.knowledgeretrievalkmp.db.Message

@Stable
data class UiConversation(
    val conversation: MutableState<Conversation> = mutableStateOf(
        Conversation(
            ConversationId = "",
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
            DocumentCount = 0,
            ConversationId = null
        )
    ),
    val documents: SnapshotStateList<Document> = mutableStateListOf(),
    val conversation: MutableState<UiConversation> = mutableStateOf(UiConversation()),
)

fun ConversationWithMessages.toUiConversation() = UiConversation(
    conversation = mutableStateOf(this.conversation),
    messages = this.messages.toMutableStateList()
)

fun KbWithDocumentsAndConversation.toUiKnowledgeBase() = UiKnowledgeBase(
    kb = mutableStateOf(this.kb),
    documents = this.documents.toMutableStateList(),
    conversation = this.conversation?.toUiConversation()?.let {
        mutableStateOf(it)
    } ?: mutableStateOf(UiConversation())
)

enum class FileExtension(val extension: String) {
    PDF("pdf"),
    DOCX("docx"),
    TXT("txt")
}