package com.rainmaker.workchat.activities

import android.util.Log
import com.rainmaker.workchat.modifyEncryptionKey
import com.rainmaker.workchat.presenters.EncryptionPresenterImpl
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

/**
 * Created by dmitry on 3/10/18.
 */

class EncryptionTest {

    @Mock
    val log = null

    @Before
    fun setUp(){

    }

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