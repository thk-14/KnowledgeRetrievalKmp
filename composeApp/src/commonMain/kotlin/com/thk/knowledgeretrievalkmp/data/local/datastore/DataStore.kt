package com.thk.knowledgeretrievalkmp.data.local.datastore

import com.russhwolf.settings.coroutines.SuspendSettings

const val DATA_STORE_FILE_NAME = "knowledge_retrieval_ds"
expect fun createDataStoreSettings(fileName: String = DATA_STORE_FILE_NAME): SuspendSettings

val dataStore by lazy { createDataStoreSettings() }