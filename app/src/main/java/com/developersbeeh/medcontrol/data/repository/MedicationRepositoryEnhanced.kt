package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.audit.AccessAction
import com.developersbeeh.medcontrol.audit.AuditAction
import com.developersbeeh.medcontrol.audit.AuditLogger
import com.developersbeeh.medcontrol.audit.EntityType
import com.developersbeeh.medcontrol.data.model.Conquista
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.RecordedDoseStatus
import com.developersbeeh.medcontrol.data.model.TipoConquista
import com.developersbeeh.medcontrol.notifications.NotificationScheduler
import com.developersbeeh.medcontrol.util.InvalidIdException
import com.developersbeeh.medcontrol.util.UserNotAuthenticatedException
import com.developersbeeh.medcontrol.util.advanced.TextUtils
import com.developersbeeh.medcontrol.validation.MedicamentoValidator
import com.developersbeeh.medcontrol.validation.ValidationResult
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

private const val TAG = "MedicationRepositoryEnhanced"

/**
 * Versão melhorada do MedicationRepository com validações, auditoria e cenários avançados.
 * Mantém compatibilidade total com os data models existentes.
 */
@Singleton
class MedicationRepositoryEnhanced @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val achievementRepository: AchievementRepository,
    private val auditLogger: AuditLogger,
    @ApplicationContext private val context: Context
) {

    private val scheduler by lazy { NotificationScheduler(context) }
    private val validator = MedicamentoValidator()

    private fun getMedicamentosCollection(dependentId: String) =
        db.collection("dependentes").document(dependentId).collection("medicamentos")

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // ==================== MÉTODOS BÁSICOS (MANTIDOS) ====================

    fun getMedicamentos(dependentId: String): Flow<List<Medicamento>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
            return@callbackFlow
        }

        val listener = getMedicamentosCollection(dependentId)
            .whereEqualTo("isArchived", false)
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

    // ==================== MÉTODOS MELHORADOS ====================

    /**
     * Salva um medicamento com validação completa e auditoria.
     */
    suspend fun saveMedicamento(
        dependentId: String,
        medicamento: Medicamento,
        skipValidation: Boolean = false
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
        }

        // Validação
        if (!skipValidation) {
            val validationResult = validator.validate(medicamento)
            if (validationResult.isError() && validationResult.hasCriticalErrors()) {
                val errors = validationResult.getCriticalErrors()
                    .joinToString(", ") { "${it.field}: ${it.message}" }
                return Result.failure(Exception("Erros de validação: $errors"))
            }
        }

        val updatedMedicamento = medicamento.copy(userId = userId)
        
        return try {
            // Buscar medicamento anterior para auditoria
            val oldMedicamento = getMedicamento(dependentId, medicamento.id)
            
            getMedicamentosCollection(dependentId).document(updatedMedicamento.id)
                .set(updatedMedicamento).await()

            // Auditoria
            if (oldMedicamento != null) {
                val changes = detectChanges(oldMedicamento, updatedMedicamento)
                if (changes.isNotEmpty()) {
                    auditLogger.logMedicationChange(
                        dependentId = dependentId,
                        medicamentoId = medicamento.id,
                        changes = changes,
                        userId = userId,
                        action = AuditAction.UPDATE
                    )
                }
            } else {
                auditLogger.logMedicationChange(
                    dependentId = dependentId,
                    medicamentoId = medicamento.id,
                    changes = emptyMap(),
                    userId = userId,
                    action = AuditAction.CREATE
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_saving_medication, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Valida um medicamento sem salvá-lo.
     */
    fun validateMedicamento(medicamento: Medicamento): ValidationResult {
        return validator.validate(medicamento)
    }

    /**
     * Verifica se existem medicamentos duplicados (mesmo nome e dosagem).
     */
    suspend fun checkDuplicates(
        dependentId: String,
        nome: String,
        dosagem: String,
        excludeId: String? = null
    ): List<Medicamento> {
        if (dependentId.isBlank()) return emptyList()

        return try {
            val snapshot = getMedicamentosCollection(dependentId)
                .whereEqualTo("isArchived", false)
                .get()
                .await()

            val allMedicamentos = snapshot.toObjects(Medicamento::class.java)
            
            allMedicamentos.filter { med ->
                med.id != excludeId &&
                TextUtils.areSimilar(med.nome, nome) &&
                TextUtils.areSimilar(med.dosagem, dosagem)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking duplicates", e)
            emptyList()
        }
    }

    /**
     * Arquiva um medicamento com auditoria.
     */
    suspend fun archiveMedicamento(
        dependentId: String,
        medicamentoId: String,
        reason: String? = null
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        
        if (medicamentoId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        }

        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId)
                .update("isArchived", true).await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.MEDICAMENTO,
                entityId = medicamentoId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.ARCHIVE,
                metadata = mapOf("reason" to reason)
            )

            // TODO: Cancelar notificações quando implementado
            // scheduler.cancelMedicationNotifications(medicamentoId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Desarquiva um medicamento com auditoria.
     */
    suspend fun unarchiveMedicamento(dependentId: String, medicamentoId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        
        if (medicamentoId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        }

        return try {
            getMedicamentosCollection(dependentId).document(medicamentoId)
                .update("isArchived", false).await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.MEDICAMENTO,
                entityId = medicamentoId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.UNARCHIVE
            )

            // TODO: Reagendar notificações quando implementado
            // val medicamento = getMedicamento(dependentId, medicamentoId)
            // if (medicamento != null && medicamento.usaNotificacao && !medicamento.isPaused) {
            //     scheduler.scheduleMedicationNotifications(medicamento)
            // }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deleta permanentemente um medicamento com auditoria.
     */
    suspend fun permanentlyDeleteMedicamento(dependentId: String, medicamentoId: String): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        
        if (medicamentoId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_medication_id)))
        }

        return try {
            // Buscar medicamento antes de deletar para auditoria
            val medicamento = getMedicamento(dependentId, medicamentoId)
            
            getMedicamentosCollection(dependentId).document(medicamentoId).delete().await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.MEDICAMENTO,
                entityId = medicamentoId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.DELETE,
                metadata = mapOf("medicamentoNome" to medicamento?.nome)
            )

            // TODO: Cancelar notificações quando implementado
            // scheduler.cancelMedicationNotifications(medicamentoId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== GERENCIAMENTO DE ESTOQUE ====================

    /**
     * Adiciona um lote de estoque com validação.
     */
    suspend fun addStockLot(dependentId: String, medId: String, newLot: EstoqueLote): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        
        if (dependentId.isBlank() || medId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_ids)))
        }

        // Validar lote
        if (newLot.quantidade < 0) {
            return Result.failure(Exception("Quantidade do lote não pode ser negativa"))
        }
        if (newLot.dataValidade.isBefore(LocalDate.now())) {
            return Result.failure(Exception("Data de validade não pode estar no passado"))
        }

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

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.MEDICAMENTO,
                entityId = medId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.UPDATE,
                metadata = mapOf(
                    "action" to "add_stock_lot",
                    "loteId" to newLot.id,
                    "quantidade" to newLot.quantidade
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_adding_stock_lot, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Ajusta manualmente o estoque de um lote específico.
     */
    suspend fun adjustStock(
        dependentId: String,
        medicamentoId: String,
        loteId: String,
        adjustment: Double,
        reason: String
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(UserNotAuthenticatedException())
        
        val medRef = getMedicamentosCollection(dependentId).document(medicamentoId)
        
        return try {
            db.runTransaction { transaction ->
                val med = transaction.get(medRef).toObject(Medicamento::class.java)
                    ?: throw Exception("Medicamento não encontrado")
                
                val updatedLotes = med.lotes.map { lote ->
                    if (lote.id == loteId) {
                        val newQuantity = (lote.quantidade + adjustment).coerceAtLeast(0.0)
                        lote.copy(quantidade = newQuantity)
                    } else {
                        lote
                    }
                }
                
                transaction.update(medRef, "lotes", updatedLotes)
            }.await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.MEDICAMENTO,
                entityId = medicamentoId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.UPDATE,
                metadata = mapOf(
                    "action" to "adjust_stock",
                    "loteId" to loteId,
                    "adjustment" to adjustment,
                    "reason" to reason
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting stock", e)
            Result.failure(e)
        }
    }

    /**
     * Obtém lotes próximos do vencimento.
     */
    suspend fun getExpiringLots(
        dependentId: String,
        daysThreshold: Int = 30
    ): List<Pair<Medicamento, EstoqueLote>> {
        return try {
            val snapshot = getMedicamentosCollection(dependentId)
                .whereEqualTo("isArchived", false)
                .get()
                .await()

            val medicamentos = snapshot.toObjects(Medicamento::class.java)
            val expiringLots = mutableListOf<Pair<Medicamento, EstoqueLote>>()
            
            val thresholdDate = LocalDate.now().plusDays(daysThreshold.toLong())
            
            medicamentos.forEach { med ->
                med.lotes.forEach { lote ->
                    if (lote.dataValidade.isBefore(thresholdDate) && 
                        lote.dataValidade.isAfter(LocalDate.now()) &&
                        lote.quantidade > 0) {
                        expiringLots.add(Pair(med, lote))
                    }
                }
            }
            
            expiringLots.sortedBy { it.second.dataValidade }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expiring lots", e)
            emptyList()
        }
    }

    // ==================== REGISTRO DE DOSES ====================

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
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
        }

        // Validar medicamento para registro de dose
        val validationResult = validator.validateForDoseRecording(medicamento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception(errors))
        }

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
            val historyRef = db.collection("dependentes").document(dependentId)
                .collection("historico_doses").document()
            
            db.runTransaction { transaction ->
                val medOnDb = transaction.get(medRef).toObject(Medicamento::class.java)
                    ?: throw Exception(context.getString(R.string.error_medication_not_found))
                
                transaction.set(historyRef, doseHistory)

                // Atualizar estoque
                if (medOnDb.lotes.isNotEmpty()) {
                    val doseValue = quantidadeAdministrada 
                        ?: TextUtils.extractDosageValue(medOnDb.dosagem) 
                        ?: 1.0
                    
                    val validLots = medOnDb.lotes
                        .filter { it.dataValidade.isAfter(LocalDate.now().minusDays(1)) && it.quantidade > 0 }
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

            // Auditoria
            auditLogger.logDoseRecording(
                dependentId = dependentId,
                medicamentoId = medicamento.id,
                doseHistoryId = historyRef.id,
                userId = currentUserId,
                status = RecordedDoseStatus.TAKEN.name,
                metadata = mapOf(
                    "localDeAplicacao" to localDeAplicacao,
                    "quantidadeAdministrada" to quantidadeAdministrada,
                    "notas" to notas
                )
            )

            scheduler.cancelMissedDoseFollowUp(medicamento.id)
            checkAndAwardDoseCountAchievements(dependentId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.error_recording_dose, e.message)
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    suspend fun recordSkippedDose(
        dependentId: String,
        medicamento: Medicamento,
        reason: String?,
        scheduledTime: LocalDateTime
    ): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: "system_user"
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException(context.getString(R.string.error_invalid_dependent_id)))
        }

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
            val historyRef = db.collection("dependentes").document(dependentId)
                .collection("historico_doses").document()
            
            historyRef.set(doseHistory).await()

            // Auditoria
            auditLogger.logDoseRecording(
                dependentId = dependentId,
                medicamentoId = medicamento.id,
                doseHistoryId = historyRef.id,
                userId = currentUserId,
                status = RecordedDoseStatus.SKIPPED.name,
                metadata = mapOf("reason" to reason)
            )

            scheduler.cancelMissedDoseFollowUp(medicamento.id)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.error_recording_skipped_dose), e)
            Result.failure(e)
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private suspend fun checkAndAwardDoseCountAchievements(dependentId: String) {
        try {
            val doseCount = db.collection("dependentes").document(dependentId)
                .collection("historico_doses")
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                .count
            
            if (doseCount == 10L) {
                achievementRepository.awardAchievement(
                    dependentId,
                    Conquista(
                        id = TipoConquista.DEZ_DOSES_REGISTRADAS.name,
                        tipo = TipoConquista.DEZ_DOSES_REGISTRADAS
                    )
                )
            }
            if (doseCount == 100L) {
                achievementRepository.awardAchievement(
                    dependentId,
                    Conquista(
                        id = TipoConquista.CEM_DOSES_REGISTRADAS.name,
                        tipo = TipoConquista.CEM_DOSES_REGISTRADAS
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.error_checking_achievements, e.message), e)
        }
    }

    /**
     * Detecta mudanças entre dois medicamentos para auditoria.
     */
    private fun detectChanges(old: Medicamento, new: Medicamento): Map<String, Pair<Any?, Any?>> {
        val changes = mutableMapOf<String, Pair<Any?, Any?>>()
        
        if (old.nome != new.nome) changes["nome"] = Pair(old.nome, new.nome)
        if (old.dosagem != new.dosagem) changes["dosagem"] = Pair(old.dosagem, new.dosagem)
        if (old.isPaused != new.isPaused) changes["isPaused"] = Pair(old.isPaused, new.isPaused)
        if (old.isUsoContinuo != new.isUsoContinuo) changes["isUsoContinuo"] = Pair(old.isUsoContinuo, new.isUsoContinuo)
        if (old.isUsoEsporadico != new.isUsoEsporadico) changes["isUsoEsporadico"] = Pair(old.isUsoEsporadico, new.isUsoEsporadico)
        if (old.frequenciaTipo != new.frequenciaTipo) changes["frequenciaTipo"] = Pair(old.frequenciaTipo, new.frequenciaTipo)
        if (old.horarios != new.horarios) changes["horarios"] = Pair(old.horarios, new.horarios)
        if (old.nivelDeAlertaEstoque != new.nivelDeAlertaEstoque) changes["nivelDeAlertaEstoque"] = Pair(old.nivelDeAlertaEstoque, new.nivelDeAlertaEstoque)
        
        return changes
    }
}

