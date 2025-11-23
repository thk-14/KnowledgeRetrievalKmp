package com.thk.knowledgeretrievalkmp.data.network

import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.util.log
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import java.awt.Desktop
import java.net.URI

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

actual suspend fun initiateLogin() {
    val port = 5789
    val redirectUri = "http://localhost:$port/callback"
    val serverUrl = "https://smart-kind-macaque.ngrok-free.app"
    val loginUrl = "$serverUrl/auth/google/login?redirect_uri=$redirectUri"
    val deferredCode = CompletableDeferred<String>()

    val server = embeddedServer(Netty, port = port) {
        routing {
            get("/callback") {
                val code = call.request.queryParameters["code"]
                if (code != null) {
                    call.respondText("Login successful! You can close this window.")
                    deferredCode.complete(code)
                } else {
                    call.respondText("Login failed.")
                    deferredCode.completeExceptionally(Exception("No code"))
                }
            }
        }
    }.start(wait = false)

    try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(loginUrl))
        }
        val code = deferredCode.await()
        DefaultKnowledgeRetrievalRepository.exchangeGoogleAuthCode(code)
    } catch (exception: Exception) {
        log("initiateLogin exception: $exception")
        DefaultKnowledgeRetrievalRepository.exchangeGoogleAuthCode(null)
    } finally {
        server.stop(1000, 2000)
    }
}