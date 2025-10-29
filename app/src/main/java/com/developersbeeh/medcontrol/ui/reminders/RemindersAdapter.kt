package com.developersbeeh.medcontrol.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.Reminder
import com.developersbeeh.medcontrol.databinding.ReminderItemBinding
import java.time.format.DateTimeFormatter

class RemindersAdapter(
    private val onEditClick: (Reminder) -> Unit, // <-- ADICIONADO
    private val onDeleteClick: (Reminder) -> Unit,
    private val onToggleClick: (Reminder) -> Unit
) : ListAdapter<Reminder, RemindersAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReminderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ReminderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.textViewReminderType.text = reminder.type
            binding.textViewReminderTime.text = reminder.time.format(DateTimeFormatter.ofPattern("HH:mm"))

            // Remove o listener temporariamente para evitar chamadas recursivas ao setar o estado
            binding.switchReminder.setOnCheckedChangeListener(null)
            binding.switchReminder.isChecked = reminder.isActive
            binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
                onToggleClick(reminder.copy(isActive = isChecked))
            }

            binding.imageViewDelete.setOnClickListener {
                onDeleteClick(reminder)
            }

            // AÇÃO DE CLIQUE NO BOTÃO DE EDITAR
            binding.imageViewEdit.setOnClickListener {
                onEditClick(reminder)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}