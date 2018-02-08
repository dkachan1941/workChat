package com.rainmaker.workchat.presenters

/**
 * Created by dmitry on 2/8/18.
 */
interface FirebasePresenter {
    fun createChat(text: String)

    fun pushUserToDb(uuid: String?, email: String?, displayName: String?, providerId: String?)
}