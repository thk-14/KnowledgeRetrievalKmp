package com.thk.knowledgeretrievalkmp.data.network

interface GoogleCredentialManager {
    suspend fun signInWithGoogle(
        onSignInFinish: (GoogleCredentialResponse?) -> Unit
    )

    suspend fun logOutFromGoogle(
        onLogOutFinish: () -> Unit
    )
}

internal expect fun getGoogleCredentialManager(): GoogleCredentialManager

data class GoogleCredentialResponse(
    val displayName: String = "",
    val familyName: String = "",
    val givenName: String = "",
    val id: String = "",
    val idToken: String = "",
    val profilePictureUri: String = ""
)