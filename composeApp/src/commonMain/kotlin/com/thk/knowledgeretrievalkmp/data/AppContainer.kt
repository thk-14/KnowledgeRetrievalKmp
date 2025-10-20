package com.thk.knowledgeretrievalkmp.data

import com.thk.knowledgeretrievalkmp.data.local.datastore.createDataStoreSettings
import com.thk.knowledgeretrievalkmp.data.network.NetworkApiService
import com.thk.knowledgeretrievalkmp.db.KbDatabase

object AppContainer {
    val apiService: NetworkApiService by lazy { NetworkApiService() }
    val dataStore by lazy { createDataStoreSettings() }
    lateinit var db: KbDatabase
}