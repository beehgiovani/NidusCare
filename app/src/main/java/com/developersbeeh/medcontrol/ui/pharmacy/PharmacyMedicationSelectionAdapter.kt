// src/main/java/com/developersbeeh/medcontrol/ui/pharmacy/PharmacyMedicationSelectionAdapter.kt
package com.developersbeeh.medcontrol.ui.pharmacy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.databinding.ItemPharmacyMedicationSelectionBinding

class PharmacyMedicationSelectionAdapter(
    private val onMedicationSelected: (SelectableMedication) -> Unit
) : ListAdapter<SelectableMedication, PharmacyMedicationSelectionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPharmacyMedicationSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPharmacyMedicationSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onMedicationSelected(getItem(bindingAdapterPosition))
                }
            }
        }

        fun bind(item: SelectableMedication) {
            binding.textViewMedicationName.text = item.medicationName
            binding.textViewDependents.text = "Para: ${item.dependentNames.joinToString(", ")}"
            binding.textViewStockLevel.text = "${item.totalStock} ${item.unit}"
            binding.checkboxMedication.isChecked = item.isSelected
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectableMedication>() {
        override fun areItemsTheSame(oldItem: SelectableMedication, newItem: SelectableMedication): Boolean {
            return oldItem.medicationName == newItem.medicationName
        }

        override fun areContentsTheSame(oldItem: SelectableMedication, newItem: SelectableMedication): Boolean {
            return oldItem == newItem
        }
    }
}