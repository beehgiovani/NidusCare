// src/main/java/com/developersbeeh/medcontrol/ui/chat/ChatAdapter.kt
package com.developersbeeh.medcontrol.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.ChatMessage
import com.developersbeeh.medcontrol.data.model.Sender
import com.developersbeeh.medcontrol.databinding.ItemChatMessageAiBinding
import com.developersbeeh.medcontrol.databinding.ItemChatMessageUserBinding

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (Sender.valueOf(getItem(position).sender)) {
            Sender.USER -> VIEW_TYPE_USER
            Sender.AI -> VIEW_TYPE_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> UserViewHolder(ItemChatMessageUserBinding.inflate(inflater, parent, false))
            VIEW_TYPE_AI -> AiViewHolder(ItemChatMessageAiBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> holder.bind(getItem(position))
            is AiViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class UserViewHolder(private val binding: ItemChatMessageUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.textViewMessage.text = message.text
        }
    }

    inner class AiViewHolder(private val binding: ItemChatMessageAiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            // ✅ LÓGICA CORRIGIDA E ADICIONADA
            if (message.id == "typing_indicator") {
                binding.textViewMessage.visibility = View.GONE
                binding.typingIndicator.visibility = View.VISIBLE
            } else {
                binding.textViewMessage.visibility = View.VISIBLE
                binding.typingIndicator.visibility = View.GONE
                binding.textViewMessage.text = message.text
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}