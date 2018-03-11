package com.rainmaker.workchat.presenters

/**
 * Created by dmitry on 3/10/18.
 *
 */
interface EncryptionPresenter {

    fun encrypt(text: String): String
    fun decrypt(encryptedText: String): String
    fun setKey(key: ByteArray?)
    fun getRawKey(): ByteArray?
}