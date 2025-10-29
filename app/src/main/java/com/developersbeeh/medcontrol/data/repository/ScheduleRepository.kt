package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getSchedules(dependentId: String): Flow<List<AgendamentoSaude>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("agendamentos")
            .orderBy("timestampString", Query.Direction.ASCENDING) // Ordena do mais próximo para o mais distante
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val schedules = snapshot.toObjects(AgendamentoSaude::class.java)
                    trySend(schedules)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveSchedule(agendamento: AgendamentoSaude): Result<Unit> {
        return try {
            db.collection("dependentes").document(agendamento.dependentId)
                .collection("agendamentos").document(agendamento.id)
                .set(agendamento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(agendamento: AgendamentoSaude): Result<Unit> {
        return try {
            db.collection("dependentes").document(agendamento.dependentId)
                .collection("agendamentos").document(agendamento.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}