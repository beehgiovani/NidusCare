package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.RecordedDoseStatus
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.notifications.NotificationScheduler
import com.developersbeeh.medcontrol.util.InvalidIdException
import com.developersbeeh.medcontrol.util.UserNotAuthenticatedException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MedicationRepository"

@Singleton
class MedicationRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val achievementRepository: AchievementRepository,
    @ApplicationContext private val context: Context
) {

    private val scheduler by lazy { NotificationScheduler(context) }

    private fun getMedicamentosCollection(dependentId: String) =
        db.collection("dependentes").document(dependentId).collection("medicamentos")

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getMedicamentos(dependentId: String): Flow<List<Medicamento>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
            return@callbackFlow
        }

        val listener = getMedicamentosCollection(dependentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects(Medicamento::class.java)) }
            }
        awaitClose { listener.remove() }
    }

    fun getArchivedMedicamentos(dependentId: String): Flow<List<Medicamento>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
            return@callbackFlow
        }

        val listener = getMedicamentosCollection(dependentId)
            .whereEqualTo("isArchived", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                snapshot?.let { trySend(it.toObjects(Medicamento::class.java)) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getMedicamento(dependentId: String, medicamentoId: String): Medicamento? {
        if (dependentId.isBlank() || medicamentoId.isBlank()) return null
        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId)
                .get().await().toObject(Medicamento::class.java)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.error_fetching_medication, medicamentoId, e.message), e)
            null
        }
    }

    suspend fun saveMedicamento(dependentId: String, medicamento: Medicamento): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        if (dependentId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
        val updatedMedicamento = medicamento.copy(userId = userId)
        return try {
            getMedicamentosCollection(dependentId).document(updatedMedicamento.id)
                .set(updatedMedicamento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_saving_medication, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    suspend fun archiveMedicamento(dependentId: String, medicamentoId: String): Result<Unit> {
        if (medicamentoId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId).update("isArchived", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unarchiveMedicamento(dependentId: String, medicamentoId: String): Result<Unit> {
        if (medicamentoId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId).update("isArchived", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun permanentlyDeleteMedicamento(dependentId: String, medicamentoId: String): Result<Unit> {
        if (medicamentoId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStockLot(dependentId: String, medId: String, newLot: EstoqueLote): Result<Unit> {
        if (dependentId.isBlank() || medId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_ids)))
        val medRef = getMedicamentosCollection(dependentId).document(medId)
        return try {
            db.runTransaction { transaction ->
                val medOnDb = transaction.get(medRef).toObject(Medicamento::class.java)
                    ?: throw Exception(context.getString(R.string.error_medication_not_found))
                val updatedLotes = medOnDb.lotes.toMutableList().apply { add(newLot) }
                val updates = hashMapOf<String, Any>("lotes" to updatedLotes)
                if (medOnDb.alertaDeEstoqueEnviado) {
                    updates["alertaDeEstoqueEnviado"] = false
                }
                transaction.update(medRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_adding_stock_lot, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    fun getDoseHistory(dependentId: String): Flow<List<DoseHistory>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
            return@callbackFlow
        }
        val listener = db.collection("dependentes").document(dependentId)
            .collection("historico_doses")
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects(DoseHistory::class.java)) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun recordDoseAndUpdateStock(
        dependentId: String,
        medicamento: Medicamento,
        localDeAplicacao: String?,
        quantidadeAdministrada: Double?,
        notas: String?,
        doseTime: LocalDateTime = LocalDateTime.now()
    ): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: "dependent_user"
        if (dependentId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))

        val doseHistory = DoseHistory(
            medicamentoId = medicamento.id,
            userId = currentUserId,
            localDeAplicacao = localDeAplicacao,
            quantidadeAdministrada = quantidadeAdministrada,
            notas = notas,
            status = RecordedDoseStatus.TAKEN
        ).apply {
            timestamp = doseTime
        }

        return try {
            val medRef = getMedicamentosCollection(dependentId).document(medicamento.id)
            db.runTransaction { transaction ->
                val medOnDb = transaction.get(medRef).toObject(Medicamento::class.java)
                    ?: throw Exception(context.getString(R.string.error_medication_not_found))
                val historyRef = db.collection("dependentes").document(dependentId).collection("historico_doses").document()
                transaction.set(historyRef, doseHistory)

                if (medOnDb.lotes.isNotEmpty()) {
                    val doseValue = quantidadeAdministrada ?: Regex("^(\\d*\\.?\\d+)").find(medOnDb.dosagem.replace(',', '.'))
                        ?.value?.toDoubleOrNull() ?: 1.0
                    val validLots = medOnDb.lotes
                        .filter { it.dataValidade.isAfter(LocalDate.now().minusDays(1)) }
                        .sortedBy { it.dataValidade }
                    if (validLots.isNotEmpty()) {
                        val lotToConsume = validLots.first()
                        val newQuantity = (lotToConsume.quantidade - doseValue).coerceAtLeast(0.0)
                        val updatedLotes = medOnDb.lotes.toMutableList()
                        val index = updatedLotes.indexOfFirst { it.id == lotToConsume.id }
                        if (index != -1) {
                            if (newQuantity > 0) {
                                updatedLotes[index] = lotToConsume.copy(quantidade = newQuantity)
                            } else {
                                updatedLotes.removeAt(index)
                            }
                            transaction.update(medRef, "lotes", updatedLotes)
                        }
                    }
                }
                if (medOnDb.missedDoseAlertSent) {
                    transaction.update(medRef, "missedDoseAlertSent", false)
                }
            }.await()

            scheduler.cancelMissedDoseFollowUp(medicamento.id)
            checkAndAwardDoseCountAchievements(dependentId)
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_recording_dose, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    // ✅ REMOVIDO: Função duplicada 'recordDoseAsSkipped' foi removida.

    suspend fun recordSkippedDose(dependentId: String, medicamento: Medicamento, reason: String?, scheduledTime: LocalDateTime): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: "system_user"
        if (dependentId.isBlank()) return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))

        val doseHistory = DoseHistory(
            medicamentoId = medicamento.id,
            userId = currentUserId,
            notas = reason,
            status = RecordedDoseStatus.SKIPPED,
            quantidadeAdministrada = null,
            localDeAplicacao = null
        ).apply {
            timestamp = scheduledTime
        }

        return try {
            db.collection("dependentes").document(dependentId)
                .collection("historico_doses").add(doseHistory).await()
            scheduler.cancelMissedDoseFollowUp(medicamento.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.error_recording_skipped_dose), e)
            Result.failure(e)
        }
    }

    private suspend fun checkAndAwardDoseCountAchievements(dependentId: String) {
        try {
            val doseCount = db.collection("dependentes").document(dependentId)
                .collection("historico_doses")
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                .count
            if (doseCount == 10L) {
                achievementRepository.awardAchievement(dependentId, Conquista(id = TipoConquista.DEZ_DOSES_REGISTRADAS.name, tipo = TipoConquista.DEZ_DOSES_REGISTRADAS))
            }
            if (doseCount == 100L) {
                achievementRepository.awardAchievement(dependentId, Conquista(id = TipoConquista.CEM_DOSES_REGISTRADAS.name, tipo = TipoConquista.CEM_DOSES_REGISTRADAS))
            }
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.error_checking_achievements, e.message), e)
        }
    }
}