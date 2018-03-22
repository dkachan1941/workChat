package com.rainmaker.workchat.controllers

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.ChatsPresenter
import com.rainmaker.workchat.presenters.ChatsPresenterImpl
import com.rainmaker.workchat.repository.ChatsRepositoryImpl

class PublicChatRoomsController : Controller(), ChatsPresenter.chatsView {

    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: ChatsAdapter

    private val chatsPresenter: ChatsPresenter by lazy {
        ChatsPresenterImpl(this@PublicChatRoomsController, ChatsRepositoryImpl())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        setUpViews(view)

        // load
        chatsPresenter.loadPublicChats()
        chatsPresenter.loadNewMessagesCountForChats()

        return view
    }

    private fun setUpViews(view: View) {
        chatsAdapter = ChatsAdapter(ArrayList())
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mLinearLayoutManager = LinearLayoutManager(view.context)
        textViewNoChats = view.findViewById(R.id.textViewNoChats)
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
    }

    override fun displayNoChats() {
        mMessageRecyclerView.visibility = View.GONE
        textViewNoChats.visibility = View.VISIBLE
    }

    override fun displayChats(chats: MutableList<ChatModelNew>) {
        mProgressBar.visibility = View.GONE
        textViewNoChats.visibility = View.GONE
        mMessageRecyclerView.visibility = View.VISIBLE
        chatsAdapter.setData(ArrayList(chats))
        chatsAdapter.notifyDataSetChanged()
    }

}
