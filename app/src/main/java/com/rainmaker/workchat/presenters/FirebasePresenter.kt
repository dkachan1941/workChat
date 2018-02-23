package com.rainmaker.workchat.presenters

import com.google.firebase.database.DatabaseReference
import com.rainmaker.workchat.MessageModel
import com.rainmaker.workchat.User

/**
 * Created by dmitry on 2/8/18.
 */
interface FirebasePresenter {

    interface FirebasePresenterListener{
        fun onChatUsersLoaded(users: List<User?>?)
    }

    fun createChat(text: String)
    fun pushUserToDb(uuid: String?, email: String?, displayName: String?, providerId: String?)
    fun sendMessage(mFirebaseDatabaseReference: DatabaseReference?, newMessage: MessageModel, chatId: String, isUpdate: Boolean, key: String?)
    fun requestUsersForChat(chatId: String?, listener: FirebasePresenterListener?)
    fun requestAllUsers(listener: FirebasePresenterListener?)
    fun resetNewMessagesCount(uuid: String?, chatId: String)
}