package com.thk.knowledgeretrievalkmp.data.network

import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.thk.knowledgeretrievalkmp.AndroidAppContainer
import timber.log.Timber
import java.security.MessageDigest
import java.util.*

class AndroidGoogleCredentialManager : GoogleCredentialManager {
    val credentialManager: CredentialManager by lazy {
        CredentialManager.create(AndroidAppContainer.appContext)
    }

    override suspend fun signInWithGoogle(
        onSignInFinish: (GoogleCredentialResponse?) -> Unit
    ) {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
        val signInWithGoogleOption: GetSignInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val result = credentialManager.getCredential(
            request = request,
            context = AndroidAppContainer.appContext
        )
        val googleCredentialResponse = handleSignInWithGoogleOption(result)
        onSignInFinish(googleCredentialResponse)
    }

    override suspend fun logOutFromGoogle(
        onLogOutFinish: () -> Unit
    ) {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        onLogOutFinish()
    }

    private fun handleSignInWithGoogleOption(result: GetCredentialResponse): GoogleCredentialResponse? {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val googleCredentialResponse =
                            googleIdTokenCredential.toGoogleCredentialResponse()
                        Timber.d("Google ID token: $googleCredentialResponse")
                        return googleCredentialResponse
                    } catch (exception: GoogleIdTokenParsingException) {
                        Timber.e("Google ID token parsing failed: ${exception.message}")
                        return null
                    }
                } else {
                    Timber.e("Unexpected type of credential")
                    return null
                }
            }

            else -> {
                Timber.e("Unexpected type of credential")
                return null
            }
        }
    }

    companion object {
        private const val WEB_CLIENT_ID = "725888804337-h9fgbvec56iqs2etcais2mkr6kcvb3pq.apps.googleusercontent.com"
    }
}

fun GoogleIdTokenCredential.toGoogleCredentialResponse() = GoogleCredentialResponse(
    displayName = this.displayName ?: "",
    familyName = this.familyName ?: "",
    givenName = this.givenName ?: "",
    id = this.id,
    idToken = this.idToken,
    profilePictureUri = this.profilePictureUri.toString()
)

actual fun getGoogleCredentialManager(): GoogleCredentialManager {
    return AndroidGoogleCredentialManager()
}