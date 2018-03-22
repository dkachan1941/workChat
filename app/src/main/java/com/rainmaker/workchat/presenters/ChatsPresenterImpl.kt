package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.ChatModelNew
import com.rainmaker.workchat.repository.ChatsRepository

/**
 * Created by dmitry on 3/15/18.
 *
 */

class ChatsPresenterImpl(private val chatsView: ChatsPresenter.chatsView, private val chatsRepository: ChatsRepository) : ChatsPresenter, ChatsPresenter.RepositoryCallbacks {

    var chats: MutableList<ChatModelNew>? = null

    override fun onNewMessagesCountForChatLoaded(messagesCountForChats: HashMap<String?, String?>) {
        if (chats != null && !chats!!.isEmpty() && !messagesCountForChats.isEmpty()){
            chats?.forEachIndexed { index, chatModel ->
                if (messagesCountForChats.containsKey(chatModel.key?: "")){
                    chats!![index].messageCount = messagesCountForChats[chatModel.key]?.toInt()
                }
            }
            chatsView.displayChats(chats!!)
        }
    }

    override fun loadNewMessagesCountForChats() {
        chatsRepository.getNewMessagesCountForChats(this@ChatsPresenterImpl)
    }

    override fun onChatsLoaded(chats: MutableList<ChatModelNew>) {
        if (chats.isEmpty()){
            chatsView.displayNoChats()
        } else {
            this.chats = chats
            chatsView.displayChats(chats)
        }
    }

    override fun loadPrivateChats() {
        chatsRepository.getPrivateChats(this@ChatsPresenterImpl)
    }

    override fun loadPublicChats() {
        chatsRepository.getPublicChats(this@ChatsPresenterImpl)
    }

}