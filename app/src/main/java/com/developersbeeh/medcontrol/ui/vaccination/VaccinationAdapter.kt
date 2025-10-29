package com.developersbeeh.medcontrol.ui.vaccination

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.ItemVaccineBinding
import com.developersbeeh.medcontrol.databinding.ItemVaccineGroupBinding
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

sealed class VaccinationListItem {
    data class GroupHeader(val ageInMonths: Int) : VaccinationListItem()
    data class VaccineItem(val uiItem: VacinaUiItem) : VaccinationListItem()
}

class VaccinationAdapter(
    private val onRegisterClick: (VacinaUiItem) -> Unit
) : ListAdapter<VaccinationListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is VaccinationListItem.GroupHeader -> VIEW_TYPE_HEADER
            is VaccinationListItem.VaccineItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> GroupViewHolder(ItemVaccineGroupBinding.inflate(inflater, parent, false))
            VIEW_TYPE_ITEM -> ItemViewHolder(ItemVaccineBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is VaccinationListItem.GroupHeader -> (holder as GroupViewHolder).bind(item)
            is VaccinationListItem.VaccineItem -> (holder as ItemViewHolder).bind(item)
        }
    }

    inner class GroupViewHolder(private val binding: ItemVaccineGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: VaccinationListItem.GroupHeader) {
            binding.textViewVaccineGroupTitle.text = when (header.ageInMonths) {
                0 -> "Ao Nascer"
                1 -> "Aos 1 Mês"
                else -> "Aos ${header.ageInMonths} Meses"
            }
        }
    }

    inner class ItemViewHolder(private val binding: ItemVaccineBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        fun bind(item: VaccinationListItem.VaccineItem) {
            val uiItem = item.uiItem
            val context = binding.root.context
            binding.textViewVaccineName.text = uiItem.vacina.nome
            binding.textViewVaccineDose.text = uiItem.vacina.dose
            binding.textViewPrevents.text = "Previne: ${uiItem.vacina.previne}"
            binding.buttonRegister.setOnClickListener { onRegisterClick(uiItem) }

            // ✅ CORREÇÃO: Converte o LocalDateTime para Date para usar com o SimpleDateFormat.
            val applicationDate = uiItem.registro?.timestamp?.let { Date.from(it.atZone(ZoneId.systemDefault()).toInstant()) }

            when (uiItem.status) {
                VacinaStatus.TOMADA -> setupStatus(context.getString(R.string.vaccine_taken), R.color.success_green, false, applicationDate)
                VacinaStatus.ATRASADA -> setupStatus(context.getString(R.string.vaccine_late), R.color.error_red, true)
                VacinaStatus.PROXIMA -> setupStatus(context.getString(R.string.vaccine_next), R.color.warning_orange, true)
                VacinaStatus.OK -> setupStatus(context.getString(R.string.vaccine_ok), R.color.md_theme_onSurfaceVariant, false)
            }
        }

        private fun setupStatus(text: String, colorRes: Int, showRegisterButton: Boolean, date: Date? = null) {
            val context = binding.root.context
            binding.textViewStatus.text = text
            binding.textViewStatus.backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
            binding.buttonRegister.visibility = if (showRegisterButton) View.VISIBLE else View.GONE

            if (date != null) {
                binding.textViewDateTaken.visibility = View.VISIBLE
                binding.textViewDateTaken.text = "Aplicada em: ${dateFormatter.format(date)}"
            } else {
                binding.textViewDateTaken.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<VaccinationListItem>() {
        override fun areItemsTheSame(oldItem: VaccinationListItem, newItem: VaccinationListItem): Boolean {
            return if (oldItem is VaccinationListItem.VaccineItem && newItem is VaccinationListItem.VaccineItem) {
                oldItem.uiItem.vacina.id == newItem.uiItem.vacina.id
            } else if (oldItem is VaccinationListItem.GroupHeader && newItem is VaccinationListItem.GroupHeader) {
                oldItem.ageInMonths == newItem.ageInMonths
            } else {
                false
            }
        }
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: VaccinationListItem, newItem: VaccinationListItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
}