package com.thk.knowledgeretrievalkmp.data.network

class DesktopGoogleCredentialManager : GoogleCredentialManager {
    override suspend fun signInWithGoogle(): GoogleCredentialResponse? {
        TODO("Not yet implemented")
    }

    override suspend fun logOutFromGoogle() {
        TODO("Not yet implemented")
    }

}

actual fun getGoogleCredentialManager(): GoogleCredentialManager {
    return DesktopGoogleCredentialManager()
}