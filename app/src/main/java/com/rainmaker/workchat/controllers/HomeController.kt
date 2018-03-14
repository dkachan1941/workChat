package com.rainmaker.workchat.controllers

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.HomePresenter
import com.rainmaker.workchat.presenters.HomePresenterImpl
import com.rainmaker.workchat.repository.ChatsRepository

import java.util.HashMap

/**
 * Created by dmitry on 1/29/18.
 * home controller
 */

class HomeController: Controller(), HomePresenter.HomeView {

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

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var chatsList: MutableList<ChatModelNew?>
    private lateinit var homePresenter: HomePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        mFirebaseAuth = FirebaseAuth.getInstance()
        chatsAdapter = ChatsAdapter(ArrayList())
        homePresenter = HomePresenterImpl(this@HomeController, ChatsRepository())

        setUpViews(view)

        homePresenter.loadUnReadChats()

        return view
    }

    private fun setUpViews(view: View) {
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mLinearLayoutManager = LinearLayoutManager(view.context)
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
        textViewNoChats = view.findViewById(R.id.textViewNoChats)
        textViewNoChats.text = applicationContext?.getString(R.string.no_new_events) ?: ""
        if (mFirebaseAuth.currentUser?.uid == null){
            mProgressBar.visibility = View.GONE
            textViewNoChats.visibility = View.VISIBLE
        }
    }

}
