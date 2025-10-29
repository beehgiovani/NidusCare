package com.developersbeeh.medcontrol.ui.hydration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Hidratacao
import com.developersbeeh.medcontrol.databinding.ItemHydrationHistoryBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class HydrationHistoryAdapter : ListAdapter<Hidratacao, HydrationHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHydrationHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHydrationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        fun bind(record: Hidratacao) {
            val context = binding.root.context
            // ✅ REATORADO: Usa strings.xml
            binding.textViewAmount.text = "${context.getString(R.string.hydration_log_prefix)}${record.quantidadeMl} ${context.getString(R.string.hydration_log_suffix)}"
            binding.textViewTime.text = record.timestamp.format(timeFormatter)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Hidratacao>() {
        override fun areItemsTheSame(oldItem: Hidratacao, newItem: Hidratacao): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Hidratacao, newItem: Hidratacao): Boolean {
            return oldItem == newItem
        }
    }
}