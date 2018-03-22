package com.rainmaker.workchat.presenters

import com.rainmaker.workchat.ChatModelNew
import com.rainmaker.workchat.repository.ChatsRepository
import org.junit.Test

import org.junit.Before
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 * Created by dmitry on 3/15/18.
 *
 */

class ChatsPresenterImplTest {

    private lateinit var chatsRepository: ChatsRepository
    private lateinit var chatsView: ChatsPresenter.chatsView
    private lateinit var presenter: ChatsPresenterImpl

    private val chatsList = mutableListOf(
            ChatModelNew("", 0, "", "", hashMapOf(), "", "-L7Pfnr9XIiF6VoB_F8M"),
            ChatModelNew("", 0, "", "", hashMapOf(), "", "-L7_rHYhVlNlDzn2WXVJ"),
            ChatModelNew())
    private val emptyChatsList = mutableListOf<ChatModelNew>()

    private val messagesCountForChats: HashMap<String?, String?> = hashMapOf("-L7Pfnr9XIiF6VoB_F8M" to "3", "-L7_rHYhVlNlDzn2WXVJ" to "6")
    private val emptyMessagesCountForChats: HashMap<String?, String?> = hashMapOf()

    @Before
    fun setUp() {
        chatsView = Mockito.mock(ChatsPresenter.chatsView::class.java)
        chatsRepository = Mockito.mock(ChatsRepository::class.java)
        presenter = ChatsPresenterImpl(chatsView, chatsRepository)
    }

    @Test
    fun loadPrivateChats_withChats() {

        doAnswer {
            presenter.onChatsLoaded(chatsList)
        }.`when`(chatsRepository).getPrivateChats(presenter)

        presenter.loadPrivateChats()

        Mockito.verify(chatsView).displayChats(chatsList.toMutableList())

    }

    @Test
    fun loadPrivateChats_noChats() {

        doAnswer {
            presenter.onChatsLoaded(emptyChatsList)
        }.`when`(chatsRepository).getPrivateChats(presenter)

        presenter.loadPrivateChats()

        Mockito.verify(chatsView).displayNoChats()

    }

    @Test
    fun loadNewMessagesCountForChats() {

        doAnswer {
            presenter.onNewMessagesCountForChatLoaded(messagesCountForChats)
        }.`when`(chatsRepository).getNewMessagesCountForChats(presenter)

        presenter.chats = chatsList
        presenter.loadNewMessagesCountForChats()

        Mockito.verify(chatsView).displayChats(Mockito.anyList())

    }

    @Test
    fun loadNewMessagesCountForChats_noMessages() {

        doAnswer {
            presenter.onNewMessagesCountForChatLoaded(emptyMessagesCountForChats)
        }.`when`(chatsRepository).getNewMessagesCountForChats(presenter)

        presenter.loadNewMessagesCountForChats()

        Mockito.verify(chatsView, times(0)).displayChats(Mockito.anyList())

    }



}