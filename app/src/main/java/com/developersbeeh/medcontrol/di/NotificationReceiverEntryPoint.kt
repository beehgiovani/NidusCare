// src/main/java/com/developersbeeh/medcontrol/di/NotificationReceiverEntryPoint.kt
package com.developersbeeh.medcontrol.di

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import com.developersbeeh.medcontrol.data.repository.ReminderRepository // ✅ IMPORTAÇÃO ADICIONADA
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationReceiverEntryPoint {
    fun getFirestoreRepository(): FirestoreRepository
    fun getMedicationRepository(): MedicationRepository
    fun getReminderRepository(): ReminderRepository // ✅ FUNÇÃO ADICIONADA
}