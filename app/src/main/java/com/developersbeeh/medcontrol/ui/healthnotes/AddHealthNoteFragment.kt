package com.developersbeeh.medcontrol.ui.healthnotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.databinding.FragmentAddHealthNoteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddHealthNoteFragment : Fragment() {

    private var _binding: FragmentAddHealthNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthNotesViewModel by viewModels()
    private val args: AddHealthNoteFragmentArgs by navArgs()
    private var healthNoteToEdit: HealthNote? = null

    // CORREÇÃO: Filtra a lista para remover os tipos que não são adicionados por esta tela.
    private val noteTypes = HealthNoteType.values()
        .filter { it != HealthNoteType.MOOD && it != HealthNoteType.SYMPTOM }
        .map { it.displayName }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHealthNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        healthNoteToEdit = args.healthNoteToEdit
        if (healthNoteToEdit != null) {
            prefillFormForEditing(healthNoteToEdit!!)
            binding.buttonSaveNote.text = "Atualizar Anotação"
        } else {
            setupTypeDropdown()
            // Mostra os inputs para o primeiro item da lista filtrada.
            val initialType = HealthNoteType.values().first { it.displayName == noteTypes[0] }
            showDynamicInputs(initialType)
        }
        setupListeners()
        observeViewModel()
    }

    private fun setupTypeDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, noteTypes)
        binding.autoCompleteNoteType.setAdapter(adapter)
        binding.autoCompleteNoteType.setText(noteTypes[0], false)
    }

    private fun setupListeners() {
        binding.autoCompleteNoteType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = HealthNoteType.values().first { it.displayName == noteTypes[position] }
            showDynamicInputs(selectedType)
        }

        binding.buttonSaveNote.setOnClickListener {
            saveHealthNote()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun prefillFormForEditing(note: HealthNote) {
        binding.tilNoteType.isEnabled = false
        binding.autoCompleteNoteType.setText(note.type.displayName, false)
        binding.editTextNote.setText(note.note)

        showDynamicInputs(note.type)

        when (note.type) {
            HealthNoteType.BLOOD_PRESSURE -> {
                binding.editTextSystolic.setText(note.values["systolic"])
                binding.editTextDiastolic.setText(note.values["diastolic"])
                binding.editTextHeartRate.setText(note.values["heartRate"])
            }
            HealthNoteType.BLOOD_SUGAR -> {
                binding.editTextBloodSugar.setText(note.values["sugarLevel"])
            }
            HealthNoteType.WEIGHT -> {
                binding.editTextWeight.setText(note.values["weight"])
            }
            HealthNoteType.TEMPERATURE -> {
                binding.editTextTemperature.setText(note.values["temperature"])
            }
            HealthNoteType.GENERAL -> {
                binding.editTextGeneralNote.setText(note.values["generalNote"])
            }
            // Os tipos MOOD e SYMPTOM não são editáveis nesta tela, então não precisam de preenchimento.
            HealthNoteType.MOOD, HealthNoteType.SYMPTOM -> {
            }
        }
    }

    private fun showDynamicInputs(type: HealthNoteType) {
        binding.layoutBloodPressure.visibility = View.GONE
        binding.tilBloodSugar.visibility = View.GONE
        binding.tilWeight.visibility = View.GONE
        binding.tilTemperature.visibility = View.GONE
        binding.tilGeneralNote.visibility = View.GONE

        when (type) {
            HealthNoteType.BLOOD_PRESSURE -> binding.layoutBloodPressure.visibility = View.VISIBLE
            HealthNoteType.BLOOD_SUGAR -> binding.tilBloodSugar.visibility = View.VISIBLE
            HealthNoteType.WEIGHT -> binding.tilWeight.visibility = View.VISIBLE
            HealthNoteType.TEMPERATURE -> binding.tilTemperature.visibility = View.VISIBLE
            HealthNoteType.GENERAL -> binding.tilGeneralNote.visibility = View.VISIBLE
            // Os tipos MOOD e SYMPTOM não possuem inputs dinâmicos nesta tela.
            HealthNoteType.MOOD, HealthNoteType.SYMPTOM -> {
            }
        }
    }

    private fun saveHealthNote() {
        val selectedTypeString = binding.autoCompleteNoteType.text.toString()
        val type = HealthNoteType.values().first { it.displayName == selectedTypeString }

        val values = mutableMapOf<String, String>()
        val noteText = binding.editTextNote.text.toString().takeIf { it.isNotBlank() }

        when (type) {
            HealthNoteType.BLOOD_PRESSURE -> {
                val systolic = binding.editTextSystolic.text.toString()
                val diastolic = binding.editTextDiastolic.text.toString()
                val heartRate = binding.editTextHeartRate.text.toString()

                if (systolic.isBlank() || diastolic.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha a pressão sistólica e diastólica.", Toast.LENGTH_SHORT).show()
                    return
                }
                values["systolic"] = systolic
                values["diastolic"] = diastolic
                if (heartRate.isNotBlank()) {
                    values["heartRate"] = heartRate
                }
            }
            HealthNoteType.BLOOD_SUGAR -> {
                val sugarLevel = binding.editTextBloodSugar.text.toString()
                if (sugarLevel.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha o nível de glicemia.", Toast.LENGTH_SHORT).show()
                    return
                }
                values["sugarLevel"] = sugarLevel
            }
            HealthNoteType.WEIGHT -> {
                val weight = binding.editTextWeight.text.toString()
                if (weight.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha o peso.", Toast.LENGTH_SHORT).show()
                    return
                }
                values["weight"] = weight
            }
            HealthNoteType.TEMPERATURE -> {
                val temperature = binding.editTextTemperature.text.toString()
                if (temperature.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha a temperatura.", Toast.LENGTH_SHORT).show()
                    return
                }
                values["temperature"] = temperature
            }
            HealthNoteType.GENERAL -> {
                val generalNote = binding.editTextGeneralNote.text.toString()
                if (generalNote.isBlank()) {
                    Toast.makeText(requireContext(), "Preencha a anotação geral.", Toast.LENGTH_SHORT).show()
                    return
                }
                values["generalNote"] = generalNote
            }
            HealthNoteType.MOOD, HealthNoteType.SYMPTOM -> {
                // Não salva a partir desta tela
                return
            }
        }

        viewModel.addHealthNote(type, values, noteText)
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Anotação salva com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Falha ao salvar anotação.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}