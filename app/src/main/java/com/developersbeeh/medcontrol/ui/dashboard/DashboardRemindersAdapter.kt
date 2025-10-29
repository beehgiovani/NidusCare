package com.developersbeeh.medcontrol.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.Reminder
import com.developersbeeh.medcontrol.databinding.ItemDashboardReminderBinding
import com.developersbeeh.medcontrol.util.ReminderIconMapper
import java.time.format.DateTimeFormatter

class DashboardRemindersAdapter : ListAdapter<Reminder, DashboardRemindersAdapter.ViewHolder>(DiffCallback()) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDashboardReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.textViewTime.text = reminder.time.format(timeFormatter)
            binding.textViewType.text = reminder.type
            binding.imageViewIcon.setImageResource(ReminderIconMapper.getIconForType(reminder.type))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}