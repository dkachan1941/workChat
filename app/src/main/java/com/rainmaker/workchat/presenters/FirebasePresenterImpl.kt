package com.rainmaker.workchat.presenters

import android.content.res.Resources
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rainmaker.workchat.*
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
        val mFireBaseDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        mFireBaseDatabaseReference.child(uuid).setValue(user)
    }

    override fun createChat(text: String) {
        val fireBaseUser = FirebaseAuth.getInstance().currentUser
        if (fireBaseUser != null){
            val users = HashMap<String, String>()
            users[fireBaseUser.uid] = fireBaseUser.displayName ?: ""
            val newRoom = ChatModel(text, 0, Date().toString(), fireBaseUser.uid, users)
            FirebaseDatabase.getInstance()?.reference?.child(CHILD_ROOMS)?.push()?.setValue(newRoom)
        } else {
            Log.d(TAG, resources.getString(R.string.err_error_creating_chat))
        }
    }
}