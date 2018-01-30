package com.rainmaker.workchat.controllers

import android.content.Intent

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.bluelinelabs.conductor.Controller
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_chat_rooms.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ChatRoomsController : Controller() {
    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        mFirebaseAdapter.stopListening()
    }

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressBar
    private var mFirebaseUser: FirebaseUser? = null
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null

    class ChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var chatNameTextView: TextView = itemView.findViewById(R.id.chatName)
        internal var chatStatusTextView: TextView = itemView.findViewById(R.id.chatStatus)
        internal var messageCount: Button = itemView.findViewById(R.id.messageCount)
        internal var lastMessageFrom: TextView = itemView.findViewById(R.id.lastMessageFrom)
        private var mClickListener: ChatViewHolder.ClickListener? = null

        interface ClickListener {
            fun onItemClick(view: View, position: Int)
            fun onItemLongClick(view: View, position: Int)
        }

        fun setOnClickListener(clickListener: ChatViewHolder.ClickListener) {
            mClickListener = clickListener
        }

        init {
//            chatNameTextView = itemView.findViewById(R.id.chatName)
//            chatStatusTextView = itemView.findViewById(R.id.chatStatus)
            itemView.setOnClickListener { v -> mClickListener!!.onItemClick(v, adapterPosition) }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.activity_chat_rooms, container, false)
//        val mToolbar = view.findViewById<Toolbar>(R.id.toolbar_main)
//        mToolbar.title = ""
//        setSupportActionBar(mToolbar)
        mProgressBar = view.findViewById(R.id.progressBar)

        FirebaseMessaging.getInstance().subscribeToTopic("notifications")

        checkAuth()

        mProgressBar.visibility = View.VISIBLE

        mMessageRecyclerView = view.findViewById(R.id.roomsRrecyclerView)
        mLinearLayoutManager = LinearLayoutManager(view.context)
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<ChatModel> { dataSnapshot ->
            val chat = dataSnapshot.getValue(ChatModel::class.java)
            if (chat != null) {
                chat.id = dataSnapshot.key
            }
            chat
        }

        val chatsRef = mFirebaseDatabaseReference.child(ChatActivity.ROOMS_CHILD).orderByKey().equalTo("-L0JymeoCWS3uaiBiv9K").ref

        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //onDataChange called so remove progress bar
                val textViewNoChats = view.findViewById<TextView>(R.id.textViewNoChats)
                if (!dataSnapshot.hasChildren()) {
                    mProgressBar.visibility = View.GONE
                    textViewNoChats.visibility = View.VISIBLE
                } else {
                    textViewNoChats.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        val options = FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(chatsRef, parser)
                .build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val viewHolder = ChatViewHolder(inflater.inflate(R.layout.item_chat, parent, false))
                viewHolder.setOnClickListener(object : ChatViewHolder.ClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val intent = Intent(viewHolder.chatNameTextView.context, ChatActivity::class.java)
                        intent.putExtra("chatId", mFirebaseAdapter.getRef(position).ref.key)
                        startActivity(intent)
                    }

                    override fun onItemLongClick(view: View, position: Int) {

                    }
                })
                return viewHolder
            }

            override fun onBindViewHolder(viewHolder: ChatViewHolder,
                                          position: Int,
                                          chatItem: ChatModel) {

                mProgressBar.visibility = ProgressBar.GONE

                viewHolder.chatNameTextView.text = chatItem.name
//                viewHolder.chatStatusTextView.text = chatItem.messageCount.toString()
                chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val items = dataSnapshot.child(chatItem.id).child("messages").value
                        if (items is HashMap<*, *>){
                            viewHolder.chatStatusTextView.text = String.format(resources!!.getString(R.string.message_count), items.size)
                        } else {
                            viewHolder.chatStatusTextView.text = String.format(resources!!.getString(R.string.message_count), 0)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

                val lastQuery = chatsRef.child(chatItem.id).child("messages").orderByKey().limitToLast(1)
                lastQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.map {
                            viewHolder.lastMessageFrom.text = String.format(resources!!.getString(R.string.last_message_from, it.getValue<MessageModel>(MessageModel::class.java)?.name))
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        //Handle possible errors.
                    }
                })

                viewHolder.messageCount.visibility = View.INVISIBLE
            }
        }

        mFirebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = mFirebaseAdapter.itemCount
                val lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    mMessageRecyclerView.scrollToPosition(positionStart)
                }
            }
        })

        mMessageRecyclerView.layoutManager = mLinearLayoutManager
        mMessageRecyclerView.adapter = mFirebaseAdapter

        mFirebaseAdapter.startListening()

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

//    public override fun onPause() {
//        mFirebaseAdapter.stopListening()
//        super.onPause()
//    }
//
//    public override fun onResume() {
//        super.onResume()
//        mFirebaseAdapter.startListening()
//        mProgressBar.visibility = View.VISIBLE
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.main_menu, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_chat -> {
                addChat()
                return true
            }
            R.id.invite_menu -> {
//                sendInvitation()
                return true
            }
            R.id.crash_menu -> {
//                FirebaseCrash.logcat(Log.ERROR, TAG, "crash caused")
//                causeCrash()
                return true
            }
            R.id.sign_out_menu -> {
//                mFirebaseAuth.signOut()
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
//                mFirebaseUser = null
//                mUsername = ANONYMOUS
//                mPhotoUrl = null
//                startActivity(Intent(this, SignInActivity::class.java))
//                finish()
                return true
            }
            R.id.fresh_config_menu -> {
//                fetchConfig()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun addChat() {
//        startActivity(Intent(this, NewChatActivity::class.java))
    }

}
