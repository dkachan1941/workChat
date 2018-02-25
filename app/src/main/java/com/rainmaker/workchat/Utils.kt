package com.rainmaker.workchat

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Created by dmitry on 2/24/18.
 *
 */


fun generateKey(password: String): SecretKeySpec {
    return SecretKeySpec(password.toByteArray(charset("UTF-8")), "AES")
}

fun encryptMsg(message: String, secret: SecretKey): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val iv = "fedcba9876543210"
    val ivspec = IvParameterSpec(iv.toByteArray(charset("UTF-8")))
    cipher?.init(Cipher.ENCRYPT_MODE, secret, ivspec)
    return cipher.doFinal(message.toByteArray(charset("UTF-8")))
}

fun decryptMsg(cipherText: ByteArray, secret: SecretKey): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    val iv = "fedcba9876543210"
    val ivspec = IvParameterSpec(iv.toByteArray(charset("UTF-8")))
    cipher?.init(Cipher.DECRYPT_MODE, secret, ivspec)
    return String(cipher.doFinal(cipherText), charset("UTF-8"))
}