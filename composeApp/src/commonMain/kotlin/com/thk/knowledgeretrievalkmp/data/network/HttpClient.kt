package com.thk.knowledgeretrievalkmp.data.network

import io.ktor.client.engine.*

internal expect fun getHttpClientEngine(): HttpClientEngineFactory<*>

