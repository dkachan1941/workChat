package com.rainmaker.workchat

import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by dmitry on 2/3/18.
 */

class ChatsAdapter(var chatList: ArrayList<ChatModel1?>) : RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    fun setData(chatList: ArrayList<ChatModel1?>){
        this.chatList = chatList
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ChatsViewHolder {
        val viewHolder = ChatsViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_chat, parent, false))
        return viewHolder
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatsViewHolder?, position: Int): Unit = holder?.bind(chatList[position]!!)!!

//    override fun onBindViewHolder(holder: ChatsViewHolder?, position: Int) {
//        holder?.txtName?.text = userList[position].name
//        holder?.txtTitle?.text = userList[position].title
//    }

    class ChatsViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val chatName = itemView?.findViewById<TextView>(R.id.chatName)
        val lastMessageFrom = itemView?.findViewById<TextView>(R.id.lastMessageFrom)
//        fun bind(item: ChatModel, listener: (ChatModel) -> Unit) = with(itemView) {
        fun bind(item: ChatModel1) = with(itemView) {
            chatName?.text = item.name
            lastMessageFrom?.text = item.messageCount.toString()
            setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("chatId", item.key)
                startActivity(itemView.context, intent, null)
            }
        }
    }
}