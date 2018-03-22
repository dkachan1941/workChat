package com.rainmaker.workchat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rainmaker.workchat.*
import com.rainmaker.workchat.presenters.ChatsPresenter
import com.rainmaker.workchat.presenters.HomePresenter
import java.util.HashMap

/**
 * Created by dmitry on 3/14/18.
 *
 */

open class ChatsRepositoryImpl: ChatsRepository {

    override fun getNewMessagesCountForChats(listener: ChatsPresenter.RepositoryCallbacks) {
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        val ref = mFirebaseDatabaseReference.child(CHILD_USERS).child(FirebaseAuth.getInstance()?.currentUser?.uid ?: "").child(CHILD_NEW_MESSAGES).ref
        val ti = object : GenericTypeIndicator<HashMap<String?, String?>?>() {}
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newMessages: HashMap<String?, String?>? = dataSnapshot.getValue(ti)
                if (newMessages != null) {
                    listener.onNewMessagesCountForChatLoaded(newMessages)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun getPublicChats(listener: ChatsPresenter.RepositoryCallbacks) {
        FirebaseDatabase.getInstance()?.reference
                ?.child(CHILD_ROOMS)?.ref?.orderByChild(FIELD_IS_PUBLIC)?.equalTo(false)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {

                        val res = mutableListOf<ChatModelNew>()
                        p0?.children?.map {
                            val item = it?.getValue(ChatModelNew::class.java)
                            item?.key = it.key
                            if (item != null) res.add(item)
                        }
                        listener.onChatsLoaded(res.toMutableList())
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        listener.onChatsLoaded(mutableListOf())
                    }
                })
    }

    override fun getPrivateChats(listener: ChatsPresenter.RepositoryCallbacks) {
        FirebaseDatabase.getInstance().reference.child(CHILD_ROOMS).ref
                .orderByChild("$CHILD_USERS/${FirebaseAuth.getInstance()?.currentUser?.uid}")
                .equalTo(FirebaseAuth.getInstance().currentUser?.displayName)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {

                        val res = mutableListOf<ChatModelNew>()
                        p0?.children?.map {
                            val item = it?.getValue(ChatModelNew::class.java)
                            item?.key = it.key
                            if (item != null && item.isPrivate == true) res.add(item)
                        }
                        listener.onChatsLoaded(res.toMutableList())
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        listener.onChatsLoaded(mutableListOf())
                    }
                })
    }

    override fun getUnReadChats(listener: HomePresenter.RepositoryCallbacks) {
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

    override fun getChats(listener: HomePresenter.RepositoryCallbacks, newChats: MutableList<String>) {
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