package com.rainmaker.workchat.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.rainmaker.workchat.*
import com.rainmaker.workchat.presenters.FirebasePresenter
import javax.inject.Inject

class ChatParticipantsActivity : AppCompatActivity(), FirebasePresenter.FirebasePresenterListener {

    @Inject
    lateinit var firebasePresenter: FirebasePresenter

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats_members)
        (applicationContext as App).component.inject(this@ChatParticipantsActivity)
        setUpViews()
    }

    private fun setUpViews() {
        usersAdapter = UsersAdapter(ArrayList()){
            // todo handle user click
        }
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        mLinearLayoutManager = LinearLayoutManager(baseContext)
        mLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        usersRecyclerView.layoutManager = mLinearLayoutManager
        usersRecyclerView.adapter = usersAdapter
        usersRecyclerView.addItemDecoration(
                DividerItemDecoration(usersRecyclerView.context, DividerItemDecoration.VERTICAL)
        )
        firebasePresenter.requestUsersForChat(intent?.getStringExtra(CHAT_ID) ?: "", this@ChatParticipantsActivity)
    }

    override fun onChatUsersLoaded(users: List<User?>?) {
        if (users !== null && users.isNotEmpty()){
            usersAdapter.chatList = ArrayList(users)
            usersAdapter.notifyDataSetChanged()
        }
    }
}
