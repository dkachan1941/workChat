package com.rainmaker.workchat

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rainmaker.workchat.activities.ChatActivity

/**
 * Created by dmitry on 2/3/18.
 *
 */

class ChatsAdapter(var chatList: ArrayList<ChatModel1?>) : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    fun setData(chatList: ArrayList<ChatModel1?>){
        this.chatList = chatList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_chat, parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatsViewHolder?, position: Int): Unit = holder?.bind(chatList[position]!!)!!

    class ChatsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private val chatName = itemView?.findViewById<TextView>(R.id.chatName)
        private val lastMessageFrom = itemView?.findViewById<TextView>(R.id.lastMessageFrom)
        private val badgeMessageCount = itemView?.findViewById<TextView>(R.id.badgeMessageCount)

        fun bind(item: ChatModel1) = with(itemView) {
            chatName?.text = item.name
            setChatParams(context, badgeMessageCount, lastMessageFrom, item.key)
            setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra(CHAT_ID, item.key)
                intent.putExtra(CHAT_NAME, item.name)
                startActivity(itemView.context, intent, null)
            }
        }

        private fun setChatParams(context: Context?, textViewMessageCount: TextView?, textViewLastMessageFrom: TextView?, key: String?) {
            textViewMessageCount?.visibility = View.GONE
            textViewLastMessageFrom?.visibility = View.GONE
            FirebaseDatabase.getInstance().reference?.child(CHILD_ROOMS)?.child(key)?.child(CHILD_MESSAGES)
                    ?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.childrenCount > 0){

                                // find message count
                                textViewMessageCount?.text = dataSnapshot.childrenCount.toString()
                                textViewMessageCount?.visibility = View.VISIBLE

                                // find latest message author
                                val userList: List<MessageModel?> = dataSnapshot.children.map { it.getValue(MessageModel::class.java) }
                                if (userList.isNotEmpty()){
                                    textViewLastMessageFrom?.visibility = View.VISIBLE
                                    textViewLastMessageFrom?.text = String.format(context!!.resources.getString(R.string.latest_msg_from), userList[userList.size - 1]?.name)
                                }

                            } else {
                                textViewMessageCount?.visibility = View.GONE
                                textViewLastMessageFrom?.visibility = View.GONE
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
        }
    }
}

class UsersAdapter(var chatList: ArrayList<User?>, private val listener: (User) -> Unit) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UsersViewHolder {
        return UsersViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_user, parent, false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder?, position: Int) = holder?.bind(chatList[position]!!, listener)!!

    class UsersViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private val userName = itemView?.findViewById<TextView>(R.id.userName)
        private val userEmail = itemView?.findViewById<TextView>(R.id.userEmail)
        fun bind(item: User, listener: (User) -> Unit) = with(itemView) {
            userName?.text = item.name
            userEmail?.text = item.email
            setOnClickListener {
                listener(item)
            }
        }
    }
}