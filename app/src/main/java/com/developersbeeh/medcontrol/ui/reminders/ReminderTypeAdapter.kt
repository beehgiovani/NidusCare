package com.developersbeeh.medcontrol.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.util.ReminderIconMapper
import com.developersbeeh.medcontrol.databinding.ReminderTypeItemBinding

class ReminderTypeAdapter(
    private val reminderTypes: List<String>,
    private val onTypeSelected: (String) -> Unit
) : RecyclerView.Adapter<ReminderTypeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ReminderTypeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(type: String) {
            binding.textViewType.text = type
            binding.imageViewIcon.setImageResource(ReminderIconMapper.getIconForType(type))
            binding.root.setOnClickListener {
                onTypeSelected(type)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReminderTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = reminderTypes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reminderTypes[position])
    }
}