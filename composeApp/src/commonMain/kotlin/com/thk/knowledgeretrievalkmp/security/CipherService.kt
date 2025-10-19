package com.thk.knowledgeretrievalkmp.security

import korlibs.crypto.AES
import korlibs.crypto.Padding
import korlibs.crypto.encoding.fromBase64
import korlibs.crypto.encoding.toBase64

class CipherService {
    private val key = "1f2250d22a4071e5b3bf566708fb85e6".encodeToByteArray()
    private val iv = "1e8c2d744283a01b".encodeToByteArray()

    fun encryptString(data: String): String {
        val encryptedData = AES.encryptAesCbc(
            data.encodeToByteArray(),
            key,
            iv,
            Padding.PKCS7Padding
        )
        return encryptedData.toBase64()
    }

    fun decryptString(encryptedData: String): String {
        val decryptedData = AES.decryptAesCbc(
            encryptedData.fromBase64(),
            key,
            iv,
            Padding.PKCS7Padding
        )
        return decryptedData.decodeToString()
    }
}