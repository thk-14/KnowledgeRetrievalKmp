package com.thk.knowledgeretrievalkmp.data.network


class WebGoogleCredentialManager : GoogleCredentialManager {
    var initialized = false
    override suspend fun signInWithGoogle(
        onSignInFinish: (GoogleCredentialResponse?) -> Unit
    ) {
        if (!initialized) {
            initializeGoogleSignIn(WEB_CLIENT_ID)
            initialized = true
        }
        setGoogleSignInFinishCallback { googleCredentialPayload ->
            onSignInFinish(googleCredentialPayload?.toGoogleCredentialResponse())
        }
        triggerGoogleSignIn()
    }

    override suspend fun logOutFromGoogle(
        onLogOutFinish: () -> Unit
    ) {
        setGoogleSignOutFinishCallback(onLogOutFinish)
        triggerGoogleSignOut()
    }

    private companion object {
        private const val WEB_CLIENT_ID = "725888804337-h9fgbvec56iqs2etcais2mkr6kcvb3pq.apps.googleusercontent.com"
    }
}

actual fun getGoogleCredentialManager(): GoogleCredentialManager {
    return WebGoogleCredentialManager()
}

external fun setGoogleSignInFinishCallback(callback: (GoogleCredentialPayload?) -> Unit)
external fun setGoogleSignOutFinishCallback(callback: () -> Unit)
external fun initializeGoogleSignIn(clientId: String)
external fun triggerGoogleSignIn()
external fun triggerGoogleSignOut()

external interface GoogleCredentialPayload {
    val email: String?
    val emailVerified: Boolean?
    val familyName: String?
    val givenName: String?
    val name: String?
    val picture: String?
    val token: String?
}

fun GoogleCredentialPayload.toGoogleCredentialResponse() = GoogleCredentialResponse(
    displayName = this.name ?: "",
    familyName = this.familyName ?: "",
    givenName = this.givenName ?: "",
    id = this.email ?: "",
    idToken = this.token ?: "",
    profilePictureUri = this.picture ?: ""
)