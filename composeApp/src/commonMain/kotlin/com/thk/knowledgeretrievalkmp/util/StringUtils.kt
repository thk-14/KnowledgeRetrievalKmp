package com.thk.knowledgeretrievalkmp.util

import com.thk.knowledgeretrievalkmp.data.network.SseEvent
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.uuid.Uuid

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

fun String.titlecase() = this.replaceFirstChar { it.uppercase() }

fun String.toSseEvent(): SseEvent? {
    SseEvent.entries.forEach { sseEvent ->
        if (this == sseEvent.value) {
            return sseEvent
        }
    }
    return null
}

fun Uuid.Companion.generateV7(): Uuid {
    val timestamp = Clock.System.now().toEpochMilliseconds()
    // Get 16 random bytes
    val randomBytes = Random.Default.nextBytes(16)
    // 48 bits for timestamp
    randomBytes[0] = (timestamp shr 40).toByte()
    randomBytes[1] = (timestamp shr 32).toByte()
    randomBytes[2] = (timestamp shr 24).toByte()
    randomBytes[3] = (timestamp shr 16).toByte()
    randomBytes[4] = (timestamp shr 8).toByte()
    randomBytes[5] = timestamp.toByte()
    // 4 bits for version (0111)
    randomBytes[6] = (randomBytes[6].toInt() and 0x0F or 0x70).toByte()
    // 2 bits for variant (10)
    randomBytes[8] = (randomBytes[8].toInt() and 0x3F or 0x80).toByte()
    return Uuid.fromByteArray(randomBytes)
}