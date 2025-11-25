package com.thk.knowledgeretrievalkmp.security

import korlibs.crypto.AES
import korlibs.crypto.Padding

interface CipherService {
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray
    fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray
}

class AesCbcCipher : CipherService {
    private val iv = "1e8c2d744283a01b".encodeToByteArray()

    override fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        return AES.encryptAesCbc(data, key, iv, Padding.PKCS7Padding)
    }

    override fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {
        return AES.decryptAesCbc(encryptedData, key, iv, Padding.PKCS7Padding)
    }
}