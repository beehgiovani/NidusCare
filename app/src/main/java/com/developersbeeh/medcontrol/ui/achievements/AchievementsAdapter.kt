// src/main/java/com/developersbeeh/medcontrol/ui/achievements/AchievementsAdapter.kt
package com.developersbeeh.medcontrol.ui.achievements

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
import com.developersbeeh.medcontrol.databinding.ItemAchievementBinding
import com.google.android.material.color.MaterialColors
import kotlin.math.roundToInt

class AchievementsAdapter : ListAdapter<Achievement, AchievementsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            val context = binding.root.context
            val isUnlocked = achievement.state == AchievementState.UNLOCKED

            // Define a opacidade e as cores com base no estado
            val alpha = if (isUnlocked) 1.0f else 0.5f
            binding.root.alpha = alpha

            val iconColor = if (isUnlocked) {
                MaterialColors.getColor(context, colorPrimary, Color.BLACK)
            } else {
                MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }

            val textColor = if (isUnlocked) {
                MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
            } else {
                MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }

            binding.imageViewAchievementIcon.setImageResource(achievement.unlockedIcon)
            binding.imageViewAchievementIcon.imageTintList = ColorStateList.valueOf(iconColor)
            binding.textViewAchievementTitle.text = achievement.title
            binding.textViewAchievementTitle.setTextColor(textColor)
            binding.textViewAchievementDescription.text = achievement.description

            // Barra de Progresso
            if (isUnlocked) {
                binding.progressAchievement.progress = 100
                binding.textViewProgress.text = "Concluído!"
            } else {
                val progress = (achievement.currentProgress.toFloat() / achievement.targetValue * 100).roundToInt()
                binding.progressAchievement.progress = progress
                binding.textViewProgress.text = "${achievement.currentProgress} / ${achievement.targetValue}"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem == newItem
        }
    }
}