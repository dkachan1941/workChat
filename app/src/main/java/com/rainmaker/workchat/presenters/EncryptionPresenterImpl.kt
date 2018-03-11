package com.rainmaker.workchat.presenters

import android.annotation.SuppressLint
import android.util.Log
import com.rainmaker.workchat.and
import com.rainmaker.workchat.shr
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Created by dmitry on 3/10/18.
 *
 */

class EncryptionPresenterImpl: EncryptionPresenter {

    private var key: ByteArray? = null
    private val hex = "0123456789ABCDEF"

    override fun encrypt(text: String): String {
        return if (getRawKey() == null) text else toHex(encrypt(getRawKey()!!, text.toByteArray()))
    }

    override fun decrypt(encryptedText: String): String {
        if (getRawKey() == null || getRawKey()!!.isEmpty()) return encryptedText
        var res = ""
        try {
            res = String(decrypt(toByte(encryptedText)))
        } catch (e: Exception){
            e.printStackTrace()
            //  the message can not be decrypted
        }
        return res
    }

    override fun setKey(key: ByteArray?) {
        this.key = key
    }

    override fun getRawKey(): ByteArray? {
        return key
    }

    @SuppressLint("GetInstance")
    private fun encrypt(raw: ByteArray, clear: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        return cipher.doFinal(clear)
    }

    private fun toHex(buf: ByteArray?): String {
        if (buf == null)
            return ""
        val result = StringBuffer(2 * buf.size)
        for (i in buf.indices) {
            appendHex(result, buf[i], hex)
        }
        return result.toString()
    }

    private fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16)!!.toByte()
        return result
    }

    @SuppressLint("GetInstance")
    private fun decrypt(encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        val skeySpec = SecretKeySpec(getRawKey(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }

    private fun appendHex(sb: StringBuffer, b: Byte, hex: String) {
        sb.append(hex[b shr 4 and 0x0f]).append(hex[b and 0x0f])
        Log.d("appendHexNew: ", sb.toString())
    }

}