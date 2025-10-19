@file:JvmName("DataStoreDesktop")

package com.thk.knowledgeretrievalkmp.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import okio.Path.Companion.toOkioPath
import java.io.File

actual fun createDataStoreSettings(fileName: String): SuspendSettings {
    val settingsFile = File(System.getProperty("java.io.tmpdir"), "$fileName.preferences_pb")
    val datastore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { settingsFile.toOkioPath() }
    )
    return DataStoreSettings(datastore)
}