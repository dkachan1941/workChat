package com.rainmaker.workchat.controllers

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R

class PublicChatRoomsController : Controller() {
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        chatsAdapter = ChatsAdapter(ArrayList())
        setUpViews(view)
        setUpFireBaseListener(view)
        return view
    }

    private fun setUpFireBaseListener(view: View?) {
        FirebaseDatabase.getInstance()?.reference
                ?.child(CHILD_ROOMS)?.ref?.orderByChild(FIELD_IS_PUBLIC)?.equalTo("true")
                ?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                val chatsList = p0?.children?.map { it.getValue(ChatModel1::class.java) }
                p0?.children?.mapIndexed { index, dataSnapshot ->
                    chatsList?.get(index)?.key = dataSnapshot.key
                }
                mProgressBar.visibility = View.GONE
                if (chatsList?.size != null && chatsList.isNotEmpty()){
                    chatsAdapter.setData(ArrayList(chatsList))
                    textViewNoChats.visibility = View.GONE
                } else {
                    textViewNoChats.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, view?.context?.getString(R.string.err_making_request))
            }
        })
    }

    private fun setUpViews(view: View) {
        mProgressBar = view.findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mLinearLayoutManager = LinearLayoutManager(view.context)
        textViewNoChats = view.findViewById(R.id.textViewNoChats)
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
    }

}
