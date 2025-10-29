package com.developersbeeh.medcontrol.ui.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.AtividadeFisica
import com.developersbeeh.medcontrol.databinding.ItemActivityHistoryBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActivityHistoryAdapter : ListAdapter<AtividadeFisica, ActivityHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActivityHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActivityHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        fun bind(record: AtividadeFisica) {
            val context = binding.root.context
            binding.textViewType.text = record.tipo
            binding.textViewDuration.text = context.getString(R.string.activity_log_suffix, record.duracaoMinutos)
            binding.textViewTime.text = record.timestamp.format(timeFormatter)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AtividadeFisica>() {
        override fun areItemsTheSame(oldItem: AtividadeFisica, newItem: AtividadeFisica): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AtividadeFisica, newItem: AtividadeFisica): Boolean {
            return oldItem == newItem
        }
    }
}