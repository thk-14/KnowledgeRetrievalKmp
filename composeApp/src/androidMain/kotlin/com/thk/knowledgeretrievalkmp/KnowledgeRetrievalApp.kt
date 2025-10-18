package com.thk.knowledgeretrievalkmp

import android.app.Application
import timber.log.Timber

class KnowledgeRetrievalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}