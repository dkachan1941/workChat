package com.rainmaker.workchat.controllers

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.HomePresenter
import com.rainmaker.workchat.presenters.HomePresenterImpl
import com.rainmaker.workchat.repository.ChatsRepositoryImpl

/**
 * Created by dmitry on 1/29/18.
 * home controller
 */

class HomeController: Controller(), HomePresenter.HomeView {

    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: EventsAdapter

    private val homePresenter: HomePresenter by lazy {
        HomePresenterImpl(this@HomeController, ChatsRepositoryImpl())
    }

    override fun displayChats(chats: MutableList<ChatModelNew?>) {
        mProgressBar.visibility = View.GONE
        textViewNoChats.visibility = View.GONE
        mMessageRecyclerView.visibility = View.VISIBLE
        chatsAdapter.setData(ArrayList(chats))
        chatsAdapter.notifyDataSetChanged()
    }

    override fun showUnReadChats(newChats: MutableList<String>) {
        homePresenter.loadChats(newChats)
    }

    override fun showNoChats() {
        textViewNoChats.visibility = View.VISIBLE
        mMessageRecyclerView.visibility = View.GONE
        mProgressBar.visibility = View.GONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        setUpViews(view)

        // load
        homePresenter.loadUnReadChats()

        return view
    }

    private fun setUpViews(view: View) {
        chatsAdapter = EventsAdapter(ArrayList())
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mLinearLayoutManager = LinearLayoutManager(view.context)
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
        textViewNoChats = view.findViewById(R.id.textViewNoChats)
        textViewNoChats.text = applicationContext?.getString(R.string.no_new_events) ?: ""
    }

}
