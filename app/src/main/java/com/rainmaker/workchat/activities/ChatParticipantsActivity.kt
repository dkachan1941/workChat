package com.rainmaker.workchat.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rainmaker.workchat.*

class ChatParticipantsActivity : AppCompatActivity() {

    lateinit var usersRecyclerView: RecyclerView
    lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats_members)
        setUpViews()
        setUpFireBaseListener()
    }

    private fun setUpFireBaseListener() {
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: List<User?> = dataSnapshot.children.map { User(it.key, it.value as String?) }
                if (userList.isNotEmpty()){
                    usersAdapter.chatList = ArrayList(userList)
                    usersAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        FirebaseDatabase.getInstance().reference.child("$CHILD_ROOMS/${intent?.getStringExtra(CHAT_ID)}/$CHILD_USERS")
                .addListenerForSingleValueEvent(eventListener)
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
    }
}
