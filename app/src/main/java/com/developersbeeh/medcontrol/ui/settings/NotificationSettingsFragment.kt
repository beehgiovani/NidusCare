// src/main/java/com/developersbeeh/medcontrol/ui/settings/NotificationSettingsFragment.kt

package com.developersbeeh.medcontrol.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentNotificationSettingsBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Central de Notificações"

        setupPremiumFeatures()
        observeViewModel()
        setupListeners()
    }

    private fun setupPremiumFeatures() {
        val isPremium = UserPreferences(requireContext()).isPremium()
        binding.switchDailySummary.isEnabled = isPremium
        binding.tilSummaryTime.isEnabled = isPremium
        if (!isPremium) {
            binding.switchDailySummary.isChecked = false
        }
    }

    private fun observeViewModel() {
        viewModel.doseRemindersEnabled.observe(viewLifecycleOwner) { binding.switchDoseReminders.isChecked = it }
        viewModel.missedDoseAlertsEnabled.observe(viewLifecycleOwner) { binding.switchMissedDose.isChecked = it }
        viewModel.lowStockAlertsEnabled.observe(viewLifecycleOwner) { binding.switchLowStock.isChecked = it }
        viewModel.expiryAlertsEnabled.observe(viewLifecycleOwner) { binding.switchExpiry.isChecked = it }
        viewModel.vaccineAlertsEnabled.observe(viewLifecycleOwner) { binding.switchVaccineAlerts.isChecked = it }
        viewModel.appointmentRemindersEnabled.observe(viewLifecycleOwner) { binding.switchUpcomingAppointments.isChecked = it }
        viewModel.motivationalNotificationsEnabled.observe(viewLifecycleOwner) { binding.switchMotivationalNotifications.isChecked = it }
        viewModel.hydrationRemindersEnabled.observe(viewLifecycleOwner) { binding.switchHydrationReminders.isChecked = it }

        viewModel.dailySummaryEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.switchDailySummary.isChecked = isEnabled
            binding.tilSummaryTime.isEnabled = isEnabled && UserPreferences(requireContext()).isPremium()
        }
        viewModel.dailySummaryTime.observe(viewLifecycleOwner) { time ->
            binding.editTextSummaryTime.setText(time)
        }
    }

    private fun setupListeners() {
        binding.switchDoseReminders.setOnCheckedChangeListener { _, isChecked -> viewModel.setDoseRemindersEnabled(isChecked) }
        binding.switchMissedDose.setOnCheckedChangeListener { _, isChecked -> viewModel.setMissedDoseAlertsEnabled(isChecked) }
        binding.switchLowStock.setOnCheckedChangeListener { _, isChecked -> viewModel.setLowStockAlertsEnabled(isChecked) }
        binding.switchExpiry.setOnCheckedChangeListener { _, isChecked -> viewModel.setExpiryAlertsEnabled(isChecked) }
        binding.switchVaccineAlerts.setOnCheckedChangeListener { _, isChecked -> viewModel.setVaccineAlertsEnabled(isChecked) }
        binding.switchUpcomingAppointments.setOnCheckedChangeListener { _, isChecked -> viewModel.setAppointmentRemindersEnabled(isChecked) }
        binding.switchMotivationalNotifications.setOnCheckedChangeListener { _, isChecked -> viewModel.setMotivationalNotificationsEnabled(isChecked) }
        binding.switchHydrationReminders.setOnCheckedChangeListener { _, isChecked -> viewModel.setHydrationRemindersEnabled(isChecked) }

        binding.switchDailySummary.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !UserPreferences(requireContext()).isPremium()) {
                binding.switchDailySummary.isChecked = false
                Toast.makeText(context, "O Resumo Diário é uma funcionalidade Premium.", Toast.LENGTH_LONG).show()
            } else {
                viewModel.setDailySummaryEnabled(isChecked)
            }
        }

        binding.editTextSummaryTime.setOnClickListener {
            if (binding.tilSummaryTime.isEnabled) {
                showTimePicker()
            }
        }
    }

    private fun showTimePicker() {
        val currentTime = LocalTime.parse(binding.editTextSummaryTime.text.toString(), DateTimeFormatter.ofPattern("HH:mm"))
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentTime.hour)
            .setMinute(currentTime.minute)
            .setTitleText("Selecione o horário para o resumo")
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedTime = LocalTime.of(picker.hour, picker.minute)
            viewModel.setDailySummaryTime(selectedTime)
        }

        picker.show(parentFragmentManager, "TIME_PICKER_SUMMARY")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}