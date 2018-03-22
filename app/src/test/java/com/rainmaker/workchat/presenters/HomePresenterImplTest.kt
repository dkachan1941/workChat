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

class HomePresenterImplTest {

    private lateinit var chatsRepository: ChatsRepository
    private lateinit var homeView: HomePresenter.HomeView
    private lateinit var presenter: HomePresenterImpl

    private val unreadChatsList = mutableListOf("-L7Pfnr9XIiF6VoB_F8M", "-L7_rHYhVlNlDzn2WXVJ")
    private val emptyUnreadChatsList = mutableListOf<String>()

    private val chatsList = mutableListOf(ChatModelNew(), ChatModelNew(), ChatModelNew())
    private val emptyChatsList = mutableListOf<ChatModelNew>()

    @Before
    fun setUp() {
        homeView = Mockito.mock(HomePresenter.HomeView::class.java)
        chatsRepository = Mockito.mock(ChatsRepository::class.java)
        presenter = HomePresenterImpl(homeView, chatsRepository)
    }

    @Test
    fun loadChats_withChats() {

        doAnswer {
            presenter.onChatsLoaded(chatsList.toMutableList())
        }.`when`(chatsRepository).getChats(presenter, unreadChatsList)

        presenter.loadChats(unreadChatsList)

        Mockito.verify(homeView).displayChats(chatsList.toMutableList())
    }

    @Test
    fun loadChats_noChats() {

        doAnswer {
            presenter.onChatsLoaded(emptyChatsList.toMutableList())
        }.`when`(chatsRepository).getChats(presenter, emptyUnreadChatsList)

        presenter.loadChats(emptyUnreadChatsList)

        Mockito.verify(homeView).showNoChats()
    }

    @Test
    fun loadUnReadChats_withChats() {

        doAnswer {
            presenter.onUnReadChatsLoaded(unreadChatsList)
        }.`when`(chatsRepository).getUnReadChats(presenter)

        presenter.loadUnReadChats()

        Mockito.verify(homeView).showUnReadChats(unreadChatsList)

    }

    @Test
    fun loadUnReadChats_NoChats() {

        doAnswer {
            presenter.onUnReadChatsLoaded(emptyUnreadChatsList)
        }.`when`(chatsRepository).getUnReadChats(presenter)

        presenter.loadUnReadChats()

        Mockito.verify(homeView).showNoChats()

    }

}