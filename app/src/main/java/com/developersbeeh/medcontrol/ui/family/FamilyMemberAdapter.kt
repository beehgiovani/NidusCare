package com.developersbeeh.medcontrol.ui.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.databinding.ItemFamilyMemberBinding

class FamilyMemberAdapter(
    private val onRemoveClick: (Usuario) -> Unit,
    private val ownerId: String,
    private val isCurrentUserOwner: Boolean
) : ListAdapter<Usuario, FamilyMemberAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemFamilyMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: Usuario) {
            binding.textViewMemberName.text = member.name
            binding.textViewMemberEmail.text = member.email

            binding.imageViewAvatar.load(member.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }

            // Mostra o chip "Dono(a)" se for o dono da família
            binding.chipOwner.visibility = if (member.id == ownerId) View.VISIBLE else View.GONE

            // Mostra o botão de remover se o usuário atual for o dono E o membro não for ele mesmo
            binding.buttonRemoveMember.visibility = if (isCurrentUserOwner && member.id != ownerId) View.VISIBLE else View.GONE
            binding.buttonRemoveMember.setOnClickListener {
                onRemoveClick(member)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFamilyMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean = oldItem == newItem
    }
}