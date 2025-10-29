package com.developersbeeh.medcontrol.ui.healthnotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.databinding.HealthNoteItemBinding
import java.time.format.DateTimeFormatter

class HealthNotesAdapter(
    private val onEditClick: (HealthNote) -> Unit,
    private val onDeleteClick: (HealthNote) -> Unit
) : ListAdapter<HealthNote, HealthNotesAdapter.HealthNoteViewHolder>(HealthNoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthNoteViewHolder {
        val binding = HealthNoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HealthNoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthNoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HealthNoteViewHolder(private val binding: HealthNoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val userPreferences = UserPreferences(binding.root.context)
        private val timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        fun bind(note: HealthNote) {
            binding.textViewType.text = note.type.displayName
            binding.textViewTimestamp.text = note.timestamp.format(timeFormatter)

            // ... (o when(note.type) para exibir os valores continua o mesmo)
            binding.layoutBloodPressureValues.visibility = View.GONE
            binding.layoutBloodSugarValue.visibility = View.GONE
            binding.layoutWeightValue.visibility = View.GONE
            binding.layoutTemperatureValue.visibility = View.GONE
            binding.textViewGeneralNoteValue.visibility = View.GONE
            binding.layoutHeartRateValue.visibility = View.GONE
            binding.layoutSymptomValue.visibility = View.GONE
            binding.layoutMoodValue.visibility = View.GONE

            when (note.type) {
                HealthNoteType.BLOOD_PRESSURE -> {
                    binding.layoutBloodPressureValues.visibility = View.VISIBLE
                    binding.textViewSystolicValue.text = "${note.values["systolic"] ?: "N/A"}x${note.values["diastolic"] ?: "N/A"} mmHg"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_blood_pressure)

                    val heartRate = note.values["heartRate"]
                    if (!heartRate.isNullOrBlank()) {
                        binding.layoutHeartRateValue.visibility = View.VISIBLE
                        binding.textViewHeartRateValue.text = "$heartRate BPM"
                    } else {
                        binding.layoutHeartRateValue.visibility = View.GONE
                    }
                }
                HealthNoteType.BLOOD_SUGAR -> {
                    binding.layoutBloodSugarValue.visibility = View.VISIBLE
                    binding.textViewBloodSugarValue.text = "${note.values["sugarLevel"] ?: "N/A"} mg/dL"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_blood_glucose)
                }
                HealthNoteType.WEIGHT -> {
                    binding.layoutWeightValue.visibility = View.VISIBLE
                    binding.textViewWeightValue.text = "${note.values["weight"] ?: "N/A"} kg"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_weight)
                }
                HealthNoteType.TEMPERATURE -> {
                    binding.layoutTemperatureValue.visibility = View.VISIBLE
                    binding.textViewTemperatureValue.text = "${note.values["temperature"] ?: "N/A"} °C"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_thermostat)
                }
                HealthNoteType.MOOD -> {
                    binding.layoutMoodValue.visibility = View.VISIBLE
                    binding.textViewMoodValue.text = note.values["mood"] ?: "Não registrado"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_sentiment)
                }
                HealthNoteType.SYMPTOM -> {
                    binding.layoutSymptomValue.visibility = View.VISIBLE
                    binding.textViewSymptomValue.text = note.values["symptom"] ?: "Não descrito"
                    binding.imageViewIcon.setImageResource(R.drawable.ic_symptom)
                }
                HealthNoteType.GENERAL -> {
                    binding.textViewGeneralNoteValue.visibility = View.VISIBLE
                    binding.textViewGeneralNoteValue.text = note.values["generalNote"] ?: "Sem anotação."
                    binding.imageViewIcon.setImageResource(R.drawable.ic_notes)
                }
            }

            if (!note.note.isNullOrBlank()) {
                binding.textViewNote.visibility = View.VISIBLE
                binding.textViewNote.text = "Obs: ${note.note}"
            } else {
                binding.textViewNote.visibility = View.GONE
            }

            // ✅ CORREÇÃO: Verificação de permissão simplificada
            val canManageNotes = userPreferences.temPermissao(PermissaoTipo.REGISTRAR_ANOTACOES)

            if (canManageNotes) {
                binding.buttonMenu.visibility = View.VISIBLE
                binding.buttonMenu.setOnClickListener { view ->
                    val popup = PopupMenu(view.context, view)
                    popup.inflate(R.menu.health_note_item_menu)

                    val canBeEdited = note.type != HealthNoteType.MOOD && note.type != HealthNoteType.SYMPTOM
                    popup.menu.findItem(R.id.action_edit_health_note).isVisible = canBeEdited

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit_health_note -> {
                                onEditClick(note)
                                true
                            }
                            R.id.action_delete_health_note -> {
                                onDeleteClick(note)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            } else {
                binding.buttonMenu.visibility = View.GONE
            }
        }
    }

    private class HealthNoteDiffCallback : DiffUtil.ItemCallback<HealthNote>() {
        override fun areItemsTheSame(oldItem: HealthNote, newItem: HealthNote): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: HealthNote, newItem: HealthNote): Boolean {
            return oldItem == newItem
        }
    }
}