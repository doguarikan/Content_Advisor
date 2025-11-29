package com.example.content_advisor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private var dataSet: Array<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.user_message_text)
    }

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.bot_message_text)
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position].startsWith("USER:")) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user_message, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_bot_message, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = dataSet[position]

        if (holder is UserViewHolder) {
            holder.textView.text = message.removePrefix("USER: ")
        } else if (holder is BotViewHolder) {
            holder.textView.text = message.removePrefix("BOT: ")
        }
    }

    override fun getItemCount() = dataSet.size

    fun updateMessages(newMessages: Array<String>) {
        dataSet = newMessages
        notifyDataSetChanged()
    }
}