package com.thk.knowledgeretrievalkmp.data.local.db

import app.cash.sqldelight.ColumnAdapter
import com.thk.knowledgeretrievalkmp.data.network.NetworkDocumentStatus
import com.thk.knowledgeretrievalkmp.data.network.NetworkMessageRole

object DocumentStatusStringAdapter : ColumnAdapter<NetworkDocumentStatus, String> {
    override fun decode(databaseValue: String): NetworkDocumentStatus {
        return NetworkDocumentStatus.valueOf(databaseValue)
    }


    override fun encode(value: NetworkDocumentStatus): String {
        return value.name
    }
}

object MessageRoleStringAdapter : ColumnAdapter<NetworkMessageRole, String> {
    override fun decode(databaseValue: String): NetworkMessageRole {
        return NetworkMessageRole.valueOf(databaseValue)
    }

    override fun encode(value: NetworkMessageRole): String {
        return value.name
    }
}