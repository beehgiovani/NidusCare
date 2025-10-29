package com.developersbeeh.medcontrol.ui.caregiver

import android.content.res.ColorStateList
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.databinding.CaregiverDashboardItemBinding
import com.google.android.material.R as MaterialR

class CaregiverDashboardAdapter(
    // ✅ ALTERAÇÃO: A lambda agora passa a View do card clicado
    private val onDependentClick: (DependentWithStatus, View) -> Unit,
    private val onViewInsightsClick: (DependentWithStatus) -> Unit
) : ListAdapter<DependentWithStatus, CaregiverDashboardAdapter.ViewHolder>(DiffCallback()) {

    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CaregiverDashboardItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: CaregiverDashboardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val clickedPosition = bindingAdapterPosition
                val previouslyExpandedPosition = expandedPosition
                val item = getItem(clickedPosition)

                if (previouslyExpandedPosition == clickedPosition) {
                    // ✅ ALTERAÇÃO: Passa a view (binding.cardViewDependent) para a animação
                    onDependentClick(item, binding.cardViewDependent)
                } else {
                    expandedPosition = clickedPosition
                    // Notifica as mudanças para a animação de expansão interna
                    if (previouslyExpandedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previouslyExpandedPosition)
                    }
                    notifyItemChanged(expandedPosition)
                }
            }
        }

        fun bind(dependentWithStatus: DependentWithStatus) {
            val context = binding.root.context
            val dependente = dependentWithStatus.dependente
            val isExpanded = bindingAdapterPosition == expandedPosition

            // ✅ ADIÇÃO: Define um transitionName único para cada card
            binding.cardViewDependent.transitionName = "dependent_card_transition_${dependente.id}"

            // --- Animações e Visibilidade ---
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition().setDuration(250))
            binding.cardViewDependent.isDragged = isExpanded // Simula elevação
            binding.imageViewExpandCollapse.rotation = if (isExpanded) 180f else 0f
            binding.detailsGroup.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // --- Header ---
            binding.imageViewAvatar.load(dependente.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
            binding.textViewDependentName.text = dependente.nome
            val ageText = dependentWithStatus.age?.let { "$it anos" } ?: ""
            val sexText = Sexo.values().firstOrNull { it.name == dependente.sexo || it.displayName == dependente.sexo }?.displayName ?: ""
            binding.textViewAgeAndSex.text = listOf(ageText, sexText).filter { it.isNotBlank() }.joinToString(", ")
            binding.textViewAgeAndSex.isVisible = binding.textViewAgeAndSex.text.isNotBlank()

            // --- Sequência (Streak) ---
            binding.streakLayout.isVisible = dependentWithStatus.adherenceStreak > 0
            binding.textViewStreakCount.text = dependentWithStatus.adherenceStreak.toString()

            // --- Barra de Status e Borda do Card ---
            val hasMissedDoses = dependentWithStatus.missedDoseMedicationNames.isNotEmpty()
            val errorColor = ContextCompat.getColor(context, R.color.md_theme_error)
            val warningColor = ContextCompat.getColor(context, R.color.warning_orange)
            val successColor = ContextCompat.getColor(context, R.color.status_success_green)
            val primaryColor = ContextCompat.getColor(context, R.color.md_theme_primary)
            val outlineColor = ContextCompat.getColor(context, MaterialR.color.material_on_surface_stroke)

            val statusBarColor = when {
                hasMissedDoses -> errorColor
                dependentWithStatus.hasLowStock || dependentWithStatus.hasAppointmentToday || dependentWithStatus.unreadInsightsCount > 0 -> warningColor
                else -> successColor
            }
            binding.statusBar.setBackgroundColor(statusBarColor)
            binding.cardViewDependent.strokeColor = if (isExpanded) primaryColor else outlineColor


            // --- Preenchimento dos Detalhes (Apenas se Expandido) ---
            if (isExpanded) {
                // Progresso do Dia
                binding.textViewTodayProgressLabel.text = "Hoje: ${dependentWithStatus.dosesTomadasHoje} de ${dependentWithStatus.dosesEsperadasHoje} doses"
                binding.progressToday.max = if (dependentWithStatus.dosesEsperadasHoje > 0) dependentWithStatus.dosesEsperadasHoje else 1
                binding.progressToday.progress = dependentWithStatus.dosesTomadasHoje

                // Adesão Circular
                binding.adherenceSection.isVisible = dependentWithStatus.aderencia7dias >= 0
                binding.progressAdherence.progress = dependentWithStatus.aderencia7dias
                binding.textViewAdherencePercentage.text = "${dependentWithStatus.aderencia7dias}%"

                // Próxima Dose
                binding.textViewNextDose.text = dependentWithStatus.proximaDoseTexto
                binding.textViewNextDose.setTextColor(if(hasMissedDoses) errorColor else ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))

                // Indicadores de Status

                binding.appointmentTodayLayout.isVisible = dependentWithStatus.hasAppointmentToday
                binding.lowStockLayout.isVisible = dependentWithStatus.hasLowStock


                binding.labelAppointmentToday.setTextColor(primaryColor)
                binding.iconAppointmentToday.imageTintList = ColorStateList.valueOf(primaryColor)
                binding.labelLowStock.setTextColor(warningColor)
                binding.iconLowStock.imageTintList = ColorStateList.valueOf(warningColor)

                // Insights
                binding.insightPreviewCard.isVisible = dependentWithStatus.unreadInsightsCount > 0
                if (dependentWithStatus.unreadInsightsCount > 0) {
                    binding.textViewInsightCount.text = dependentWithStatus.unreadInsightsCount.toString()
                    binding.textViewInsightPreview.text = dependentWithStatus.latestUnreadInsightPreview
                    binding.insightPreviewCard.setOnClickListener {
                        onViewInsightsClick(dependentWithStatus)
                    }
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DependentWithStatus>() {
        override fun areItemsTheSame(oldItem: DependentWithStatus, newItem: DependentWithStatus): Boolean {
            return oldItem.dependente.id == newItem.dependente.id
        }

        override fun areContentsTheSame(oldItem: DependentWithStatus, newItem: DependentWithStatus): Boolean {
            return oldItem == newItem
        }
    }
}