package com.thk.knowledgeretrievalkmp.data.network

import com.russhwolf.settings.coroutines.SuspendSettings
import com.thk.knowledgeretrievalkmp.security.AesCbcCipher
import com.thk.knowledgeretrievalkmp.security.CipherService
import com.thk.knowledgeretrievalkmp.util.log
import korlibs.crypto.encoding.fromBase64
import korlibs.crypto.encoding.toBase64

class SessionManager(private val dataStore: SuspendSettings) {
    var isDataFetched = false
    private val cipherService: CipherService = AesCbcCipher()

    suspend fun getUserId() = getValue(USER_ID_KEY)
    suspend fun setUserId(value: String) = setValue(USER_ID_KEY, value)


    suspend fun getDisplayName() = getValue(DISPLAY_NAME_KEY)
    suspend fun setDisplayName(value: String) = setValue(DISPLAY_NAME_KEY, value)


    suspend fun getProfileUri() = getValue(PROFILE_URI_KEY)
    suspend fun setProfileUri(value: String) = setValue(PROFILE_URI_KEY, value)

    suspend fun getAccessToken(): String? = getValue(ACCESS_TOKEN_KEY)

    suspend fun setAccessToken(value: String) = setValue(ACCESS_TOKEN_KEY, value)


    suspend fun getRefreshToken(): String? = getValue(REFRESH_TOKEN_KEY)

    suspend fun setRefreshToken(value: String) = setValue(REFRESH_TOKEN_KEY, value)

    suspend fun getBaseUrl(): String? = getValue(BASE_URL_KEY)

    suspend fun setBaseUrl(value: String) = setValue(BASE_URL_KEY, value)

    suspend fun clearSession() {
        dataStore.clear()
        isDataFetched = false
        log("Clear session")
    }

    private fun encryptData(data: String): String {
        return cipherService.encrypt(
            data.encodeToByteArray(),
            KEY.encodeToByteArray()
        ).toBase64()
    }

    private fun decryptData(encryptedData: String): String {
        return cipherService.decrypt(
            encryptedData.fromBase64(),
            KEY.encodeToByteArray()
        ).decodeToString()
    }

    private suspend fun getValue(key: String): String? {
        val encryptedValue = dataStore.getStringOrNull(key) ?: return null
        val decryptedValue = decryptData(encryptedValue)
        return decryptedValue
    }

    private suspend fun setValue(key: String, value: String) {
        val encryptedValue = encryptData(value)
        dataStore.putString(key, encryptedValue)
    }

    companion object {
        private const val USER_ID_KEY = "us_id"
        private const val DISPLAY_NAME_KEY = "d_name"
        private const val PROFILE_URI_KEY = "p_uri"
        private const val ACCESS_TOKEN_KEY = "access_TK"
        private const val REFRESH_TOKEN_KEY = "refresh_TK"
        private const val BASE_URL_KEY = "base_URL"
        private const val KEY = "1f2250d22a4071e5b3bf566708fb85e6"
    }
}