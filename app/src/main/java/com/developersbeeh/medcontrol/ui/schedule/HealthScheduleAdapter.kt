// src/main/java/com/developersbeeh/medcontrol/ui/schedule/HealthScheduleAdapter.kt
package com.developersbeeh.medcontrol.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.databinding.ItemHealthScheduleBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class HealthScheduleAdapter(
    private val onItemClick: (AgendamentoSaude) -> Unit,
    private val onMenuClick: (AgendamentoSaude, View) -> Unit
) : ListAdapter<AgendamentoSaude, HealthScheduleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHealthScheduleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(agendamento: AgendamentoSaude) {
            val dateTime = agendamento.timestamp
            // Formata o mês em maiúsculas (ex: "OUT")
            val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("pt", "BR"))
            binding.textViewMonth.text = dateTime.format(monthFormatter).uppercase()
            binding.textViewDay.text = dateTime.dayOfMonth.toString()
            binding.textViewScheduleTitle.text = agendamento.titulo

            val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            val location = agendamento.local
            // Combina horário e local em uma única linha
            binding.textViewScheduleTimeAndPlace.text = if (location.isNullOrBlank()) time else "$time - $location"

            // Mostra o nome do profissional apenas se ele existir
            binding.textViewScheduleDoctor.isVisible = !agendamento.profissional.isNullOrBlank()
            binding.textViewScheduleDoctor.text = agendamento.profissional

            binding.root.setOnClickListener { onItemClick(agendamento) }
            binding.buttonMenu.setOnClickListener { onMenuClick(agendamento, it) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AgendamentoSaude>() {
        override fun areItemsTheSame(oldItem: AgendamentoSaude, newItem: AgendamentoSaude): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: AgendamentoSaude, newItem: AgendamentoSaude): Boolean {
            return oldItem == newItem
        }
    }
}