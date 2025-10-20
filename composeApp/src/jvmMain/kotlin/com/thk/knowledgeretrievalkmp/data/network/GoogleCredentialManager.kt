package com.thk.knowledgeretrievalkmp.data.network

class DesktopGoogleCredentialManager : GoogleCredentialManager {
    override suspend fun signInWithGoogle(onSignInFinish: (GoogleCredentialResponse?) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun logOutFromGoogle(onLogOutFinish: () -> Unit) {
        TODO("Not yet implemented")
    }

}

actual fun getGoogleCredentialManager(): GoogleCredentialManager {
    return DesktopGoogleCredentialManager()
}