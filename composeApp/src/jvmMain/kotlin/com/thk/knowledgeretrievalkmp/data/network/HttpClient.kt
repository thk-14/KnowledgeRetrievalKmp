@file:JvmName("HttpClientDesktop")

package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

internal actual fun getHttpClientEngine(): HttpClientEngineFactory<*> = CIO