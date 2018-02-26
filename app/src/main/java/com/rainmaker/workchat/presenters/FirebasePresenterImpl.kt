package com.rainmaker.workchat.presenters

import android.content.res.Resources
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import java.util.*
import javax.inject.Inject

/**
 * Created by dmitry on 2/8/18.
 *
 */
class FirebasePresenterImpl: FirebasePresenter {

    @Inject
    lateinit var resources: Resources

    override fun pushUserToDb(uuid: String?, email: String?, displayName: String?, providerId: String?) {
        val user = User()
        user.email = email
        user.name = displayName
        user.uuid = uuid
        user.provider = providerId
        val mFireBaseDatabaseReference = FirebaseDatabase.getInstance().reference.child(CHILD_USERS)
        mFireBaseDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.child(uuid).exists()) { // add user only if not exists
                    mFireBaseDatabaseReference.child(uuid).setValue(user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun createChat(text: String, password: String, encryptionPw: String, private: Boolean) {
        val fireBaseUser = FirebaseAuth.getInstance().currentUser
        if (fireBaseUser != null){
            val users = HashMap<String, String>()
            users[fireBaseUser.uid] = fireBaseUser.displayName ?: ""
            val newRoom = ChatModel(text, 0, Date().toString(), fireBaseUser.uid, users, password, encryptionPw, private)
            FirebaseDatabase.getInstance()?.reference?.child(CHILD_ROOMS)?.push()?.setValue(newRoom)
        } else {
            Log.d(TAG, resources.getString(R.string.err_error_creating_chat))
        }
    }

    override fun sendMessage(mFirebaseDatabaseReference: DatabaseReference?, newMessage: MessageModel, chatId: String, isUpdate: Boolean, key: String?) {
        if (isUpdate){
            mFirebaseDatabaseReference?.child(CHILD_ROOMS)?.child(chatId)?.child(CHILD_MESSAGES)?.push()?.setValue(newMessage)
        } else {
            mFirebaseDatabaseReference?.child(CHILD_ROOMS)?.child(chatId)?.child(CHILD_MESSAGES)?.child(key)?.setValue(newMessage)
        }
        addUnRedMessageForChatMembers(chatId)
    }

    private fun addUnRedMessageForChatMembers(chatId: String) {
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: List<User?> = dataSnapshot.children.map { User(it.key, it.value as String?) }
                userList.forEach { addUnRedMessageForUser(it, chatId) }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        FirebaseDatabase.getInstance().reference.child("$CHILD_ROOMS/$chatId/$CHILD_USERS")
                .addListenerForSingleValueEvent(eventListener)
    }

    private fun addUnRedMessageForUser(user: User?, chatId: String) {
        if (user?.uuid == FirebaseAuth.getInstance().currentUser?.uid) return
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val ref = mFirebaseDatabaseReference.child(CHILD_USERS).child(user?.uuid).child(CHILD_NEW_MESSAGES).ref
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var newMessages: HashMap<String?, String?>? = dataSnapshot.value as HashMap<String?, String?>?  // todo !!!
                if (newMessages != null) {
                    if (newMessages.containsKey(chatId)){
                        newMessages[chatId] = newMessages[chatId]?.toInt()?.plus(1).toString()
                    } else {
                        newMessages[chatId] = "1"
                    }
                } else {
                    newMessages = HashMap()
                    newMessages[chatId] = "1"
                }
                mFirebaseDatabaseReference.child(CHILD_USERS).child(user?.uuid).child(CHILD_NEW_MESSAGES).setValue(newMessages)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    override fun requestUsersForChat(chatId: String?, listener: FirebasePresenter.FirebasePresenterListener?){
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: List<User?> = dataSnapshot.children.map { User(it.key, it.value as String?) }
                listener?.onChatUsersLoaded(userList)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                listener?.onChatUsersLoaded(null)
            }
        }
        FirebaseDatabase.getInstance().reference.child("$CHILD_ROOMS/$chatId/$CHILD_USERS")
                .addListenerForSingleValueEvent(eventListener)
    }

    override fun requestAllUsers(listener: FirebasePresenter.FirebasePresenterListener?){
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList: List<User?> = dataSnapshot.children.map { it.getValue(User::class.java) }
                listener?.onChatUsersLoaded(userList)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                listener?.onChatUsersLoaded(null)
            }
        }
        FirebaseDatabase.getInstance().reference.child(CHILD_USERS)
                .addListenerForSingleValueEvent(eventListener)
    }

    override fun resetNewMessagesCount(uuid: String?, chatId: String) {
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val ref = mFirebaseDatabaseReference.child(CHILD_USERS).child(uuid).child(CHILD_NEW_MESSAGES).ref
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages: HashMap<String?, String?> = HashMap()
                newMessages[chatId] = "0"
                mFirebaseDatabaseReference.child(CHILD_USERS).child(uuid).child(CHILD_NEW_MESSAGES).setValue(newMessages)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

}