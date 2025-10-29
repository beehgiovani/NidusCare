package com.developersbeeh.medcontrol.ui.listmedicamentos

import android.R.attr.colorError
import android.R.attr.colorPrimary
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoMedicamento
import com.developersbeeh.medcontrol.databinding.MedicamentoItemBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

class MedicamentoAdapter(
    private val isCaregiver: Boolean,
    private val podeRegistrarDose: Boolean,
    private val onDeleteClick: (MedicamentoUiState) -> Unit,
    private val onEditClick: (MedicamentoUiState) -> Unit,
    private val onMarkAsTakenClick: (MedicamentoUiState) -> Unit,
    private val onPausePlayClick: (MedicamentoUiState) -> Unit,
    private val onRefillClick: (MedicamentoUiState) -> Unit,
    private val onSkipDoseClick: (MedicamentoUiState) -> Unit
) : ListAdapter<MedicamentoUiState, MedicamentoAdapter.MedicamentoViewHolder>(MedicamentoUiStateDiffCallback()) {

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val binding = MedicamentoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicamentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_load_in)
        holder.bind(getItem(position), position == expandedPosition)
    }

    inner class MedicamentoViewHolder(private val binding: MedicamentoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        init {
            binding.root.setOnClickListener {
                // ✅ ALTERAÇÃO: Impede o clique se estiver pendente de arquivamento
                if (getItem(bindingAdapterPosition).isPendingArchive) return@setOnClickListener
                toggleDetailsVisibility(bindingAdapterPosition)
            }
            binding.imageViewExpandCollapse.setOnClickListener {
                if (getItem(bindingAdapterPosition).isPendingArchive) return@setOnClickListener
                toggleDetailsVisibility(bindingAdapterPosition)
            }
            binding.buttonSkip.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onSkipDoseClick(getItem(bindingAdapterPosition))
                }
            }
            binding.buttonArchive.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(bindingAdapterPosition))
                }
            }
            binding.buttonQuickRefill.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onRefillClick(getItem(bindingAdapterPosition))
                }
            }
            binding.buttonMarkAsTakenDetail.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onMarkAsTakenClick(getItem(bindingAdapterPosition))
                }
            }
            binding.buttonRefill.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onRefillClick(getItem(bindingAdapterPosition))
                }
            }
        }

        @SuppressLint("ResourceType")
        fun bind(uiState: MedicamentoUiState, isExpanded: Boolean) {
            val context = binding.root.context
            val medicamento = uiState.medicamento

            val errorColor = MaterialColors.getColor(context, colorError, Color.RED)
            val warningColor = ContextCompat.getColor(context, R.color.colorWarning)
            val primaryColor = MaterialColors.getColor(context, colorPrimary, Color.BLUE)

            // --- ✅ CORREÇÃO: Lógica de estado pendente ---
            if (uiState.isPendingArchive) {
                binding.root.alpha = 0.5f
                binding.root.isClickable = false // Desabilita o clique no card
                // Garante que todos os botões de ação rápida estejam ocultos
                binding.buttonQuickRefill.visibility = View.GONE
                binding.buttonArchive.visibility = View.GONE
                binding.statusLayout.visibility = View.VISIBLE // Mostra o status (ex: Finalizado)
            } else {
                binding.root.alpha = if (uiState.status == MedicamentoStatus.FINALIZADO) 0.6f else 1.0f
                binding.root.isClickable = true
            }
            // --- Fim da correção ---

            val canTakeDose = podeRegistrarDose && uiState.status != MedicamentoStatus.FINALIZADO && !uiState.isPendingArchive

            binding.detailsLayout.visibility = if (isExpanded && !uiState.isPendingArchive) View.VISIBLE else View.GONE
            binding.imageViewExpandCollapse.rotation = if (isExpanded) 180f else 0f

            binding.textViewNome.text = medicamento.nome
            binding.textViewDosagem.text = medicamento.dosagem

            binding.iconTipoMedicamento.setImageResource(when (medicamento.tipo) {
                TipoMedicamento.ORAL -> R.drawable.ic_pill
                TipoMedicamento.TOPICO -> R.drawable.ic_lotion
                TipoMedicamento.INJETAVEL -> R.drawable.ic_injection
            })

            val hasStockInfo = medicamento.lotes.isNotEmpty() || medicamento.nivelDeAlertaEstoque > 0
            val currentStock = medicamento.estoqueAtualTotal
            val isStockLowOrEmpty = (medicamento.nivelDeAlertaEstoque > 0 && currentStock <= medicamento.nivelDeAlertaEstoque) || currentStock <= 0

            updateStatusIndicators(uiState, context, isExpanded, isStockLowOrEmpty)
            updateAdherenceProgress(uiState)

            if (isExpanded) {
                // --- Preenche o Bloco de Detalhes (Expandido) ---

                binding.buttonMarkAsTakenDetail.isEnabled = canTakeDose
                if (!podeRegistrarDose) {
                    TooltipCompat.setTooltipText(binding.buttonMarkAsTakenDetail, context.getString(R.string.permission_denied_tooltip))
                } else {
                    TooltipCompat.setTooltipText(binding.buttonMarkAsTakenDetail, null)
                }

                binding.textViewFrequenciaHorario.visibility = if (medicamento.isUsoEsporadico) View.GONE else View.VISIBLE
                binding.textViewDuracao.visibility = if (medicamento.isUsoEsporadico) View.GONE else View.VISIBLE

                binding.textViewFrequenciaHorario.text = "Horários: ${medicamento.horarios.sorted().joinToString(", ") { it.format(timeFormatter) }}"
                binding.textViewDuracao.text = if (medicamento.isUsoContinuo) "Uso contínuo" else "Duração: ${medicamento.duracaoDias} dia(s)"

                if (medicamento.anotacoes.isNullOrBlank()) {
                    binding.anotacoesLayout.visibility = View.GONE
                } else {
                    binding.anotacoesLayout.visibility = View.VISIBLE
                    binding.textViewAnotacoes.text = medicamento.anotacoes
                }

                if (medicamento.tipo == TipoMedicamento.TOPICO || medicamento.tipo == TipoMedicamento.INJETAVEL) {
                    binding.textViewUltimoLocal.visibility = if (uiState.ultimoLocalAplicado != null) View.VISIBLE else View.GONE
                    binding.textViewUltimoLocal.text = "Última aplicação: ${uiState.ultimoLocalAplicado ?: "N/A"}"
                    binding.textViewProximoLocal.visibility = if (uiState.proximoLocalSugerido != null) View.VISIBLE else View.GONE
                    binding.textViewProximoLocal.text = "Próxima aplicação: ${uiState.proximoLocalSugerido ?: "N/A"}"
                } else {
                    binding.textViewUltimoLocal.visibility = View.GONE
                    binding.textViewProximoLocal.visibility = View.GONE
                }

                if (isCaregiver) {
                    binding.actionsLayout.visibility = View.VISIBLE
                    binding.buttonPausePlay.setIconResource(if (medicamento.isPaused) R.drawable.ic_play_arrow else R.drawable.ic_pause)
                    binding.buttonPausePlay.contentDescription = if (medicamento.isPaused) "Ativar" else "Pausar"
                    binding.buttonPausePlay.setOnClickListener { onPausePlayClick(uiState) }
                    binding.buttonEdit.setOnClickListener { onEditClick(uiState) }
                    binding.buttonDelete.setOnClickListener { onDeleteClick(uiState) }

                    val isEligibleToSkip = !medicamento.isPaused &&
                            (uiState.status == MedicamentoStatus.PROXIMA_DOSE || uiState.status == MedicamentoStatus.ATRASADO)
                    binding.buttonSkip.isVisible = isEligibleToSkip && podeRegistrarDose

                } else {
                    binding.actionsLayout.visibility = View.GONE
                }

                // Preenche o Bloco de Estoque DETALHADO
                if (hasStockInfo) {
                    binding.estoqueLayout.visibility = View.VISIBLE
                    val initialStock = medicamento.estoqueInicialTotal
                    val maxProgress = (if(initialStock > 0) initialStock else currentStock).toInt().coerceAtLeast(1)
                    val loteMaisProximo = medicamento.lotes
                        .filter { it.dataValidade.isAfter(LocalDate.now().minusDays(1)) }
                        .minByOrNull { it.dataValidade }

                    val corProgresso = when {
                        currentStock <= 0 -> errorColor
                        isStockLowOrEmpty -> errorColor
                        loteMaisProximo != null && loteMaisProximo.dataValidade.isBefore(LocalDate.now().plusDays(30)) -> warningColor
                        (if (initialStock > 0) (currentStock / initialStock) * 100 else 0.0) <= 50 -> warningColor
                        else -> primaryColor
                    }

                    if (currentStock <= 0) {
                        binding.progressEstoque.visibility = View.GONE
                        binding.textViewEstoque.text = "Estoque zerado"
                        binding.textViewEstoque.setTextColor(errorColor)
                        binding.buttonRefill.text = "Repor"
                    } else {
                        binding.progressEstoque.visibility = View.VISIBLE
                        binding.textViewEstoque.setTextColor(MaterialColors.getColor(context, MaterialR.attr.colorOnSurfaceVariant, Color.GRAY))
                        binding.progressEstoque.max = maxProgress
                        binding.progressEstoque.progress = currentStock.toInt()
                        binding.textViewEstoque.text = String.format("%.1f de %.1f %s restantes", currentStock, initialStock, medicamento.unidadeDeEstoque)
                        binding.buttonRefill.text = "Repor"
                    }

                    if (loteMaisProximo != null) {
                        binding.textViewValidadeProxima.visibility = View.VISIBLE
                        binding.textViewValidadeProxima.text = "Vencimento próximo: ${loteMaisProximo.dataValidade.format(dateFormatter)}"
                        binding.textViewValidadeProxima.setTextColor(when {
                            loteMaisProximo.dataValidade.isBefore(LocalDate.now()) -> errorColor
                            loteMaisProximo.dataValidade.isBefore(LocalDate.now().plusDays(30)) -> warningColor
                            else -> MaterialColors.getColor(context, MaterialR.attr.colorOnSurfaceVariant, Color.GRAY)
                        })
                    } else {
                        binding.textViewValidadeProxima.visibility = View.VISIBLE
                        binding.textViewValidadeProxima.text = if (currentStock > 0) "Todos os lotes estão vencidos" else "Sem lotes válidos"
                        binding.textViewValidadeProxima.setTextColor(errorColor)
                    }

                    binding.progressEstoque.setIndicatorColor(corProgresso)
                    binding.buttonRefill.visibility = if (isCaregiver) View.VISIBLE else View.GONE
                } else {
                    binding.estoqueLayout.visibility = View.GONE
                }

            }
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

        @SuppressLint("ResourceType")
        private fun updateStatusIndicators(uiState: MedicamentoUiState, context: Context, isExpanded: Boolean, isStockLow: Boolean) {
            // Se estiver pendente de arquivamento, não mostre nada além do status de finalizado (que é o que o 'delete' faz)
            if (uiState.isPendingArchive) {
                binding.statusLayout.visibility = View.VISIBLE
                binding.buttonArchive.visibility = View.GONE
                binding.buttonQuickRefill.visibility = View.GONE
                binding.textViewStatus.text = "Excluindo..."
                binding.iconStatus.setImageResource(R.drawable.ic_delete)
                binding.textViewStatus.setTextColor(MaterialColors.getColor(context, colorError, Color.RED))
                binding.iconStatus.setColorFilter(MaterialColors.getColor(context, colorError, Color.RED))
                binding.buttonMarkAsTakenDetail.visibility = View.GONE
                return
            }

            binding.textViewStatus.text = uiState.statusText
            val color: Int
            val icon: Int

            binding.buttonArchive.visibility = View.GONE
            binding.buttonQuickRefill.visibility = View.GONE
            binding.statusLayout.visibility = View.VISIBLE

            when (uiState.status) {
                MedicamentoStatus.ATRASADO -> {
                    color = MaterialColors.getColor(context, colorError, Color.RED)
                    icon = R.drawable.ic_alert
                }
                MedicamentoStatus.PROXIMA_DOSE -> {
                    color = MaterialColors.getColor(context, colorPrimary, Color.BLUE)
                    icon = R.drawable.ic_alarm
                }
                MedicamentoStatus.ESPORADICO -> {
                    color = MaterialColors.getColor(context, MaterialR.attr.colorSecondary, Color.CYAN)
                    icon = R.drawable.ic_add_circle
                }
                MedicamentoStatus.FINALIZADO -> {
                    color = ContextCompat.getColor(context, R.color.success_green)
                    icon = R.drawable.ic_check
                    if (!isExpanded && isCaregiver) {
                        binding.buttonArchive.visibility = View.VISIBLE
                        binding.statusLayout.visibility = View.GONE
                    }
                }
                MedicamentoStatus.SEM_NOTIFICACAO -> {
                    color = ContextCompat.getColor(context, R.color.success_green)
                    icon = R.drawable.ic_check
                    binding.statusLayout.visibility = View.GONE
                }
                MedicamentoStatus.PAUSADO -> {
                    color = MaterialColors.getColor(context, MaterialR.attr.colorOutline, Color.GRAY)
                    icon = R.drawable.ic_pause
                }
            }
            binding.textViewStatus.setTextColor(color)
            binding.iconStatus.setImageResource(icon)
            binding.iconStatus.setColorFilter(color)

            if (!isExpanded && isCaregiver && isStockLow && uiState.status != MedicamentoStatus.FINALIZADO) {
                binding.buttonQuickRefill.visibility = View.VISIBLE
                binding.statusLayout.visibility = View.GONE
                binding.buttonArchive.visibility = View.GONE
            }

            val showSporadicButton = uiState.status == MedicamentoStatus.ESPORADICO
            val showScheduledButton = uiState.status == MedicamentoStatus.PROXIMA_DOSE || uiState.status == MedicamentoStatus.ATRASADO

            if (isExpanded) {
                if (showSporadicButton) {
                    binding.buttonMarkAsTakenDetail.text = "Registar Dose"
                    binding.buttonMarkAsTakenDetail.visibility = if (podeRegistrarDose) View.VISIBLE else View.GONE
                } else {
                    binding.buttonMarkAsTakenDetail.text = "Tomar Agora"
                    binding.buttonMarkAsTakenDetail.visibility = if (showScheduledButton && podeRegistrarDose) View.VISIBLE else View.GONE
                }
            } else {
                binding.buttonMarkAsTakenDetail.visibility = View.GONE
            }
        }

        private fun updateAdherenceProgress(uiState: MedicamentoUiState) {
            // ✅ CORREÇÃO: A lógica de adesão agora usa os IDs do layout principal (mainInfoLayout)
            if (uiState.medicamento.isUsoEsporadico || uiState.status == MedicamentoStatus.FINALIZADO || uiState.status == MedicamentoStatus.PAUSADO) {
                binding.adherenceGroup.visibility = View.GONE
                return
            }
            binding.adherenceGroup.visibility = View.VISIBLE
            binding.progressAdherence.max = if (uiState.dosesEsperadasHoje > 0) uiState.dosesEsperadasHoje else 1
            binding.progressAdherence.progress = min(uiState.dosesTomadasHoje, uiState.dosesEsperadasHoje)
            binding.textViewAdherence.text = "${uiState.dosesTomadasHoje} de ${uiState.dosesEsperadasHoje} doses hoje"

            if (uiState.adherenceStatus == AdherenceStatus.OVERDOSE) {
                val errorColor = MaterialColors.getColor(binding.root.context, colorError, Color.RED)
                binding.progressAdherence.progressTintList = ColorStateList.valueOf(errorColor)
                binding.textViewOverdoseWarning.visibility = View.VISIBLE
            } else {
                val primaryColor = MaterialColors.getColor(binding.root.context, colorPrimary, Color.BLUE)
                binding.progressAdherence.progressTintList = ColorStateList.valueOf(primaryColor)
                binding.textViewOverdoseWarning.visibility = View.GONE
            }
        }
    }

    private class MedicamentoUiStateDiffCallback : DiffUtil.ItemCallback<MedicamentoUiState>() {
        override fun areItemsTheSame(oldItem: MedicamentoUiState, newItem: MedicamentoUiState): Boolean {
            return oldItem.medicamento.id == newItem.medicamento.id
        }
        override fun areContentsTheSame(oldItem: MedicamentoUiState, newItem: MedicamentoUiState): Boolean {
            return oldItem == newItem
        }
    }
}