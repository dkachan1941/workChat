package com.rainmaker.workchat.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.EncryptionPresenter
import com.rainmaker.workchat.presenters.FirebasePresenter

import java.util.HashMap

import javax.inject.Inject

class ChatActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, ChatFireBaseAdapter.ChatAdapterListener {

    @Inject
    lateinit var firebasePresenter: FirebasePresenter

    @Inject
    lateinit var encryptionPresenter: EncryptionPresenter

    private lateinit var mUsername: String
    private lateinit var mPhotoUrl: String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mSendButton: Button
    private lateinit var mMessageRecyclerView: RecyclerView
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<MessageModel, ChatFireBaseAdapter.MessageViewHolder>
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mFirebaseDatabaseReference: DatabaseReference
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mMessageEditText: EditText
    private lateinit var mAddMessageImageView: ImageView
    private lateinit var mToolbar: Toolbar
    private lateinit var chatName: String
    private lateinit var chatId: String
    private lateinit var textViewNoNewMessages: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (applicationContext as App).component.inject(this@ChatActivity)
        mSharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        chatName = intent?.getStringExtra(CHAT_NAME) ?: ""
        chatId = intent?.getStringExtra(CHAT_ID) ?: ""
        checkAuth()
        setUpViews()
        setUpChatRecyclerView()
        setMessagesViewed()
    }

    private fun setMessagesViewed() {
        if (mFirebaseAuth.currentUser?.uid != null){
            firebasePresenter.resetNewMessagesCount(mFirebaseAuth.currentUser?.uid, chatId)
        }
    }

    private fun setUpChatRecyclerView() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val parser = SnapshotParser<MessageModel> { dataSnapshot ->
            val chatMessage = dataSnapshot.getValue(MessageModel::class.java)
            if (chatMessage != null) {
                chatMessage.id = dataSnapshot.key
            }
            chatMessage
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

        if (!mSharedPreferences.getString(chatId, "").isEmpty()){
            encryptionPresenter.setKey(mSharedPreferences.getString(chatId, "").toByteArray())
        } else {
            encryptionPresenter.setKey(null)
        }
        mFirebaseAdapter = ChatFireBaseAdapter(applicationContext, encryptionPresenter, this@ChatActivity, options, mFirebaseAuth.currentUser)
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
    }

    private fun addImageIfUserLoggedIn() {
        if (mFirebaseAuth.currentUser == null) {
            Toast.makeText(baseContext, getString(R.string.msg_cannot_send_msg_if_no_signed_in), Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE)
        }
    }

    private fun sendMessageIfUserLoggedIn() {
        if (mFirebaseAuth.currentUser == null) {
            Toast.makeText(baseContext, getString(R.string.msg_cannot_send_msg_if_no_signed_in), Toast.LENGTH_SHORT).show()
        } else {
            val modifiedMsg = encryptMsgIfRequired(mMessageEditText.text.toString())
            val newMessage = MessageModel(modifiedMsg, mUsername, mPhotoUrl, null, mFirebaseAuth.currentUser?.uid)
            firebasePresenter.sendMessage(mFirebaseDatabaseReference, newMessage, chatId, true, null)
            mMessageEditText.setText("")
        }
    }

    private fun encryptMsgIfRequired(msg: String): String {
        val pw = mSharedPreferences.getString(chatId, "")
        return if (pw.isEmpty()){
            msg
        } else {
            encryptionPresenter.encrypt(msg)
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

        mMessageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))
        mMessageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mSendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }
            override fun afterTextChanged(editable: Editable) {}
        })

        mAddMessageImageView.setOnClickListener {
            addImageIfUserLoggedIn()
        }

        mSendButton.setOnClickListener {
            sendMessageIfUserLoggedIn()
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
        setMessagesViewed()
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
            R.id.use_encryption_key -> {
                showEncryptionKeyDialog()
                true
            }
            R.id.clear_encryption_key -> {
                mSharedPreferences.edit().putString(chatId, "").apply()
                encryptionPresenter.setKey(null)
                mFirebaseAdapter.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("InflateParams")
    private fun showEncryptionKeyDialog() {
        val alert = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.set_encryption_key_dialog, null)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.dialog_editText_password)

        with (alert) {
            setTitle("Set encryption password")
            setView(dialogView)

            setPositiveButton("Assign") {
                dialog, _ ->
                setEncryptionPasswordForChat(passwordEditText.text?.toString() ?: "")
                dialog.dismiss()
            }

            setNegativeButton("Cancel") {
                dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.show()
    }

    private fun setEncryptionPasswordForChat(password: String) {
        if (password.isNotEmpty()){
            val encryptionPw = modifyEncryptionKey(password)
            mSharedPreferences.edit().putString(chatId, encryptionPw).apply()
            encryptionPresenter.setKey(encryptionPw.toByteArray())
            mFirebaseAdapter.notifyDataSetChanged()
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
                val ti = object : GenericTypeIndicator<HashMap<String?, String?>?>() {}
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users: HashMap<String?, String?>? = dataSnapshot.getValue(ti)
                        if (users != null) {
                            if (!users.containsKey(userUid)) {
                                users[userUid] = userName

                                // add user
                                mFirebaseDatabaseReference.child(CHILD_ROOMS).child(chatId).child(CHILD_USERS).setValue(users)

                                // add user added msg
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
                val newMessage = MessageModel(null, mUsername, mPhotoUrl, task.result.downloadUrl?.toString(), mFirebaseAuth.currentUser?.uid)
                firebasePresenter.sendMessage(mFirebaseDatabaseReference, newMessage, chatId, false, key)
            } else {
                Log.w(TAG, resources.getString(R.string.err_image_loading),
                        task.exception)
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, resources.getString(R.string.err_google_services) + connectionResult)
    }

    override fun onChatAdapterFirstItemLoaded() {
        mProgressBar.visibility = ProgressBar.INVISIBLE
    }

    override fun onChatAdapterItemClick(item: MessageModel) {

    }

    override fun onChatAdapterItemLongClick(item: MessageModel) {

    }

}
