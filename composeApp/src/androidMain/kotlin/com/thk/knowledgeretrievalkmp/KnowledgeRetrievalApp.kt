package com.thk.knowledgeretrievalkmp

import android.app.Application
import android.content.Context
import com.thk.knowledgeretrievalkmp.data.AppContainer
import timber.log.Timber

class KnowledgeRetrievalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AndroidAppContainer.appContext = this.applicationContext
        AppContainer.googleAuthProvider
    }
}

object AndroidAppContainer {
    lateinit var appContext: Context
}