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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import java.util.HashMap

class PrivateChatRoomsController : Controller() {

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressBar
    private lateinit var textViewNoChats: TextView
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var chatsList: List<ChatModel1?>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        mFirebaseAuth = FirebaseAuth.getInstance()
        chatsAdapter = ChatsAdapter(ArrayList())
        setUpViews(view)
        setUpFireBaseListener(view)
        return view
    }

    private fun setUpNewMessagesListener() {
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val ref = mFirebaseDatabaseReference.child(CHILD_USERS).child(mFirebaseAuth.currentUser?.uid ?: "").child(CHILD_NEW_MESSAGES).ref
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages: HashMap<String?, String?>? = dataSnapshot.value as HashMap<String?, String?>?  // todo !!!
                if (newMessages != null) {
                    chatsList.forEachIndexed { index, chatModel1 ->
                        if (newMessages.containsKey(chatModel1?.key)){
                            chatsList[index]?.messageCount = newMessages[chatModel1?.key]?.toInt()
                            chatsAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setUpFireBaseListener(view: View) {
        mFirebaseDatabaseReference.child(CHILD_ROOMS).ref
                .orderByChild("$CHILD_USERS/${mFirebaseAuth.currentUser?.uid}")
                .equalTo(mFirebaseAuth.currentUser?.displayName)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot?) {
                        val chatsListTemp = p0?.children?.map { it.getValue(ChatModel1::class.java) }
                        chatsList = chatsListTemp ?: mutableListOf()
                        setUpNewMessagesListener()
                        p0?.children?.mapIndexed { index, dataSnapshot ->
                            chatsList?.get(index)?.key = dataSnapshot.key
                        }
                        mProgressBar.visibility = View.GONE
                        if (chatsList?.size != null && chatsList.isNotEmpty()){
                            chatsAdapter.setData(ArrayList(chatsList))
                            chatsAdapter.notifyDataSetChanged()
                            textViewNoChats.visibility = View.GONE
                        } else {
                            textViewNoChats.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        Log.d(TAG, view.context?.getString(R.string.err_making_request))
                    }
                })
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
    }

}
