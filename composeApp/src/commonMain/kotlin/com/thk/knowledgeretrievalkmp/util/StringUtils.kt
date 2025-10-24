package com.thk.knowledgeretrievalkmp.util

import com.thk.knowledgeretrievalkmp.data.network.SseEvent

fun String.isValidEmail() =
    Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        .matches(this)

fun String.ensureMinLength(minLength: Int) = length >= minLength

fun String.hasDigit() = this.any { it.isDigit() }

fun String.hasCapitalLetter() = this.any { it.isUpperCase() }

fun String.hasSmallLetter() = this.any { it.isLowerCase() }

fun String.hasWhiteSpace() = this.any { it.isWhitespace() }

fun String.hasSpecialChar() = this.any {
    it in "!@#$%^&*-+_="
}

fun checkValidPasswordError(password: String): String? {
    return when {
        !password.ensureMinLength(8) -> "Password must contain at least 8 characters"
        !password.hasSmallLetter() -> "Password must contain at least one small letter"
        !password.hasCapitalLetter() -> "Password must contain at least one capital letter"
        !password.hasDigit() -> "Password must contain at least one digit"
        !password.hasSpecialChar() -> "Password must contain at least one special character"
        password.hasWhiteSpace() -> "Password must not contain white space"
        else -> null
    }
}

fun String.toSseEvent(): SseEvent? {
    SseEvent.entries.forEach { sseEvent ->
        if (this == sseEvent.value) {
            return sseEvent
        }
    }
    return null
}