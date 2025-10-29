package com.developersbeeh.medcontrol.ui.caregiver



import android.view.LayoutInflater

import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView

import com.developersbeeh.medcontrol.data.model.Convite

import com.developersbeeh.medcontrol.databinding.ItemInvitePendingBinding



class PendingInviteAdapter(

    private val onCancelClick: (Convite) -> Unit

) : ListAdapter<Convite, PendingInviteAdapter.ViewHolder>(DiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemInvitePendingBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position))

    }



    inner class ViewHolder(private val binding: ItemInvitePendingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(invite: Convite) {

            val displayText = if (invite.hasDependente()) {

                "Convite enviado para ${invite.destinatarioEmail} sobre ${invite.getDisplayDependenteNome()}"

            } else {

                "Convite enviado para ${invite.destinatarioEmail}"

            }



            binding.textViewInvitedEmail.text = displayText

            binding.buttonCancelInvite.setOnClickListener { onCancelClick(invite) }

        }

    }



    class DiffCallback : DiffUtil.ItemCallback<Convite>() {

        override fun areItemsTheSame(oldItem: Convite, newItem: Convite): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Convite, newItem: Convite): Boolean = oldItem == newItem

    }

}