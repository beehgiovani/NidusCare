// src/main/java/com/developersbeeh/medcontrol/ui/settings/NotificationSettingsViewModel.kt

package com.developersbeeh.medcontrol.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    // LiveData para cada switch na UI
    private val _doseRemindersEnabled = MutableLiveData<Boolean>()
    val doseRemindersEnabled: LiveData<Boolean> = _doseRemindersEnabled

    private val _missedDoseAlertsEnabled = MutableLiveData<Boolean>()
    val missedDoseAlertsEnabled: LiveData<Boolean> = _missedDoseAlertsEnabled

    private val _lowStockAlertsEnabled = MutableLiveData<Boolean>()
    val lowStockAlertsEnabled: LiveData<Boolean> = _lowStockAlertsEnabled

    private val _expiryAlertsEnabled = MutableLiveData<Boolean>()
    val expiryAlertsEnabled: LiveData<Boolean> = _expiryAlertsEnabled

    private val _vaccineAlertsEnabled = MutableLiveData<Boolean>()
    val vaccineAlertsEnabled: LiveData<Boolean> = _vaccineAlertsEnabled

    private val _appointmentRemindersEnabled = MutableLiveData<Boolean>()
    val appointmentRemindersEnabled: LiveData<Boolean> = _appointmentRemindersEnabled

    private val _dailySummaryEnabled = MutableLiveData<Boolean>()
    val dailySummaryEnabled: LiveData<Boolean> = _dailySummaryEnabled

    private val _dailySummaryTime = MutableLiveData<String>()
    val dailySummaryTime: LiveData<String> = _dailySummaryTime

    private val _motivationalNotificationsEnabled = MutableLiveData<Boolean>()
    val motivationalNotificationsEnabled: LiveData<Boolean> = _motivationalNotificationsEnabled

    private val _hydrationRemindersEnabled = MutableLiveData<Boolean>()
    val hydrationRemindersEnabled: LiveData<Boolean> = _hydrationRemindersEnabled

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        // Usando os nomes de função EXATOS do seu UserPreferences.kt
        _doseRemindersEnabled.value = userPreferences.getDoseRemindersEnabled()
        _missedDoseAlertsEnabled.value = userPreferences.getMissedDoseAlertsEnabled()
        _lowStockAlertsEnabled.value = userPreferences.getLowStockAlertsEnabled()
        _expiryAlertsEnabled.value = userPreferences.getExpiryAlertsEnabled()
        _vaccineAlertsEnabled.value = userPreferences.getVaccineAlertsEnabled()
        _appointmentRemindersEnabled.value = userPreferences.getAppointmentRemindersEnabled()
        _dailySummaryEnabled.value = userPreferences.isDailySummaryEnabled()
        _dailySummaryTime.value = userPreferences.getDailySummaryTime()
        _motivationalNotificationsEnabled.value = userPreferences.isMotivationalNotificationsEnabled()
        _hydrationRemindersEnabled.value = userPreferences.isHydrationRemindersEnabled()
    }

    // Funções "setter" para cada switch, usando seus nomes de função
    fun setDoseRemindersEnabled(enabled: Boolean) {
        _doseRemindersEnabled.value = enabled
        userPreferences.setDoseRemindersEnabled(enabled)
        updateUserPreferenceInFirestore("doseRemindersEnabled", enabled)
    }

    fun setMissedDoseAlertsEnabled(enabled: Boolean) {
        _missedDoseAlertsEnabled.value = enabled
        userPreferences.setMissedDoseAlertsEnabled(enabled)
        updateUserPreferenceInFirestore("missedDoseAlertsEnabled", enabled)
    }

    fun setLowStockAlertsEnabled(enabled: Boolean) {
        _lowStockAlertsEnabled.value = enabled
        userPreferences.setLowStockAlertsEnabled(enabled)
        updateUserPreferenceInFirestore("lowStockAlertsEnabled", enabled)
    }

    fun setExpiryAlertsEnabled(enabled: Boolean) {
        _expiryAlertsEnabled.value = enabled
        userPreferences.setExpiryAlertsEnabled(enabled)
        updateUserPreferenceInFirestore("expiryAlertsEnabled", enabled)
    }

    fun setVaccineAlertsEnabled(enabled: Boolean) {
        _vaccineAlertsEnabled.value = enabled
        userPreferences.setVaccineAlertsEnabled(enabled)
        updateUserPreferenceInFirestore("vaccineAlertsEnabled", enabled)
    }

    fun setAppointmentRemindersEnabled(enabled: Boolean) {
        _appointmentRemindersEnabled.value = enabled
        userPreferences.setAppointmentRemindersEnabled(enabled)
        updateUserPreferenceInFirestore("appointmentRemindersEnabled", enabled)
    }

    fun setDailySummaryEnabled(enabled: Boolean) {
        _dailySummaryEnabled.value = enabled
        userPreferences.setDailySummaryEnabled(enabled)
        updateUserPreferenceInFirestore("dailySummaryEnabled", enabled)
    }

    fun setDailySummaryTime(time: LocalTime) {
        val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))
        _dailySummaryTime.value = timeString
        userPreferences.setDailySummaryTime(timeString)
        updateUserPreferenceInFirestore("dailySummaryTime", time.hour)
    }

    fun setMotivationalNotificationsEnabled(enabled: Boolean) {
        _motivationalNotificationsEnabled.value = enabled
        userPreferences.setMotivationalNotificationsEnabled(enabled)
        updateUserPreferenceInFirestore("motivationalNotificationsEnabled", enabled)
    }

    fun setHydrationRemindersEnabled(enabled: Boolean) {
        _hydrationRemindersEnabled.value = enabled
        userPreferences.setHydrationRemindersEnabled(enabled)
        updateUserPreferenceInFirestore("hydrationRemindersEnabled", enabled)
    }

    private fun updateUserPreferenceInFirestore(key: String, value: Any) {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.uid?.let { userId ->
                userRepository.updateUserProfileData(userId, mapOf(key to value))
            }
        }
    }
}