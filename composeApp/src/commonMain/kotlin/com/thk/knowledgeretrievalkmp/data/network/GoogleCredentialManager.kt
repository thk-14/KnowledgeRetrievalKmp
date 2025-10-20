package com.thk.knowledgeretrievalkmp.data.network

interface GoogleCredentialManager {
    suspend fun signInWithGoogle(): GoogleCredentialResponse?
    suspend fun logOutFromGoogle()
}

internal expect fun getGoogleCredentialManager(): GoogleCredentialManager

data class GoogleCredentialResponse(
    val displayName: String = "",
    val familyName: String = "",
    val givenName: String = "",
    val id: String = "",
    val idToken: String = "",
    val phoneNumber: String = "",
    val profilePictureUri: String = ""
)