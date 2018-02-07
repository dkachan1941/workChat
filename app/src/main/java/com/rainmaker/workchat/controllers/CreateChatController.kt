package com.rainmaker.workchat.controllers

import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rainmaker.workchat.activities.ChatActivity
import com.rainmaker.workchat.ChatModel

import com.rainmaker.workchat.R
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by dmitry on 1/29/18.
 */
class CreateChatController : Controller() {

    companion object {
        const val TAG_ROUTER = "CreateChatController"
    }

    lateinit var mFirebaseDatabaseReference: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val mView = inflater.inflate(R.layout.controller_create_chat, container, false)
        val createChatButton = mView.findViewById<Button>(R.id.buttonCreateChat)
        val chatNameEditText = mView.findViewById<TextInputLayout>(R.id.textInputLayoutChatName)

        createChatButton.setOnClickListener {
            createChat(chatNameEditText.editText?.text.toString())
        }

        // init db
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        return mView
    }

    private fun createChat(text: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        val users = HashMap<String, String>()
        users[firebaseUser ?: ""] = "true"
        users[firebaseUser+"sdfdsfdsf" ?: ""] = "true"
        val newRoom = ChatModel(text, 0, Date().toString(), firebaseUser!!, users)
        mFirebaseDatabaseReference.child(ChatActivity.ROOMS_CHILD).push().setValue(newRoom)
        router.pushController(RouterTransaction.with(ChatRoomsController()))
    }
}