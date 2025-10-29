// src/main/java/com/developersbeeh/medcontrol/ui/insights/InsightsAdapter.kt
package com.developersbeeh.medcontrol.ui.insights

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
import com.developersbeeh.medcontrol.data.model.Insight
import com.developersbeeh.medcontrol.databinding.ItemInsightBinding
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Locale

class InsightsAdapter : ListAdapter<Insight, InsightsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInsightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInsightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("'Gerado em' dd/MM/yyyy", Locale.getDefault())

        fun bind(insight: Insight) {
            binding.textViewTitle.text = insight.title
            binding.textViewDescription.text = insight.description

            // ✅ CORREÇÃO APLICADA AQUI
            // Converte o Timestamp do Firebase para um objeto Date antes de formatar.
            binding.textViewTimestamp.text = insight.timestamp?.toDate()?.let { dateFormatter.format(it) } ?: ""

            // Muda a aparência do card se ele já foi lido
            val cardAlpha = if (insight.isRead) 0.7f else 1.0f
            binding.root.alpha = cardAlpha

            // Define o ícone e a cor dinamicamente com base no tipo de insight
            val context = binding.root.context
            val (iconRes, colorRes) = when (insight.type) {
                "POSITIVE_REINFORCEMENT" -> R.drawable.ic_check_circle to R.color.success_green
                "ADHERENCE_ISSUE" -> R.drawable.ic_alert to R.color.warning_orange
                "HEALTH_TREND_ALERT" -> R.drawable.ic_show_chart to R.color.error_red
                "CORRELATION_INSIGHT" -> R.drawable.ic_timeline to colorPrimary
                else -> R.drawable.ic_info to com.google.android.material.R.attr.colorOnSurfaceVariant
            }

            binding.imageViewIcon.setImageResource(iconRes)

            val color = if (colorRes == colorPrimary || colorRes == com.google.android.material.R.attr.colorOnSurfaceVariant) {
                MaterialColors.getColor(context, colorRes, ContextCompat.getColor(context, R.color.colorPrimary))
            } else {
                ContextCompat.getColor(context, colorRes)
            }
            binding.imageViewIcon.imageTintList = ColorStateList.valueOf(color)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Insight>() {
        override fun areItemsTheSame(oldItem: Insight, newItem: Insight): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Insight, newItem: Insight): Boolean {
            return oldItem == newItem
        }
    }
}