package com.rainmaker.workchat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.FirebaseDatabase


class ChatRoomsActivity : AppCompatActivity() {

    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    class ChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var chatNameTextView: TextView = itemView.findViewById(R.id.chatName)
        internal var chatStatusTextView: TextView = itemView.findViewById(R.id.chatStatus)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_rooms)
        val mToolbar = findViewById<Toolbar>(R.id.toolbar_main)
        mToolbar.title = ""
        setSupportActionBar(mToolbar)

        mMessageRecyclerView = findViewById(R.id.roomsRrecyclerView)
        mLinearLayoutManager = LinearLayoutManager(this)
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<ChatModel> { dataSnapshot ->
            val chat = dataSnapshot.getValue(ChatModel::class.java)
            if (chat != null) {
                chat.id = dataSnapshot.key
            }
            chat
        }

        val chatsRef = mFirebaseDatabaseReference.child(ChatActivity.ROOMS_CHILD)

        val options = FirebaseRecyclerOptions.Builder<ChatModel>()
                .setQuery(chatsRef, parser)
                .build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(options) {

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ChatViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                return ChatViewHolder(inflater.inflate(R.layout.item_chat, viewGroup, false))
            }

            override fun onBindViewHolder(viewHolder: ChatViewHolder,
                                          position: Int,
                                          chatItem: ChatModel) {

//                mProgressBar.setVisibility(ProgressBar.INVISIBLE)

                viewHolder.chatNameTextView.text = chatItem.name
                viewHolder.chatStatusTextView.text = chatItem.status.toString()

                val intent = Intent(viewHolder.chatNameTextView.context, ChatActivity::class.java)
                intent.putExtra("chatName", chatItem.name)
                intent.putExtra("chatId", chatItem.id)
                viewHolder.chatNameTextView.setOnClickListener { startActivity(intent) }


//                if (chatItem.text != null) {
//                    // write this message to the on-device index
//                    FirebaseAppIndex.getInstance().update(getMessageIndexable(chatItem))
//                }
//
//                // log a view action on it
//                FirebaseUserActions.getInstance().end(getMessageViewAction(chatItem))
            }
        }

        mFirebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = mFirebaseAdapter.getItemCount()
                val lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    mMessageRecyclerView.scrollToPosition(positionStart)
                }
            }
        })

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager)
        mMessageRecyclerView.setAdapter(mFirebaseAdapter)

    }

//    private fun getMessageViewAction(friendlyMessage: MessageModel): Action {
//        return Action.Builder(Action.Builder.VIEW_ACTION)
//                .setObject(friendlyMessage.name!!, MESSAGE_URL + friendlyMessage.id)
//                .setMetadata(Action.Metadata.Builder().setUpload(false))
//                .build()
//    }
//
//    private fun getMessageIndexable(friendlyMessage: MessageModel): Indexable {
//        val sender = Indexables.personBuilder()
//                .setIsSelf(mUsername == friendlyMessage.name)
//                .setName(friendlyMessage.name!!)
//                .setUrl(MESSAGE_URL + (friendlyMessage.id!! + "/sender"))
//
//        val recipient = Indexables.personBuilder()
//                .setName(mUsername)
//                .setUrl(MESSAGE_URL + (friendlyMessage.id!! + "/recipient"))
//
//        return Indexables.messageBuilder()
//                .setName(friendlyMessage.text!!)
//                .setUrl(MESSAGE_URL + friendlyMessage.id)
//                .setSender(sender)
//                .setRecipient(recipient)
//                .build()
//    }

    public override fun onPause() {
        mFirebaseAdapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

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
        startActivity(Intent(this, NewChatActivity::class.java))
    }

}
