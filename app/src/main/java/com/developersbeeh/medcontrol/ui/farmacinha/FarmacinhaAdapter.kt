package com.developersbeeh.medcontrol.ui.farmacinha

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.ItemFarmacinhaBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FarmacinhaAdapter(
    private val onTomarDoseClick: (FarmacinhaItem) -> Unit,
    private val onReporEstoqueClick: (FarmacinhaItem) -> Unit,
    private val onAdicionarPosologiaClick: (FarmacinhaItem) -> Unit,
    private val onDeleteClick: (FarmacinhaItem) -> Unit // ✅ NOVO CALLBACK ADICIONADO
) : ListAdapter<FarmacinhaItem, FarmacinhaAdapter.ViewHolder>(DiffCallback()) {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFarmacinhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    inner class ViewHolder(private val binding: ItemFarmacinhaBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            val clickListener = View.OnClickListener {
                toggleDetailsVisibility(bindingAdapterPosition)
            }
            binding.root.setOnClickListener(clickListener)
            binding.imageViewExpandCollapse.setOnClickListener(clickListener)
        }

        private fun toggleDetailsVisibility(position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            val isCurrentlyExpanded = position == expandedPosition

            val oldExpanded = expandedPosition
            expandedPosition = if (isCurrentlyExpanded) -1 else position

            if (oldExpanded != -1) {
                notifyItemChanged(oldExpanded)
            }
            if (expandedPosition != -1) {
                notifyItemChanged(expandedPosition)
            }
        }

        fun bind(item: FarmacinhaItem, isExpanded: Boolean) {
            val context = binding.root.context
            binding.textViewMedicamentoNome.text = item.medicamento.nome
            binding.textViewDependenteNome.text = "Para: ${item.dependenteNome}"
            binding.textViewEstoqueTotal.text = "${item.medicamento.estoqueAtualTotal} ${item.medicamento.unidadeDeEstoque}"

            binding.actionsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.imageViewExpandCollapse.rotation = if (isExpanded) 180f else 0f

            val loteMaisProximo = item.medicamento.lotes
                .filter { it.dataValidade.isAfter(LocalDate.now().minusDays(1)) }
                .minByOrNull { it.dataValidade }

            if (loteMaisProximo != null) {
                val dataFormatada = loteMaisProximo.dataValidade.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                binding.textViewValidadeProxima.text = dataFormatada

                when {
                    loteMaisProximo.dataValidade.isBefore(LocalDate.now()) -> {
                        binding.textViewValidadeProxima.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                    }
                    loteMaisProximo.dataValidade.isBefore(LocalDate.now().plusDays(30)) -> {
                        binding.textViewValidadeProxima.setTextColor(ContextCompat.getColor(context, R.color.warning_orange))
                    }
                    else -> {
                        binding.textViewValidadeProxima.setTextColor(ContextCompat.getColor(context, R.color.success_green))
                    }
                }
            } else {
                binding.textViewValidadeProxima.text = "Sem lotes válidos"
                binding.textViewValidadeProxima.setTextColor(ContextCompat.getColor(context, R.color.error_red))
            }

            binding.buttonTomarDose.setOnClickListener { onTomarDoseClick(item) }
            binding.buttonReporEstoque.setOnClickListener { onReporEstoqueClick(item) }
            binding.buttonAdicionarPosologia.setOnClickListener { onAdicionarPosologiaClick(item) }
            binding.buttonExcluir.setOnClickListener { onDeleteClick(item) } // ✅ NOVO LISTENER ADICIONADO
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FarmacinhaItem>() {
        override fun areItemsTheSame(oldItem: FarmacinhaItem, newItem: FarmacinhaItem): Boolean {
            return oldItem.medicamento.id == newItem.medicamento.id
        }

        override fun areContentsTheSame(oldItem: FarmacinhaItem, newItem: FarmacinhaItem): Boolean {
            return oldItem == newItem
        }
    }
}