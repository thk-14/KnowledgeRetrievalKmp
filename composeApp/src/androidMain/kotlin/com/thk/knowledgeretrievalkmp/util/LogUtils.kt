package com.thk.knowledgeretrievalkmp.util

import timber.log.Timber

actual fun log(msg: String) {
    Timber.d(msg)
}