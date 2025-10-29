package com.developersbeeh.medcontrol.ui.dashboard

import android.R.attr.colorPrimary
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.ItemDashboardCategoryBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors

data class DashboardCategory(
    val title: String,
    val iconRes: Int,
    val actionId: Int
)

class DashboardCategoryAdapter(
    private val categories: List<DashboardCategory>,
    private val onItemClick: (DashboardCategory) -> Unit
) : RecyclerView.Adapter<DashboardCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDashboardCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: DashboardCategory) {
            binding.imageViewCategoryIcon.setImageResource(category.iconRes)
            binding.textViewCategoryTitle.text = category.title
            binding.root.setOnClickListener {
                onItemClick(category)
            }

            val context = binding.root.context
            // ✅ ROBUSTEZ: Corrigido para usar o atributo correto do tema
            var iconTint = MaterialColors.getColor(binding.root, colorPrimary, Color.BLACK)
            var textColor = MaterialColors.getColor(binding.root, MaterialR.attr.colorOnSurface, Color.BLACK)

            when (category.actionId) {
                R.id.action_global_to_premiumPlansFragment -> {
                    iconTint = ContextCompat.getColor(context, R.color.premium_gold)
                    textColor = ContextCompat.getColor(context, R.color.premium_gold)
                }
                ACTION_ID_EMERGENCY -> {
                    iconTint = ContextCompat.getColor(context, R.color.md_theme_error)
                    textColor = ContextCompat.getColor(context, R.color.md_theme_error)
                }
                ACTION_ID_DELETE_DEPENDENT -> {
                    iconTint = ContextCompat.getColor(context, R.color.md_theme_error)
                    textColor = ContextCompat.getColor(context, R.color.md_theme_error)
                }
            }
            binding.imageViewCategoryIcon.setColorFilter(iconTint)
            binding.textViewCategoryTitle.setTextColor(textColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }
}