package com.developersbeeh.medcontrol.ui.caregiver

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
import com.developersbeeh.medcontrol.databinding.ItemCaregiverBinding


class CaregiverAdapter(
    private val currentUserId: String,
    private val onRemoveClick: (Usuario) -> Unit
) : ListAdapter<Usuario, CaregiverAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCaregiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCaregiverBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(caregiver: Usuario) {
            binding.textViewCaregiverName.text = caregiver.name
            binding.textViewCaregiverEmail.text = caregiver.email

            binding.imageViewAvatar.load(caregiver.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }

            // O cuidador não pode remover a si mesmo
            if (caregiver.id == currentUserId) {
                binding.buttonRemoveCaregiver.visibility = View.GONE
            } else {
                binding.buttonRemoveCaregiver.visibility = View.VISIBLE
                binding.buttonRemoveCaregiver.setOnClickListener {
                    onRemoveClick(caregiver)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem == newItem
        }
    }
}