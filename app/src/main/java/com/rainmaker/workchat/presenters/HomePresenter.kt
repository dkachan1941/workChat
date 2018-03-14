package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.ChatModelNew

/**
 * Created by dmitry on 3/14/18.
 *
 */

interface HomePresenter {

    interface HomeView {
        fun showNoChats()
        fun showUnReadChats(newChats: MutableList<String>)
        fun displayChats(chats: MutableList<ChatModelNew?>)
    }

    interface RepositoryCallbacks{
        fun onUnReadChatsLoaded(unreadChats: MutableList<String>)
        fun onChatsLoaded(chats: MutableList<ChatModelNew?>)
    }

    fun loadUnReadChats()
    fun loadChats(newChats: MutableList<String>)

}