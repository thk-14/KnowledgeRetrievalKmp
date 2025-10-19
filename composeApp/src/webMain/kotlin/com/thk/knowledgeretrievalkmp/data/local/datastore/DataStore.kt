package com.thk.knowledgeretrievalkmp.data.local.datastore

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.toSuspendSettings

actual fun createDataStoreSettings(fileName: String): SuspendSettings {
    return StorageSettings().toSuspendSettings()
}