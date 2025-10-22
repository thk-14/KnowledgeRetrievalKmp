package com.thk.knowledgeretrievalkmp.data.network

import com.russhwolf.settings.coroutines.SuspendSettings
import com.thk.knowledgeretrievalkmp.util.log

class SessionManager(private val dataStore: SuspendSettings) {
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

    suspend fun getAccessToken() = dataStore.getStringOrNull(ACCESS_TOKEN_KEY)
    suspend fun setAccessToken(value: String) {
        dataStore.putString(ACCESS_TOKEN_KEY, value)
        log("Set access token: $value")
    }


    suspend fun getRefreshToken() = dataStore.getStringOrNull(REFRESH_TOKEN_KEY)
    suspend fun setRefreshToken(value: String) {
        dataStore.putString(REFRESH_TOKEN_KEY, value)
        log("Set refresh token: $value")
    }

    suspend fun clearSession() {
        dataStore.clear()
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