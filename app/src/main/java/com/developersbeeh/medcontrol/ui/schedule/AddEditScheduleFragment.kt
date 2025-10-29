package com.developersbeeh.medcontrol.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.model.TipoAgendamento
import com.developersbeeh.medcontrol.databinding.FragmentAddEditScheduleBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddEditScheduleFragment : Fragment() {

    private var _binding: FragmentAddEditScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditScheduleViewModel by viewModels()
    private val args: AddEditScheduleFragmentArgs by navArgs()

    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeDropdown()

        val agendamento = args.agendamento
        if (agendamento != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_schedule_title)
            binding.textViewTitle.text = getString(R.string.edit_schedule_title)
            prefillForm(agendamento)
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_schedule_title)
            binding.textViewTitle.text = getString(R.string.add_schedule_title)
            updateDateText()
            updateTimeText()
        }

        setupListeners()
        observeViewModel()
    }

    private fun prefillForm(agendamento: AgendamentoSaude) {
        binding.autoCompleteScheduleType.setText(agendamento.tipo.displayName, false)
        binding.editTextScheduleTitle.setText(agendamento.titulo)
        selectedDate = agendamento.timestamp.toLocalDate()
        selectedTime = agendamento.timestamp.toLocalTime()
        updateDateText()
        updateTimeText()
        binding.editTextLocation.setText(agendamento.local)
        binding.editTextProfessional.setText(agendamento.profissional)
        binding.editTextNotes.setText(agendamento.notasDePreparo)
        (binding.chipGroupReminders.getChildAt(0) as Chip).isChecked = agendamento.lembretes.contains(120)
        (binding.chipGroupReminders.getChildAt(1) as Chip).isChecked = agendamento.lembretes.contains(1440)
        (binding.chipGroupReminders.getChildAt(2) as Chip).isChecked = agendamento.lembretes.contains(2880)
    }

    private fun setupTypeDropdown() {
        val types = TipoAgendamento.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.autoCompleteScheduleType.setAdapter(adapter)
        binding.autoCompleteScheduleType.setText(types[0], false)
    }

    private fun setupListeners() {
        binding.editTextScheduleDate.setOnClickListener { showDatePicker() }
        binding.tilScheduleDate.setEndIconOnClickListener { showDatePicker() }
        binding.editTextScheduleTime.setOnClickListener { showTimePicker() }
        binding.tilScheduleTime.setEndIconOnClickListener { showTimePicker() }

        binding.buttonSave.setOnClickListener {
            saveSchedule()
        }
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun saveSchedule() {
        val titulo = binding.editTextScheduleTitle.text.toString().trim()
        val tipoDisplayName = binding.autoCompleteScheduleType.text.toString()
        val tipo = TipoAgendamento.values().firstOrNull { it.displayName == tipoDisplayName }

        if (titulo.isBlank()) {
            binding.tilScheduleTitle.error = getString(R.string.required_field)
            return
        }
        if (tipo == null) {
            Toast.makeText(context, getString(R.string.invalid_schedule_type), Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDate.atTime(selectedTime).isBefore(LocalDateTime.now())) {
            Toast.makeText(context, getString(R.string.schedule_date_past_error), Toast.LENGTH_SHORT).show()
            // CORREÇÃO: Adicionado o return para impedir o salvamento de um agendamento no passado.
            return
        }

        val lembretes = mutableListOf<Int>()
        if ((binding.chipGroupReminders.getChildAt(0) as Chip).isChecked) lembretes.add(120)
        if ((binding.chipGroupReminders.getChildAt(1) as Chip).isChecked) lembretes.add(1440)
        if ((binding.chipGroupReminders.getChildAt(2) as Chip).isChecked) lembretes.add(2880)

        viewModel.saveSchedule(
            dependentId = args.dependentId,
            agendamentoToEdit = args.agendamento,
            titulo = titulo,
            tipo = tipo,
            data = selectedDate,
            hora = selectedTime,
            local = binding.editTextLocation.text.toString().trim().takeIf { it.isNotEmpty() },
            profissional = binding.editTextProfessional.text.toString().trim().takeIf { it.isNotEmpty() },
            notas = binding.editTextNotes.text.toString().trim().takeIf { it.isNotEmpty() },
            lembretes = lembretes
        )
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSave.isEnabled = !isLoading
            binding.buttonCancel.isEnabled = !isLoading
        }
        viewModel.saveStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(context, getString(R.string.schedule_saved_success), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.onFailure {
                    Toast.makeText(context, getString(R.string.error_saving_schedule, it.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.schedule_date_picker_title))
            .setSelection(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            // ✅ CORREÇÃO AQUI
            selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            updateDateText()
        }
        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                updateTimeText()
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
        timePicker.show()
    }

    private fun updateDateText() {
        binding.editTextScheduleDate.setText(selectedDate.format(dateFormatter))
    }

    private fun updateTimeText() {
        binding.editTextScheduleTime.setText(selectedTime.format(timeFormatter))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}