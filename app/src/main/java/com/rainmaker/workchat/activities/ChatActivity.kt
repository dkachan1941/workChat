package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rainmaker.workchat.CodelabPreferences
import com.rainmaker.workchat.MessageModel
import com.rainmaker.workchat.R

import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView

import com.rainmaker.workchat.CODE_SELECT_USER
import com.rainmaker.workchat.USER_UID

class ChatActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private var mSharedPreferences: SharedPreferences? = null

    private var mSendButton: Button? = null
    private var mMessageRecyclerView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<MessageModel, MessageViewHolder>? = null
    private var mProgressBar: ProgressBar? = null
    private var mFirebaseDatabaseReference: DatabaseReference? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mMessageEditText: EditText? = null
    private var mAddMessageImageView: ImageView? = null
    private var mToolbar: Toolbar? = null
    private var chatName: String? = null
    private var chatId: String? = null

    class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var messageTextView: TextView
        internal var messageImageView: ImageView
        internal var messengerTextView: TextView
        internal var messengerImageView: CircleImageView
        private var mClickListener: MessageViewHolder.ClickListener? = null

        interface ClickListener {
            fun onItemClick(view: View, position: Int)
            fun onItemLongClick(view: View, position: Int)
        }

        fun setOnClickListener(clickListener: MessageViewHolder.ClickListener) {
            mClickListener = clickListener
        }

        init {
            messageTextView = itemView.findViewById<View>(R.id.messageTextView) as TextView
            messageImageView = itemView.findViewById<View>(R.id.messageImageView) as ImageView
            messengerTextView = itemView.findViewById<View>(R.id.messengerTextView) as TextView
            messengerImageView = itemView.findViewById<View>(R.id.messengerImageView) as CircleImageView
            itemView.setOnClickListener { v -> mClickListener!!.onItemClick(v, adapterPosition) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatName = if (intent != null) intent.getStringExtra("chatName") else null
        chatId = if (intent != null) intent.getStringExtra("chatId") else null

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mUsername = ANONYMOUS

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.currentUser

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            //            startActivity(new Intent(this, SignInActivity.class));
            //            finish();
            Toast.makeText(this, "mFirebaseUser == null", Toast.LENGTH_SHORT).show()
            return
        } else {
            mUsername = mFirebaseUser!!.displayName
            if (mFirebaseUser!!.photoUrl != null) {
                mPhotoUrl = mFirebaseUser!!.photoUrl!!.toString()
            }
        }

        mProgressBar = findViewById(R.id.progressBar)
        mToolbar = findViewById(R.id.chatToolbar)
        mProgressBar!!.visibility = View.VISIBLE
        mMessageRecyclerView = findViewById<View>(R.id.messageRecyclerView) as RecyclerView
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager!!.stackFromEnd = true

        setSupportActionBar(mToolbar)
        if (supportActionBar != null) {
            supportActionBar!!.title = chatName
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference

        val parser = SnapshotParser<MessageModel> { dataSnapshot ->
            val friendlyMessage = dataSnapshot.getValue(MessageModel::class.java)
            if (friendlyMessage != null) {
                friendlyMessage.id = dataSnapshot.key
            }
            friendlyMessage
        }

        val messagesRef = mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child(MESSAGES_CHILD)

        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(messagesRef, parser)
                .build()

        val textViewNoNewMessages = findViewById<TextView>(R.id.textViewNoNewMessages)
        textViewNoNewMessages.visibility = View.GONE
        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //onDataChange called so remove progress bar
                if (!dataSnapshot.hasChildren()) {
                    mProgressBar!!.visibility = View.GONE
                    textViewNoNewMessages.visibility = View.VISIBLE
                } else {
                    textViewNoNewMessages.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<MessageModel, MessageViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val viewHolder = MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false))
                viewHolder.setOnClickListener(object : MessageViewHolder.ClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        //                        mFirebaseAdapter.getRef(position).getRef().
                    }

                    override fun onItemLongClick(view: View, position: Int) {
                        //                        Toast.makeText(getActivity(), "Item long clicked at " + position, Toast.LENGTH_SHORT).show();
                    }
                })
                return viewHolder
            }

            override fun onBindViewHolder(viewHolder: MessageViewHolder,
                                          position: Int,
                                          friendlyMessage: MessageModel) {

                mProgressBar!!.visibility = ProgressBar.INVISIBLE
                if (friendlyMessage.text != null) {
                    viewHolder.messageTextView.text = friendlyMessage.text
                    viewHolder.messageTextView.visibility = TextView.VISIBLE
                    viewHolder.messageImageView.visibility = ImageView.GONE
                } else {
                    val imageUrl = friendlyMessage.imageUrl
                    if (imageUrl != null && imageUrl.startsWith("gs://")) {
                        val storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl)
                        storageReference.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()
                                Glide.with(viewHolder.messageImageView.context)
                                        .load(downloadUrl)
                                        .into(viewHolder.messageImageView)
                            } else {
                                Log.w(TAG, "Getting download url was not successful.",
                                        task.exception)
                            }
                        }
                    } else {
                        Glide.with(viewHolder.messageImageView.context)
                                .load(friendlyMessage.imageUrl)
                                .into(viewHolder.messageImageView)
                    }
                    viewHolder.messageImageView.visibility = ImageView.VISIBLE
                    viewHolder.messageTextView.visibility = TextView.GONE
                }


                viewHolder.messengerTextView.text = friendlyMessage.name
                if (friendlyMessage.photoUrl == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(this@ChatActivity,
                            R.drawable.ic_account_circle_black_36dp))
                } else {
                    Glide.with(this@ChatActivity)
                            .load(friendlyMessage.photoUrl)
                            .into(viewHolder.messengerImageView)
                }
            }
        }

        mFirebaseAdapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                textViewNoNewMessages.visibility = View.GONE
                val friendlyMessageCount = mFirebaseAdapter!!.itemCount
                val lastVisiblePosition = mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    mMessageRecyclerView!!.scrollToPosition(positionStart)
                }
            }
        })

        mMessageRecyclerView!!.layoutManager = mLinearLayoutManager
        mMessageRecyclerView!!.adapter = mFirebaseAdapter

        mMessageEditText = findViewById<View>(R.id.messageEditText) as EditText
        mMessageEditText!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mSharedPreferences!!
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT)))
        mMessageEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mSendButton!!.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        mAddMessageImageView = findViewById<View>(R.id.addMessageImageView) as ImageView
        mAddMessageImageView!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }

        mSendButton = findViewById<View>(R.id.sendButton) as Button
        mSendButton!!.setOnClickListener {
            val workchatMessage = MessageModel(mMessageEditText!!.text.toString(), mUsername,
                    mPhotoUrl, null)
            mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child(MESSAGES_CHILD).push().setValue(workchatMessage)
            mMessageEditText!!.setText("")
            mFirebaseAnalytics!!.logEvent(MESSAGE_SENT_EVENT, null)
        }
    }

    public override fun onPause() {
        mFirebaseAdapter!!.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        mFirebaseAdapter!!.startListening()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.add_member_menu -> {
                startActivityForResult(Intent(this@ChatActivity, SelectUserActivity::class.java), CODE_SELECT_USER)
                true
            }
            R.id.all_members_menu -> {
                startActivityForResult(Intent(this@ChatActivity, ChatsMembersActivity::class.java), CODE_SELECT_USER)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    Log.d(TAG, "Uri: " + uri!!.toString())

                    val tempMessage = MessageModel(null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL)
                    mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child(MESSAGES_CHILD).push()
                            .setValue(tempMessage) { databaseError, databaseReference ->
                                if (databaseError == null) {
                                    val key = databaseReference.key
                                    val storageReference = FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser!!.uid)
                                            .child(key)
                                            .child(uri.lastPathSegment)

                                    putImageInStorage(storageReference, uri, key)
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException())
                                }
                            }
                }
            }
        } else if (requestCode == CODE_SELECT_USER) {
            if (resultCode == Activity.RESULT_OK) {
                val userUid = data!!.getStringExtra(USER_UID) // todo
                // get existed users
                val ref = mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child("users").ref
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users: HashMap<String, String>? = dataSnapshot.value as HashMap<String, String>?
                        if (users != null) {
                            if (!users.containsKey(userUid)) {
                                users[userUid] = "true"
                                mFirebaseDatabaseReference!!.child(ChatActivity.ROOMS_CHILD).child(chatId!!).child("users").setValue(users)
                                val friendlyMessage = MessageModel("Added a new member", mUsername,
                                        mPhotoUrl, null)
                                mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child(MESSAGES_CHILD).push().setValue(friendlyMessage)
                            } else {
                                Toast.makeText(this@ChatActivity, "user already added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            } else {
                // todo
            }
        }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri?, key: String) {
        storageReference.putFile(uri!!).addOnCompleteListener(this@ChatActivity
        ) { task ->
            if (task.isSuccessful) {
                val friendlyMessage = MessageModel(null, mUsername, mPhotoUrl,
                        task.result.downloadUrl!!
                                .toString())
                mFirebaseDatabaseReference!!.child(ROOMS_CHILD).child(chatId!!).child(MESSAGES_CHILD).child(key)
                        .setValue(friendlyMessage)
            } else {
                Log.w(TAG, "Image upload task was not successful.",
                        task.exception)
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
    }

    companion object {
        private val TAG = "ChatActivity"
        val MESSAGES_CHILD = "messages"
        val ROOMS_CHILD = "rooms"
        private val REQUEST_INVITE = 1
        private val REQUEST_IMAGE = 2
        val DEFAULT_MSG_LENGTH_LIMIT = 10
        val ANONYMOUS = "anonymous"
        private val MESSAGE_SENT_EVENT = "message_sent"
        private val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }

}
