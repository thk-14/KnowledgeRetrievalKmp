package com.thk.knowledgeretrievalkmp.util

external fun consoleLog(msg: String)

actual fun log(msg: String) {
    consoleLog(msg)
}