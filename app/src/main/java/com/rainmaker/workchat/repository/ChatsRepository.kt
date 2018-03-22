package com.rainmaker.workchat.repository

import com.rainmaker.workchat.presenters.ChatsPresenter
import com.rainmaker.workchat.presenters.HomePresenter

/**
 * Created by dmitry on 3/15/18.
 *
 */

interface ChatsRepository {
    fun getUnReadChats(listener: HomePresenter.RepositoryCallbacks)
    fun getChats(listener: HomePresenter.RepositoryCallbacks, newChats: MutableList<String>)
    fun getPrivateChats(listener: ChatsPresenter.RepositoryCallbacks)
    fun getNewMessagesCountForChats(listener: ChatsPresenter.RepositoryCallbacks)
    fun getPublicChats(listener: ChatsPresenter.RepositoryCallbacks)
}