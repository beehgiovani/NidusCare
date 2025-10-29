package com.developersbeeh.medcontrol.ui.analysis

import android.annotation.SuppressLint // ✅ 1. IMPORTE A ANOTAÇÃO
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.AnalysisHistory
import com.developersbeeh.medcontrol.databinding.AnalysisHistoryItemBinding
import java.time.format.DateTimeFormatter

class AnalysisHistoryAdapter(
    private val onItemClick: (AnalysisHistory) -> Unit
) : ListAdapter<AnalysisHistory, AnalysisHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AnalysisHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: AnalysisHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // ✅ 2. ADICIONE A ANOTAÇÃO AQUI
        // Diz ao linter para ignorar o aviso de "API Nova" apenas para esta linha.
        @SuppressLint("NewApi")
        private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(history: AnalysisHistory) {
            binding.textViewTimestamp.text = history.timestamp.format(formatter)
            binding.textViewSymptoms.text = "Sintomas: \"${history.symptomsPrompt}\""
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AnalysisHistory>() {
        override fun areItemsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AnalysisHistory, newItem: AnalysisHistory): Boolean {
            return oldItem == newItem
        }
    }
}
