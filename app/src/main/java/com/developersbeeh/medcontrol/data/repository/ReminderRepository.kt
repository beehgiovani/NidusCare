package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getReminders(dependentId: String): Flow<List<Reminder>> = callbackFlow {
        if (dependentId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("dependentes").document(dependentId)
            .collection("reminders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reminders = snapshot.toObjects(Reminder::class.java)
                    trySend(reminders)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveReminder(dependentId: String, reminder: Reminder): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("reminders").document(reminder.id)
                .set(reminder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminder(dependentId: String, reminder: Reminder): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("reminders").document(reminder.id)
                .set(reminder).await() // Set sobrescreve, o que é seguro para este caso
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(dependentId: String, reminderId: String): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("reminders").document(reminderId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}