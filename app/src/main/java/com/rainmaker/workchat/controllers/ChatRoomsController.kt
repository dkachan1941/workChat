package com.rainmaker.workchat.controllers

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R

class ChatRoomsController : Controller() {

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressBar
    private var mFirebaseUser: FirebaseUser? = null
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private val ROOMS_CHILD = "rooms"
    private var chatsAdapter = ChatsAdapter(ArrayList())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
        mProgressBar = view.findViewById(R.id.progressBar)
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")

        checkAuth()

        mProgressBar.visibility = View.VISIBLE
        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mMessageRecyclerView.adapter = chatsAdapter
        mLinearLayoutManager = LinearLayoutManager(view.context)
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val chatsRefNew = mFirebaseDatabaseReference.child(ROOMS_CHILD).ref.orderByChild("users/${mFirebaseAuth.currentUser?.uid}").equalTo("true")
        chatsRefNew.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                val chatsList = p0?.children?.map { it.getValue(ChatModel1::class.java) }
                p0?.children?.mapIndexed { index, dataSnapshot ->
                    chatsList?.get(index)?.key = dataSnapshot.key
                }
                val textViewNoChats = view.findViewById<TextView>(R.id.textViewNoChats)
                mProgressBar.visibility = View.GONE
                if (chatsList?.size != null && chatsList.isNotEmpty()){
                    chatsAdapter.setData(ArrayList(chatsList))
                    chatsAdapter.notifyDataSetChanged()
                    textViewNoChats.visibility = View.GONE
                } else {
                    textViewNoChats.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })

        mMessageRecyclerView.layoutManager = mLinearLayoutManager

        return view
    }

    private fun checkAuth() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth.currentUser

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
//            startActivity(Intent(this, SignInActivity::class.java))
//            finish()
            return
        } else { // TODO think about !! below
            mUsername = mFirebaseUser!!.displayName
            if (mFirebaseUser!!.photoUrl != null) {
                mPhotoUrl = mFirebaseUser!!.photoUrl!!.toString()
            }
        }
    }

}
