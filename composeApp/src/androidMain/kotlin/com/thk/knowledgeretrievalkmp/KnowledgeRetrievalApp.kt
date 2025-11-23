package com.thk.knowledgeretrievalkmp

import android.app.Activity
import android.app.Application
import android.content.Context
import com.thk.knowledgeretrievalkmp.data.AppContainer
import timber.log.Timber
import java.lang.ref.WeakReference

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
    var activityContext: WeakReference<Activity>? = null
}