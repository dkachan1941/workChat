package com.rainmaker.workchat.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rainmaker.workchat.*
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.HomePresenter
import java.util.HashMap

/**
 * Created by dmitry on 3/14/18.
 *
 */

class ChatsRepository {

    fun getUnReadChats(listener: HomePresenter.RepositoryCallbacks) {
        val mFireBaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val ref = mFireBaseDatabaseReference.child(CHILD_USERS).child(FirebaseAuth.getInstance().currentUser?.uid ?: "").child(CHILD_NEW_MESSAGES).ref
        val typeIndicator = object : GenericTypeIndicator<HashMap<String?, String?>?>() {}

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val res: MutableList<String> = mutableListOf()
                val newChats: HashMap<String?, String?>? = dataSnapshot.getValue(typeIndicator)
                newChats?.asIterable()
                        ?.filter { "0" != it.value }
                        ?.forEach { res.add(it.key ?: "") }
                listener.onUnReadChatsLoaded(res)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onUnReadChatsLoaded(mutableListOf())
            }
        })
    }

    fun getChats(listener: HomePresenter.RepositoryCallbacks, newChats: MutableList<String>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().reference.child(CHILD_ROOMS).ref
                .orderByChild("$CHILD_USERS/$uid")
                .equalTo(FirebaseAuth.getInstance().currentUser?.displayName ?: "")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        val res = mutableListOf<ChatModelNew>()
                        p0?.children?.map {
                            if (newChats.contains(it.key)) {
                                val item = it?.getValue(ChatModelNew::class.java)
                                item?.key = it.key
                                if (item != null) res.add(item)
                            }
                        }
                        listener.onChatsLoaded(res.toMutableList())
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        listener.onChatsLoaded(mutableListOf())
                    }
                })
    }

}