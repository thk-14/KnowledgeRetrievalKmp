@file:JvmName("DataStoreAndroid")

package com.thk.knowledgeretrievalkmp.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import com.thk.knowledgeretrievalkmp.AndroidAppContainer
import okio.Path.Companion.toOkioPath

actual fun createDataStoreSettings(fileName: String): SuspendSettings {
    val datastore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { AndroidAppContainer.appContext.preferencesDataStoreFile(fileName).toOkioPath() }
    )
    return DataStoreSettings(datastore)
}