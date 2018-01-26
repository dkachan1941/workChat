package com.rainmaker.workchat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.rainmaker.workchat.ChatActivity.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList


class NewChatActivity : AppCompatActivity() {

    lateinit var mFirebaseDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)

        val createChatButton = findViewById<Button>(R.id.buttonCreateChat)
//        createChatButton.setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        createChatButton.setOnClickListener {
            createChat(findViewById < TextInputLayout > (R.id.textInputLayoutChatName).editText?.text.toString())
        }

        // init db
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun createChat(text: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser?.uid
        val newRoom = ChatModel(text, 0, Date().toString(), firebaseUser!!, arrayListOf(firebaseUser))
        mFirebaseDatabaseReference.child(ROOMS_CHILD).push().setValue(newRoom)
        finish()
    }
}
