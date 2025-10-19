package com.thk.knowledgeretrievalkmp

import android.app.Application
import android.content.Context
import timber.log.Timber

class KnowledgeRetrievalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppContainer.appContext = this.applicationContext
    }
}

object AppContainer {
    lateinit var appContext: Context
}