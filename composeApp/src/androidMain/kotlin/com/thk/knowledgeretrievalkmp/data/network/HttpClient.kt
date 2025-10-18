@file:JvmName("HttpClientAndroid")

package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.android.*

internal actual val httpClientEngine: HttpClientEngineFactory<*> = Android