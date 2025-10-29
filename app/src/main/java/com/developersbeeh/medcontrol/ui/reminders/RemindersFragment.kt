package com.developersbeeh.medcontrol.ui.reminders

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.data.model.Reminder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.developersbeeh.medcontrol.databinding.FragmentRemindersBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import com.developersbeeh.medcontrol.R

@AndroidEntryPoint
class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RemindersViewModel by viewModels()
    private val args: RemindersFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initialize(args.dependentId)

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        val adapter = RemindersAdapter(
            onEditClick = { reminder ->
                showTimePickerForReminder(reminder.type, reminder, isEditing = true)
            },
            onDeleteClick = { reminder ->
                viewModel.deleteReminder(reminder)
            },
            onToggleClick = { reminder ->
                viewModel.toggleReminder(reminder)
            }
        )

        binding.recyclerViewReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewReminders.adapter = adapter

        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            adapter.submitList(reminders)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddReminder.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun showAddReminderDialog() {
        val reminderTypes = arrayOf(
            "Beber água", "Medir pressão arterial", "Exercitar-se",
            "Tomar vitaminas", "Verificar glicose", "Outro"
        )

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Escolher tipo de lembrete")
            .setItems(reminderTypes) { dialog, which ->
                val selectedType = reminderTypes[which]
                showTimePickerForReminder(selectedType, null, isEditing = false)
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showTimePickerForReminder(type: String, existingReminder: Reminder?, isEditing: Boolean) {
        val pickerTitle = if (isEditing) "Editar Horário" else "Escolher Horário"
        val initialHour = existingReminder?.time?.hour ?: 12
        val initialMinute = existingReminder?.time?.minute ?: 0

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .setTitleText(pickerTitle)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val time = LocalTime.of(timePicker.hour, timePicker.minute)
            if (isEditing && existingReminder != null) {
                viewModel.updateReminderTime(existingReminder, time)
            } else {
                viewModel.addReminder(type, time)
            }
        }

        timePicker.show(parentFragmentManager, "TIME_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}