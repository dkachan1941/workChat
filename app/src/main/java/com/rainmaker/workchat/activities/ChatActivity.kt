package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rainmaker.workchat.*

import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mUsername: String
    private lateinit var mPhotoUrl: String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mSendButton: Button
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<MessageModel, MessageViewHolder>
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mMessageEditText: EditText
    private lateinit var mAddMessageImageView: ImageView
    private lateinit var mToolbar: Toolbar
    private lateinit var chatName: String
    private lateinit var chatId: String
    private lateinit var textViewNoNewMessages: TextView

    class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        internal var messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        internal var messageImageView: ImageView = itemView.findViewById(R.id.messageImageView)
        internal var messengerTextView: TextView = itemView.findViewById(R.id.messengerTextView)
        internal var messengerImageView: CircleImageView = itemView.findViewById(R.id.messengerImageView)
        internal var messageLayout: CardView = itemView.findViewById(R.id.card_chat_layout)
        private var mClickListener: MessageViewHolder.ClickListener? = null

        interface ClickListener {
            fun onItemClick(view: View, position: Int)
            fun onItemLongClick(view: View, position: Int)
        }

        fun setOnClickListener(clickListener: MessageViewHolder.ClickListener) {
            mClickListener = clickListener
        }

        init {
            itemView.setOnClickListener { view -> mClickListener?.onItemClick(view, adapterPosition) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        chatName = intent?.getStringExtra(CHAT_NAME) ?: ""
        chatId = intent?.getStringExtra(CHAT_ID) ?: ""
        checkAuth()
        setUpViews()

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val parser = SnapshotParser<MessageModel> { dataSnapshot ->
            val friendlyMessage = dataSnapshot.getValue(MessageModel::class.java)
            if (friendlyMessage != null) {
                friendlyMessage.id = dataSnapshot.key
            }
            friendlyMessage
        }
        val messagesRef = mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_MESSAGES)
        val options = FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(messagesRef, parser)
                .build()

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    mProgressBar.visibility = View.GONE
                    textViewNoNewMessages.visibility = View.VISIBLE
                } else {
                    textViewNoNewMessages.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, resources.getString(R.string.err_making_request))
            }
        })

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<MessageModel, MessageViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val viewHolder = MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false))
                viewHolder.setOnClickListener(object : MessageViewHolder.ClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        // todo
                    }
                    override fun onItemLongClick(view: View, position: Int) {
                        // todo
                    }
                })
                return viewHolder
            }

            override fun onBindViewHolder(viewHolder: MessageViewHolder,
                                          position: Int,
                                          curMessage: MessageModel) {
                mProgressBar.visibility = ProgressBar.INVISIBLE
                setMessageColor(viewHolder.messageLayout, mFirebaseAuth.currentUser, curMessage.userUid)
                if (curMessage.text != null) {
                    viewHolder.messageTextView.text = curMessage.text
                    viewHolder.messageTextView.visibility = TextView.VISIBLE
                    viewHolder.messageImageView.visibility = ImageView.GONE
                } else if (LOADING_IMAGE_URL != curMessage.imageUrl) {
                    val imageUrl = curMessage.imageUrl
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
                                Log.w(TAG, resources.getString(R.string.err_making_request), task.exception)
                            }
                        }
                    } else {
                        Glide.with(viewHolder.messageImageView.context)
                                .load(curMessage.imageUrl)
                                .into(viewHolder.messageImageView)
                    }
                    viewHolder.messageImageView.visibility = ImageView.VISIBLE
                    viewHolder.messageTextView.visibility = TextView.GONE
                }

                viewHolder.messengerTextView.text = curMessage.name
                if (curMessage.photoUrl == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(this@ChatActivity,
                            R.drawable.ic_account_circle_black_36dp))
                } else {
                    Glide.with(this@ChatActivity)
                            .load(curMessage.photoUrl)
                            .into(viewHolder.messengerImageView)
                }
            }

            private fun setMessageColor(messageLayout: android.support.v7.widget.CardView, currentUser: com.google.firebase.auth.FirebaseUser?, userUid: kotlin.String?) {
                when {
                    currentUser == null -> messageLayout.setBackgroundColor(resources.getColor(FOREIGN_MESSAGE_COLOR)) // todo
                    currentUser.uid == userUid -> messageLayout.setBackgroundColor(resources.getColor(MY_MESSAGE_COLOR))
                    else -> messageLayout.setBackgroundColor(resources.getColor(FOREIGN_MESSAGE_COLOR))
                }
            }
        }

        mFirebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                textViewNoNewMessages.visibility = View.GONE
                val messageCount = mFirebaseAdapter.itemCount
                val lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= messageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    mMessageRecyclerView.scrollToPosition(positionStart)
                }
            }
        })
        mMessageRecyclerView.layoutManager = mLinearLayoutManager
        mMessageRecyclerView.adapter = mFirebaseAdapter

        mMessageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT)))
        mMessageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mSendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        mAddMessageImageView.setOnClickListener {
            if (mFirebaseAuth.currentUser == null) {
                Toast.makeText(baseContext, getString(R.string.msg_cannot_send_msg_if_no_signed_in), Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }
        }

        mSendButton.setOnClickListener {
            if (mFirebaseAuth.currentUser == null) {
                Toast.makeText(baseContext, getString(R.string.msg_cannot_send_msg_if_no_signed_in), Toast.LENGTH_SHORT).show()
            } else {
                val workchatMessage = MessageModel(mMessageEditText.text.toString(), mUsername, mPhotoUrl, null, mFirebaseAuth.currentUser?.uid)
                mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_MESSAGES).push().setValue(workchatMessage)
                mMessageEditText.setText("")
            }
        }
    }

    private fun setUpViews() {
        mToolbar = findViewById(R.id.chatToolbar)
        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar.visibility = View.VISIBLE
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView)
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager.stackFromEnd = true
        textViewNoNewMessages = findViewById(R.id.textViewNoNewMessages)
        textViewNoNewMessages.visibility = View.GONE
        mSendButton = findViewById(R.id.sendButton)
        mMessageEditText = findViewById(R.id.messageEditText)
        mAddMessageImageView = findViewById(R.id.addMessageImageView)
        setSupportActionBar(mToolbar)
        if (supportActionBar != null) {
            supportActionBar?.title = chatName
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun checkAuth() {
        mUsername = ANONYMOUS
        mFirebaseAuth = FirebaseAuth.getInstance()
        mPhotoUrl = mFirebaseAuth.currentUser?.photoUrl?.toString() ?: ""
        if (mFirebaseAuth.currentUser == null) {
            Toast.makeText(this, resources.getString(R.string.err_auth_err), Toast.LENGTH_SHORT).show()
        } else {
            mUsername = mFirebaseAuth.currentUser?.displayName ?: ""
        }
    }

    public override fun onPause() {
        mFirebaseAdapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        mFirebaseAdapter.startListening()
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
                val intent = Intent(this@ChatActivity, ChatParticipantsActivity::class.java)
                intent.putExtra(CHAT_ID, chatId)
                startActivityForResult(intent, CODE_CHANGE_USERS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    val tempMessage = MessageModel(null, mUsername, mPhotoUrl, LOADING_IMAGE_URL, mFirebaseAuth.currentUser?.uid)
                    mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_MESSAGES).push()
                            .setValue(tempMessage) { databaseError, databaseReference ->
                                if (databaseError == null) {
                                    val storageReference = FirebaseStorage.getInstance()
                                            .getReference(mFirebaseAuth.currentUser?.uid ?: "")
                                            .child(databaseReference.key)
                                            .child(uri.lastPathSegment)
                                    putImageInStorage(storageReference, uri, databaseReference.key)
                                } else {
                                    Log.w(TAG, resources.getString(R.string.err_writing_to_db),
                                            databaseError.toException())
                                }
                            }
                }
            }
        } else if (requestCode == CODE_SELECT_USER) {
            if (resultCode == Activity.RESULT_OK) {
                val userUid = data?.getStringExtra(USER_UID)
                val userName = data?.getStringExtra(USER_NAME)
                // get existed users
                val ref = mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_USERS).ref
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users: HashMap<String?, String?>? = dataSnapshot.value as HashMap<String?, String?>?  // todo !!!
                        if (users != null) {
                            if (!users.containsKey(userUid)) {
                                users[userUid] = userName
                                mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_USERS).setValue(users)
                                val chatMsg = MessageModel(resources.getString(R.string.msg_new_user_added), mUsername, mPhotoUrl, null, mFirebaseAuth.currentUser?.uid)
                                mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_MESSAGES).push().setValue(chatMsg)
                            } else {
                                Toast.makeText(this@ChatActivity, resources.getString(R.string.msg_user_exists), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri?, key: String) {
        storageReference.putFile(uri!!).addOnCompleteListener(this@ChatActivity
        ) { task ->
            if (task.isSuccessful) {
                val friendlyMessage = MessageModel(null, mUsername, mPhotoUrl, task.result.downloadUrl?.toString(), mFirebaseAuth.currentUser?.uid)
                mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_MESSAGES).child(key)
                        .setValue(friendlyMessage)
            } else {
                Log.w(TAG, resources.getString(R.string.err_image_loading),
                        task.exception)
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, resources.getString(R.string.err_google_services) + connectionResult)
    }

}
