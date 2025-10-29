package com.developersbeeh.medcontrol.ui.settings

import android.R.attr.colorPrimary
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.ItemSettingBinding
import com.google.android.material.color.MaterialColors

data class SettingItem(
    val id: Int, // Usado para o 'when' no clique
    val iconRes: Int,
    val title: String,
    val subtitle: String
)

class SettingsAdapter(
    private val onItemClick: (SettingItem) -> Unit
) : ListAdapter<SettingItem, SettingsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root) { // ✅ CORREÇÃO: Usa binding.root

        fun bind(item: SettingItem) {
            binding.imageViewIcon.setImageResource(item.iconRes)
            binding.textViewTitle.text = item.title
            binding.textViewSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onItemClick(item) }

            // Lógica para colorir itens especiais
            val context = binding.root.context
            if (item.id == 5) { // Sair da Conta
                binding.imageViewIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_error))
                binding.textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.md_theme_error))
            } else if (item.id == 7) { // Upgrade Premium
                binding.imageViewIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.premium_gold))
                binding.textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.premium_gold))
            }
            else {
                // Restaura as cores padrão para outros itens
                binding.imageViewIcon.imageTintList = ColorStateList.valueOf(MaterialColors.getColor(context, colorPrimary, Color.BLACK))
                binding.textViewTitle.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SettingItem>() {
        override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem == newItem
        }
    }
}