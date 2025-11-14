package com.thk.knowledgeretrievalkmp.data.network

import com.russhwolf.settings.coroutines.SuspendSettings
import com.thk.knowledgeretrievalkmp.security.CipherService
import com.thk.knowledgeretrievalkmp.util.log

class SessionManager(private val dataStore: SuspendSettings) {
    var isDataFetched = false
    private val cipherService = CipherService()

    suspend fun getUserId() = dataStore.getStringOrNull(USER_ID_KEY)
    suspend fun setUserId(value: String) {
        dataStore.putString(USER_ID_KEY, value)
        log("Set user id: $value")
    }


    suspend fun getDisplayName() = dataStore.getStringOrNull(DISPLAY_NAME_KEY)
    suspend fun setDisplayName(value: String) {
        dataStore.putString(DISPLAY_NAME_KEY, value)
        log("Set display name: $value")
    }


    suspend fun getProfileUri() = dataStore.getStringOrNull(PROFILE_URI_KEY)
    suspend fun setProfileUri(value: String) {
        dataStore.putString(PROFILE_URI_KEY, value)
        log("Set profile uri: $value")
    }

    suspend fun getAccessToken(): String? {
        val encryptedValue = dataStore.getStringOrNull(ACCESS_TOKEN_KEY)
        val decryptedValue = encryptedValue?.let { cipherService.decryptString(it) }
        return decryptedValue
    }

    suspend fun setAccessToken(value: String) {
        val encryptedValue = cipherService.encryptString(value)
        dataStore.putString(ACCESS_TOKEN_KEY, encryptedValue)
        log("Set access token: $value")
    }


    suspend fun getRefreshToken(): String? {
        val encryptedValue = dataStore.getStringOrNull(REFRESH_TOKEN_KEY)
        val decryptedValue = encryptedValue?.let { cipherService.decryptString(it) }
        return decryptedValue
    }

    suspend fun setRefreshToken(value: String) {
        val encryptedValue = cipherService.encryptString(value)
        dataStore.putString(REFRESH_TOKEN_KEY, encryptedValue)
        log("Set refresh token: $value")
    }

    suspend fun clearSession() {
        dataStore.clear()
        isDataFetched = false
        log("Clear session")
    }

    companion object {
        private const val USER_ID_KEY = "us_id"
        private const val DISPLAY_NAME_KEY = "d_name"
        private const val PROFILE_URI_KEY = "p_uri"
        private const val ACCESS_TOKEN_KEY = "access_TK"
        private const val REFRESH_TOKEN_KEY = "refresh_TK"
    }
}