package com.rainmaker.workchat.controllers

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.ChatsPresenter
import com.rainmaker.workchat.presenters.ChatsPresenterImpl
import com.rainmaker.workchat.repository.ChatsRepositoryImpl
import java.util.HashMap

class PrivateChatRoomsController : Controller(), ChatsPresenter.chatsView {

    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: ChatsAdapter

    private val chatsPresenter: ChatsPresenter by lazy {
        ChatsPresenterImpl(this@PrivateChatRoomsController, ChatsRepositoryImpl())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        setUpViews(view)

        // load
        chatsPresenter.loadPrivateChats()
        chatsPresenter.loadNewMessagesCountForChats()

        return view
    }

    private fun setUpViews(view: View) {
        chatsAdapter = ChatsAdapter(ArrayList())
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mLinearLayoutManager = LinearLayoutManager(view.context)
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
        textViewNoChats = view.findViewById(R.id.textViewNoChats)
        mMessageRecyclerView.visibility = View.GONE
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
