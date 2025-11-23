package com.thk.knowledgeretrievalkmp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.thk.knowledgeretrievalkmp.data.DefaultKnowledgeRetrievalRepository
import com.thk.knowledgeretrievalkmp.util.log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        FileKit.manualFileKitCoreInitialization(this)
        FileKit.init(this)

        AndroidAppContainer.activityContext = WeakReference(this)

        setContent {
            App(
                exchangeCode = getExchangeCode(intent)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AndroidAppContainer.activityContext?.get() == this) {
            AndroidAppContainer.activityContext = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        log("onNewIntent intent: $intent")

        lifecycleScope.launch {
            val code = getExchangeCode(intent)
            DefaultKnowledgeRetrievalRepository.exchangeGoogleAuthCode(code)
        }
    }

    private fun getExchangeCode(intent: Intent): String? {
        val data = intent.data
        if (data != null && data.scheme == "kms" && data.path == "/callback") {
            return data.getQueryParameter("code")
        }
        return null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}