@file:JvmName("HttpClientDesktop")

package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual val httpClientEngine: HttpClientEngineFactory<*> = CIO