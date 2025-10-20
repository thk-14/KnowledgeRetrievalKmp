package com.thk.knowledgeretrievalkmp.data.network


class WebGoogleCredentialManager : GoogleCredentialManager {
    override suspend fun signInWithGoogle(): GoogleCredentialResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun logOutFromGoogle() {
        TODO("Not yet implemented")
    }

    private companion object {
        private const val WEB_CLIENT_ID = "725888804337-h9fgbvec56iqs2etcais2mkr6kcvb3pq.apps.googleusercontent.com"
    }
}

actual fun getGoogleCredentialManager(): GoogleCredentialManager {
    return WebGoogleCredentialManager()
}