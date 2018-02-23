package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.DividerItemDecoration
import com.rainmaker.workchat.*
import com.rainmaker.workchat.presenters.FirebasePresenter
import javax.inject.Inject

class SelectUserActivity : AppCompatActivity(), FirebasePresenter.FirebasePresenterListener {

    @Inject
    lateinit var firebasePresenter: FirebasePresenter

    lateinit var usersRecyclerView: RecyclerView
    lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)
        (applicationContext as App).component.inject(this@SelectUserActivity)
        setUpViews()
    }

    private fun setUpViews() {
        usersAdapter = UsersAdapter(ArrayList()){
            val intent = Intent()
            intent.putExtra(USER_UID, it.uuid)
            intent.putExtra(USER_NAME, it.name)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        mLinearLayoutManager = LinearLayoutManager(baseContext)
        mLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        usersRecyclerView.layoutManager = mLinearLayoutManager
        usersRecyclerView.adapter = usersAdapter
        usersRecyclerView.addItemDecoration(
                DividerItemDecoration(usersRecyclerView.context, DividerItemDecoration.VERTICAL)
        )
        firebasePresenter.requestAllUsers(this@SelectUserActivity)
    }

    override fun onChatUsersLoaded(users: List<User?>?) {
        if (users !== null && users.isNotEmpty()){
            usersAdapter.chatList = ArrayList(users)
            usersAdapter.notifyDataSetChanged()
        }
    }
}
