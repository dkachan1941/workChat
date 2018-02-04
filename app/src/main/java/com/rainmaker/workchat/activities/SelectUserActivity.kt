package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import android.support.v7.widget.DividerItemDecoration
import com.rainmaker.workchat.*


class SelectUserActivity : AppCompatActivity() {

    lateinit var usersRecyclerView: RecyclerView
    lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        usersAdapter = UsersAdapter(ArrayList()){
            val intent = Intent()
            intent.putExtra(USER_UID, it.uuid)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        mLinearLayoutManager = LinearLayoutManager(baseContext)
        mLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        usersRecyclerView.layoutManager = mLinearLayoutManager
        usersRecyclerView.adapter = usersAdapter
        val mDividerItemDecoration = DividerItemDecoration(usersRecyclerView.context, DividerItemDecoration.VERTICAL)
        usersRecyclerView.addItemDecoration(mDividerItemDecoration)

        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("users")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: List<User?> = dataSnapshot.children.map { it.getValue(User::class.java) }
                if (userList.isNotEmpty()){
                    usersAdapter.chatList = ArrayList(userList)
                    usersAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        usersdRef.addListenerForSingleValueEvent(eventListener)
    }
}
