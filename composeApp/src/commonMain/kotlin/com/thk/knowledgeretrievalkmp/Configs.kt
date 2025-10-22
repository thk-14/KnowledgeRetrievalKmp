package com.thk.knowledgeretrievalkmp

class Configs {
    companion object {
        const val MAX_UPLOAD_FILE_SIZE: Long = 50_000_000
        const val MAX_DOCUMENTS_PER_KB: Int = 100
        const val MAX_KB_PER_USER: Int = 100

        const val fetchMessagesFromServer: Boolean = true
    }
}