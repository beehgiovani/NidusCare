package com.developersbeeh.medcontrol.ui.reminders

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.Reminder
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.ReminderRepository // ✅ 1. Importação atualizada
import com.developersbeeh.medcontrol.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

private const val TAG = "RemindersViewModel"

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository, // ✅ 2. Injetando o novo repositório
    private val firestoreRepository: FirestoreRepository, // Mantido para o ID do usuário
    application: Application
) : AndroidViewModel(application) {

    private val scheduler = NotificationScheduler(application)
    private val _dependentId = MutableLiveData<String>()

    val reminders: LiveData<List<Reminder>> = _dependentId.switchMap { id ->
        if (id.isNotBlank()) {
            // ✅ 3. Chamando a função do novo repositório
            reminderRepository.getReminders(id).asLiveData()
        } else {
            MutableLiveData(emptyList())
        }
    }

    fun initialize(dependentId: String) {
        if (_dependentId.value == dependentId) return
        _dependentId.value = dependentId
    }

    fun addReminder(type: String, time: LocalTime) {
        val dependentId = _dependentId.value ?: return
        viewModelScope.launch {
            try {
                val userId = firestoreRepository.getCurrentUserId() ?: "dependent_user"
                val newReminder = Reminder(userId = userId, type = type)
                newReminder.time = time

                // ✅ 4. Usando o novo repositório para salvar
                reminderRepository.saveReminder(dependentId, newReminder)
                scheduler.scheduleReminder(newReminder, dependentId)
                Log.i(TAG, "Lembrete de tipo '$type' adicionado e agendado.")

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao salvar novo lembrete: ${e.message}", e)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        val dependentId = _dependentId.value ?: return
        viewModelScope.launch {
            try {
                // ✅ 5. Usando o novo repositório para deletar
                reminderRepository.deleteReminder(dependentId, reminder.id)
                scheduler.cancelReminder(reminder, dependentId)
                Log.i(TAG, "Lembrete com ID '${reminder.id}' deletado e cancelado.")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao deletar lembrete com ID '${reminder.id}': ${e.message}", e)
            }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        val dependentId = _dependentId.value ?: return
        viewModelScope.launch {
            try {
                // ✅ 6. Usando o novo repositório para atualizar
                reminderRepository.updateReminder(dependentId, reminder)
                if (reminder.isActive) {
                    scheduler.scheduleReminder(reminder, dependentId)
                } else {
                    scheduler.cancelReminder(reminder, dependentId)
                }
                Log.i(TAG, "Lembrete com ID '${reminder.id}' teve seu status alterado para ${reminder.isActive}.")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao alterar o status do lembrete: ${e.message}", e)
            }
        }
    }

    fun updateReminderTime(reminder: Reminder, newTime: LocalTime) {
        val dependentId = _dependentId.value ?: return
        viewModelScope.launch {
            try {
                val updatedReminder = reminder.copy()
                updatedReminder.time = newTime

                // ✅ 7. Usando o novo repositório para atualizar
                reminderRepository.updateReminder(dependentId, updatedReminder)
                if (updatedReminder.isActive) {
                    scheduler.scheduleReminder(updatedReminder, dependentId)
                }
                Log.i(TAG, "Horário do lembrete '${reminder.type}' atualizado e reagendado.")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao atualizar o horário do lembrete: ${e.message}", e)
            }
        }
    }
}