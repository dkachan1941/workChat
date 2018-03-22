package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.ChatModelNew
import com.rainmaker.workchat.repository.ChatsRepository

/**
 * Created by dmitry on 3/14/18.
 *
 */

class HomePresenterImpl(private val homeView: HomePresenter.HomeView, private val chatsRepository: ChatsRepository) : HomePresenter, HomePresenter.RepositoryCallbacks {

    override fun onChatsLoaded(chats: MutableList<ChatModelNew?>) {
        if (chats.size == 0){
            homeView.showNoChats()
        } else {
            homeView.displayChats(chats)
        }
    }

    override fun loadChats(newChats: MutableList<String>) {
        chatsRepository.getChats(this@HomePresenterImpl, newChats)
    }

    override fun onUnReadChatsLoaded(unreadChats: MutableList<String>) {
        if (unreadChats.size == 0){
            homeView.showNoChats()
        } else {
            homeView.showUnReadChats(unreadChats)
        }
    }

    override fun loadUnReadChats() {
        chatsRepository.getUnReadChats(this@HomePresenterImpl)
    }


}