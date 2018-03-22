package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.modifyEncryptionKey
import junit.framework.Assert
import org.junit.Test

/**
 * Created by dmitry on 3/10/18.
 *
 */

class EncryptionTest {

    @Test
    fun encryptionTest() {

        val encryptionPresenter = EncryptionPresenterImpl()
        val originalMsg = "My original string"

        val key = modifyEncryptionKey("mykey").toByteArray()
        encryptionPresenter.setKey(key)
        val encrypted = encryptionPresenter.encrypt(originalMsg)
        val decrypted = encryptionPresenter.decrypt(encrypted)

        Assert.assertEquals(key, encryptionPresenter.getRawKey())
        Assert.assertEquals(originalMsg, decrypted)

    }

}