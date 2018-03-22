package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.ChatModelNew

/**
 * Created by dmitry on 3/15/18.
 *
 */

interface ChatsPresenter {

    fun loadPrivateChats()

    fun loadPublicChats()

    interface chatsView{
        fun displayChats(chats: MutableList<ChatModelNew>)
        fun displayNoChats()
    }

    interface RepositoryCallbacks {
        fun onChatsLoaded(chats: MutableList<ChatModelNew>)
//        fun onPublicChatsLoaded(chats: MutableList<ChatModelNew>)
        fun onNewMessagesCountForChatLoaded(messagesCountForChats: HashMap<String?, String?>)
    }

    fun loadNewMessagesCountForChats()

}