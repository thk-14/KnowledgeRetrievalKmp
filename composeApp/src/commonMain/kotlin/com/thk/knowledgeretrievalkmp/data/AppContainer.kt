package com.thk.knowledgeretrievalkmp.data

import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.thk.knowledgeretrievalkmp.data.local.datastore.createDataStoreSettings
import com.thk.knowledgeretrievalkmp.data.network.NetworkApiService
import com.thk.knowledgeretrievalkmp.data.network.SessionManager
import com.thk.knowledgeretrievalkmp.db.KbDatabase

object AppContainer {
    val apiService: NetworkApiService by lazy { NetworkApiService() }
    val sessionManager by lazy { SessionManager(dataStore) }
    lateinit var db: KbDatabase
    val googleAuthProvider by lazy {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = WEB_CLIENT_ID
            )
        )
    }
    private val dataStore by lazy { createDataStoreSettings() }

    private const val WEB_CLIENT_ID = "725888804337-h9fgbvec56iqs2etcais2mkr6kcvb3pq.apps.googleusercontent.com"
}