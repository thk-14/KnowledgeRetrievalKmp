package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

internal actual val httpClientEngine: HttpClientEngineFactory<*> = Js