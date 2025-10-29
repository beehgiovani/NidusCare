package com.developersbeeh.medcontrol.ui.caregiver



import android.view.LayoutInflater

import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView

import com.developersbeeh.medcontrol.data.model.Convite

import com.developersbeeh.medcontrol.databinding.ItemInviteReceivedBinding



class ReceivedInviteAdapter(

    private val onAcceptClick: (Convite) -> Unit,

    private val onDeclineClick: (Convite) -> Unit,



    ) : ListAdapter<Convite, ReceivedInviteAdapter.ViewHolder>(DiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemInviteReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position))

    }



    inner class ViewHolder(private val binding: ItemInviteReceivedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(invite: Convite) {

            val inviteText = "${invite.remetenteNome} convidou você para cuidar de ${invite.getDisplayDependenteNome()}."

            binding.textViewInviteText.text = inviteText



            binding.buttonAccept.setOnClickListener { onAcceptClick(invite) }

            binding.buttonDecline.setOnClickListener { onDeclineClick(invite) }



        }

    }



    class DiffCallback : DiffUtil.ItemCallback<Convite>() {

        override fun areItemsTheSame(oldItem: Convite, newItem: Convite): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Convite, newItem: Convite): Boolean = oldItem == newItem

    }

}
