package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.audit.AuditAction
import com.developersbeeh.medcontrol.audit.AuditLogger
import com.developersbeeh.medcontrol.audit.EntityType
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.model.TipoAgendamento
import com.developersbeeh.medcontrol.util.advanced.DateTimeUtils
import com.developersbeeh.medcontrol.util.advanced.TextUtils
import com.developersbeeh.medcontrol.validation.AgendamentoSaudeValidator
import com.developersbeeh.medcontrol.validation.ValidationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ScheduleRepositoryEnhanced"

/**
 * Status de agendamento.
 */
enum class AgendamentoStatus {
    AGENDADO,
    CONFIRMADO,
    REALIZADO,
    CANCELADO,
    REMARCADO
}

/**
 * Filtros para busca de agendamentos.
 */
data class ScheduleFilters(
    val tipo: TipoAgendamento? = null,
    val status: AgendamentoStatus? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val profissional: String? = null,
    val local: String? = null
)

/**
 * Versão melhorada do ScheduleRepository com validações, status e auditoria.
 */
@Singleton
class ScheduleRepositoryEnhanced @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val auditLogger: AuditLogger
) {

    private val validator = AgendamentoSaudeValidator()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ==================== MÉTODOS BÁSICOS (MANTIDOS) ====================

    fun getSchedules(dependentId: String): Flow<List<AgendamentoSaude>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("agendamentos")
            .orderBy("timestampString", Query.Direction.ASCENDING)
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

    // ==================== MÉTODOS MELHORADOS ====================

    /**
     * Valida um agendamento sem salvá-lo.
     */
    fun validateAgendamento(agendamento: AgendamentoSaude): ValidationResult {
        return validator.validate(agendamento)
    }

    /**
     * Verifica conflitos de horário com outros agendamentos.
     */
    suspend fun checkConflicts(
        dependentId: String,
        timestamp: LocalDateTime,
        durationMinutes: Int = 60,
        excludeId: String? = null
    ): List<AgendamentoSaude> {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .whereEqualTo("isActive", true)
                .get().await()

            val allSchedules = snapshot.toObjects(AgendamentoSaude::class.java)
                .filter { it.id != excludeId }

            val endTime = timestamp.plusMinutes(durationMinutes.toLong())

            allSchedules.filter { schedule ->
                val scheduleStart = schedule.timestamp
                val scheduleEnd = scheduleStart.plusMinutes(durationMinutes.toLong())
                
                DateTimeUtils.periodsOverlap(timestamp, endTime, scheduleStart, scheduleEnd)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking conflicts", e)
            emptyList()
        }
    }

    /**
     * Salva um agendamento com validação e verificação de conflitos.
     */
    suspend fun saveSchedule(
        agendamento: AgendamentoSaude,
        checkConflicts: Boolean = true
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar agendamento
        val validationResult = validator.validate(agendamento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        // Verificar conflitos se solicitado
        if (checkConflicts) {
            val conflicts = checkConflicts(
                dependentId = agendamento.dependentId,
                timestamp = agendamento.timestamp,
                excludeId = agendamento.id
            )
            
            if (conflicts.isNotEmpty()) {
                val conflictInfo = conflicts.joinToString(", ") { it.titulo }
                Log.w(TAG, "Conflito de horário detectado: $conflictInfo")
                // Não bloqueia, apenas avisa
            }
        }

        return try {
            val isUpdate = getSchedule(agendamento.dependentId, agendamento.id) != null

            db.collection("dependentes").document(agendamento.dependentId)
                .collection("agendamentos").document(agendamento.id)
                .set(agendamento).await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.AGENDAMENTO,
                entityId = agendamento.id,
                dependentId = agendamento.dependentId,
                userId = userId,
                action = if (isUpdate) AuditAction.UPDATE else AuditAction.CREATE,
                metadata = mapOf(
                    "titulo" to agendamento.titulo,
                    "tipo" to agendamento.tipo.name,
                    "timestamp" to agendamento.timestampString
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving schedule", e)
            Result.failure(e)
        }
    }

    /**
     * Obtém um agendamento específico.
     */
    suspend fun getSchedule(dependentId: String, scheduleId: String): AgendamentoSaude? {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos").document(scheduleId)
                .get().await()
            
            snapshot.toObject(AgendamentoSaude::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting schedule", e)
            null
        }
    }

    /**
     * Atualiza o status de um agendamento (ativo/inativo).
     */
    suspend fun updateStatus(
        agendamento: AgendamentoSaude,
        newStatus: Boolean,
        notes: String? = null
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar mudança de status
        val validationResult = validator.validateStatusChange(agendamento, newStatus)
        if (validationResult.isError()) {
            Log.w(TAG, "Status change warnings: ${validationResult.getWarnings()}")
        }

        return try {
            db.collection("dependentes").document(agendamento.dependentId)
                .collection("agendamentos").document(agendamento.id)
                .update("isActive", newStatus).await()

            // Auditoria
            auditLogger.logScheduleStatusChange(
                dependentId = agendamento.dependentId,
                scheduleId = agendamento.id,
                oldStatus = agendamento.isActive.toString(),
                newStatus = newStatus.toString(),
                userId = userId
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status", e)
            Result.failure(e)
        }
    }

    /**
     * Deleta um agendamento com auditoria.
     */
    suspend fun deleteSchedule(agendamento: AgendamentoSaude): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar se pode deletar
        val validationResult = validator.validateForDeletion(agendamento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        return try {
            db.collection("dependentes").document(agendamento.dependentId)
                .collection("agendamentos").document(agendamento.id)
                .delete().await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.AGENDAMENTO,
                entityId = agendamento.id,
                dependentId = agendamento.dependentId,
                userId = userId,
                action = AuditAction.DELETE,
                metadata = mapOf(
                    "titulo" to agendamento.titulo,
                    "timestamp" to agendamento.timestampString
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting schedule", e)
            Result.failure(e)
        }
    }

    /**
     * Busca agendamentos com filtros avançados.
     */
    suspend fun searchSchedules(
        dependentId: String,
        query: String,
        filters: ScheduleFilters = ScheduleFilters()
    ): List<AgendamentoSaude> {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .get().await()

            val allSchedules = snapshot.toObjects(AgendamentoSaude::class.java)
            
            allSchedules.filter { schedule ->
                // Filtro de texto
                val matchesQuery = query.isBlank() || 
                    TextUtils.containsIgnoreCase(schedule.titulo, query) ||
                    TextUtils.containsIgnoreCase(schedule.local ?: "", query) ||
                    TextUtils.containsIgnoreCase(schedule.profissional ?: "", query) ||
                    TextUtils.containsIgnoreCase(schedule.notasDePreparo ?: "", query)

                // Filtro de tipo
                val matchesTipo = filters.tipo == null || schedule.tipo == filters.tipo

                // Filtro de status (isActive)
                val matchesStatus = filters.status == null || 
                    (filters.status == AgendamentoStatus.AGENDADO && schedule.isActive) ||
                    (filters.status == AgendamentoStatus.CANCELADO && !schedule.isActive)

                // Filtro de data
                val matchesDate = try {
                    if (filters.startDate == null && filters.endDate == null) {
                        true
                    } else {
                        val scheduleDate = schedule.timestamp.toLocalDate()
                        val afterStart = filters.startDate == null || !scheduleDate.isBefore(filters.startDate)
                        val beforeEnd = filters.endDate == null || !scheduleDate.isAfter(filters.endDate)
                        afterStart && beforeEnd
                    }
                } catch (e: Exception) {
                    true
                }

                // Filtro de profissional
                val matchesProfissional = filters.profissional == null ||
                    TextUtils.areSimilar(schedule.profissional ?: "", filters.profissional)

                // Filtro de local
                val matchesLocal = filters.local == null ||
                    TextUtils.areSimilar(schedule.local ?: "", filters.local)

                matchesQuery && matchesTipo && matchesStatus && matchesDate && 
                    matchesProfissional && matchesLocal
            }.sortedBy { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching schedules", e)
            emptyList()
        }
    }

    /**
     * Obtém agendamentos próximos (próximos N dias).
     */
    suspend fun getUpcomingSchedules(dependentId: String, days: Int = 7): List<AgendamentoSaude> {
        return try {
            val now = LocalDateTime.now()
            val futureDate = now.plusDays(days.toLong())
            val futureString = futureDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .whereEqualTo("isActive", true)
                .whereLessThanOrEqualTo("timestampString", futureString)
                .orderBy("timestampString", Query.Direction.ASCENDING)
                .get().await()

            snapshot.toObjects(AgendamentoSaude::class.java)
                .filter { it.timestamp.isAfter(now) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting upcoming schedules", e)
            emptyList()
        }
    }

    /**
     * Obtém agendamentos passados.
     */
    suspend fun getPastSchedules(dependentId: String, limit: Int = 50): List<AgendamentoSaude> {
        return try {
            val now = LocalDateTime.now()
            val nowString = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .whereLessThan("timestampString", nowString)
                .orderBy("timestampString", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            snapshot.toObjects(AgendamentoSaude::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting past schedules", e)
            emptyList()
        }
    }

    /**
     * Obtém estatísticas de agendamentos por tipo.
     */
    suspend fun getScheduleStatsByType(dependentId: String): Map<TipoAgendamento, Int> {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .get().await()

            val schedules = snapshot.toObjects(AgendamentoSaude::class.java)
            schedules.groupingBy { it.tipo }.eachCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting schedule stats", e)
            emptyMap()
        }
    }

    /**
     * Obtém o próximo agendamento ativo.
     */
    suspend fun getNextSchedule(dependentId: String): AgendamentoSaude? {
        return try {
            val now = LocalDateTime.now()
            val nowString = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .whereEqualTo("isActive", true)
                .whereGreaterThan("timestampString", nowString)
                .orderBy("timestampString", Query.Direction.ASCENDING)
                .limit(1)
                .get().await()

            snapshot.toObjects(AgendamentoSaude::class.java).firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting next schedule", e)
            null
        }
    }
}

