package com.developersbeeh.medcontrol.ui.dosehistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.databinding.DoseHistoryItemBinding
import com.developersbeeh.medcontrol.databinding.ItemTimelineDateHeaderBinding
import com.developersbeeh.medcontrol.util.CalculatedDoseStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

sealed class DoseHistoryListItem {
    data class DoseItem(val dose: DoseHistory, val medName: String, val calculatedStatus: CalculatedDoseStatus) : DoseHistoryListItem()
    data class HeaderItem(val date: LocalDate) : DoseHistoryListItem()
}

class DoseHistoryAdapter :
    PagingDataAdapter<DoseHistoryListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_DOSE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DoseHistoryListItem.HeaderItem -> VIEW_TYPE_HEADER
            is DoseHistoryListItem.DoseItem -> VIEW_TYPE_DOSE
            null -> throw IllegalStateException("Item nulo em uma posição válida")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemTimelineDateHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_DOSE -> DoseViewHolder(DoseHistoryItemBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Tipo de view desconhecido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                is DoseHistoryListItem.HeaderItem -> (holder as HeaderViewHolder).bind(it)
                is DoseHistoryListItem.DoseItem -> (holder as DoseViewHolder).bind(it)
            }
        }
    }

    class HeaderViewHolder(private val binding: ItemTimelineDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR"))
        fun bind(header: DoseHistoryListItem.HeaderItem) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val dateText = when (header.date) {
                today -> "Hoje - ${header.date.format(dateFormatter)}"
                yesterday -> "Ontem - ${header.date.format(dateFormatter)}"
                else -> {
                    val dayOfWeek = header.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                        .replaceFirstChar { it.uppercase() }
                    "$dayOfWeek - ${header.date.format(dateFormatter)}"
                }
            }
            binding.textViewDate.text = dateText
        }
    }

    class DoseViewHolder(private val binding: DoseHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun bind(item: DoseHistoryListItem.DoseItem) {
            val dose = item.dose
            val context = binding.root.context
            val time = dose.timestamp.format(timeFormatter)

            val detailsText = StringBuilder(item.medName)
            dose.quantidadeAdministrada?.let { detailsText.append(" - $it") }

            // Altera o texto principal se a dose foi pulada
            if (item.calculatedStatus == CalculatedDoseStatus.SKIPPED) {
                binding.textViewDoseDetails.text = "$time - Dose de ${item.medName} foi pulada"
            } else {
                binding.textViewDoseDetails.text = "$time - $detailsText"
            }

            // Exibe notas (motivo de pular/adiantar) ou local de aplicação
            val extraInfo = when {
                !dose.notas.isNullOrBlank() -> "Nota: ${dose.notas}"
                !dose.localDeAplicacao.isNullOrBlank() -> "Local: ${dose.localDeAplicacao}"
                else -> null
            }

            if (extraInfo != null) {
                binding.textViewDoseExtraInfo.text = extraInfo
                binding.textViewDoseExtraInfo.visibility = View.VISIBLE
            } else {
                binding.textViewDoseExtraInfo.visibility = View.GONE
            }

            // ✅ CORREÇÃO: Adicionado o caso 'SKIPPED'
            val (iconRes, tintColor) = when (item.calculatedStatus) {
                CalculatedDoseStatus.ON_TIME -> R.drawable.ic_check_circle to R.color.success_green
                CalculatedDoseStatus.TOO_EARLY -> R.drawable.ic_arrow_upward to R.color.error_red
                CalculatedDoseStatus.TOO_LATE -> R.drawable.ic_arrow_downward to R.color.warning_orange
                CalculatedDoseStatus.EXTRA -> R.drawable.ic_add_alert to R.color.error_red
                CalculatedDoseStatus.SKIPPED -> R.drawable.ic_skip to R.color.md_theme_onSurfaceVariant
                CalculatedDoseStatus.SPORADIC -> R.drawable.ic_info to R.color.md_theme_onSurfaceVariant
            }

            binding.iconStatus.setImageResource(iconRes)
            binding.iconStatus.setColorFilter(ContextCompat.getColor(context, tintColor))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DoseHistoryListItem>() {
        override fun areItemsTheSame(oldItem: DoseHistoryListItem, newItem: DoseHistoryListItem): Boolean {
            return (oldItem is DoseHistoryListItem.DoseItem && newItem is DoseHistoryListItem.DoseItem && oldItem.dose.id == newItem.dose.id) ||
                    (oldItem is DoseHistoryListItem.HeaderItem && newItem is DoseHistoryListItem.HeaderItem && oldItem.date == newItem.date)
        }

        override fun areContentsTheSame(oldItem: DoseHistoryListItem, newItem: DoseHistoryListItem): Boolean {
            return oldItem == newItem
        }
    }
}