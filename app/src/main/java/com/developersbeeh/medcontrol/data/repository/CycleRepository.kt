package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.data.model.DailyCycleLog
import com.google.firebase.firestore.FieldPath // <-- 1. ADICIONE ESTE IMPORT
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    private fun getCollectionRef(dependentId: String) =
        db.collection("dependentes")
            .document(dependentId)
            .collection("daily_cycle_logs")

    fun getDailyLogs(dependentId: String, startDate: LocalDate): Flow<List<DailyCycleLog>> {
        return getCollectionRef(dependentId)
            // --- 2. ALTERE A CONSULTA AQUI ---
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), startDate.toString())
            .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<DailyCycleLog>()
            }
            .catch { exception ->
                Log.e("CycleRepository", "Erro ao obter ou converter os logs do ciclo", exception)
                emit(emptyList())
            }
    }

    suspend fun saveOrUpdateDailyLog(dependentId: String, log: DailyCycleLog): Result<Unit> {
        return try {
            if (dependentId.isBlank()) {
                val errorMsg = "ID do dependente está vazio. Não é possível salvar o log."
                Log.e("CycleRepository", errorMsg)
                return Result.failure(IllegalArgumentException(errorMsg))
            }

            getCollectionRef(dependentId)
                .document(log.dateString)
                .set(log)
                .await()

            Log.d("CycleRepository", "✅ Log salvo com sucesso para dep=$dependentId, data=${log.dateString}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CycleRepository", "❌ Erro ao salvar log para dep=$dependentId, data=${log.dateString}", e)
            Result.failure(e)
        }
    }
}