package com.rainmaker.workchat

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.rainmaker.workchat.activities.ChatActivity
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Adapter for chats list
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
        private val badgeUnRedMessageCount = itemView?.findViewById<TextView>(R.id.badgeUnRedMessageCount)

        fun bind(item: ChatModel1) = with(itemView) {
            chatName?.text = item.name
            if (item.messageCount ?: 0 > 0) {
                badgeUnRedMessageCount?.text = item.messageCount?.toString()
                badgeUnRedMessageCount?.visibility = View.VISIBLE
            } else {
                badgeUnRedMessageCount?.visibility = View.GONE
            }
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

/**
 * Adapter for Users
 */
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

/**
 * Adapter for Chat
 */
class ChatFireBaseAdapter(private var context: Context, private var listener: ChatAdapterListener, options: FirebaseRecyclerOptions<MessageModel>,
                          private var currentUser: FirebaseUser?): FirebaseRecyclerAdapter<MessageModel,
        ChatFireBaseAdapter.MessageViewHolder>(options) {

    interface ChatAdapterListener{
        fun onChatAdapterFirstItemLoaded()
        fun onChatAdapterItemClick(item: MessageModel)
        fun onChatAdapterItemLongClick(item: MessageModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatFireBaseAdapter.MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = ChatFireBaseAdapter.MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false))
        viewHolder.setOnClickListener(object : ChatFireBaseAdapter.MessageViewHolder.ClickListener {
            override fun onItemClick(view: View, position: Int) {
                listener.onChatAdapterItemClick(getItem(position))
            }
            override fun onItemLongClick(view: View, position: Int) {
                listener.onChatAdapterItemLongClick(getItem(position))
            }
        })
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ChatFireBaseAdapter.MessageViewHolder,
                                  position: Int,
                                  curMessage: MessageModel) {
        listener.onChatAdapterFirstItemLoaded()
        setMessageColor(viewHolder.messageLayout, currentUser, curMessage.userUid)
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
                        Log.w(TAG, context.getString(R.string.err_making_request), task.exception)
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
            viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_account_circle_black_36dp))
        } else {
            Glide.with(context)
                    .load(curMessage.photoUrl)
                    .into(viewHolder.messengerImageView)
        }
    }

    private fun setMessageColor(messageLayout: android.support.v7.widget.CardView, currentUser: com.google.firebase.auth.FirebaseUser?, userUid: kotlin.String?) {
        when {
            currentUser == null -> messageLayout.setBackgroundColor(context.resources.getColor(FOREIGN_MESSAGE_COLOR)) // todo
            currentUser.uid == userUid -> messageLayout.setBackgroundColor(context.resources.getColor(MY_MESSAGE_COLOR))
            else -> messageLayout.setBackgroundColor(context.resources.getColor(FOREIGN_MESSAGE_COLOR))
        }
    }

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

}
