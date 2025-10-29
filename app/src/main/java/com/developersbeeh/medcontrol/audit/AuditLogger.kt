package com.developersbeeh.medcontrol.audit

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sistema de auditoria para registrar ações importantes no aplicativo.
 * Mantém um log de todas as operações críticas para rastreabilidade e segurança.
 */
@Singleton
class AuditLogger @Inject constructor(
    private val db: FirebaseFirestore
) {

    companion object {
        private const val TAG = "AuditLogger"
        private const val AUDIT_COLLECTION = "audit_logs"
    }

    /**
     * Registra uma alteração em um medicamento.
     */
    suspend fun logMedicationChange(
        dependentId: String,
        medicamentoId: String,
        changes: Map<String, Pair<Any?, Any?>>,
        userId: String,
        action: AuditAction = AuditAction.UPDATE
    ) {
        try {
            val auditLog = AuditLog(
                entityType = EntityType.MEDICAMENTO,
                entityId = medicamentoId,
                dependentId = dependentId,
                userId = userId,
                action = action,
                changes = changes.mapValues { (_, pair) ->
                    mapOf("old" to pair.first, "new" to pair.second)
                },
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            db.collection(AUDIT_COLLECTION)
                .add(auditLog.toMap())
                .await()

            Log.d(TAG, "Medication change logged: $medicamentoId by $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log medication change", e)
        }
    }

    /**
     * Registra um acesso a documento.
     */
    suspend fun logDocumentAccess(
        dependentId: String,
        documentId: String,
        userId: String,
        action: AccessAction
    ) {
        try {
            val auditLog = AuditLog(
                entityType = EntityType.DOCUMENTO,
                entityId = documentId,
                dependentId = dependentId,
                userId = userId,
                action = when (action) {
                    AccessAction.VIEW -> AuditAction.VIEW
                    AccessAction.DOWNLOAD -> AuditAction.DOWNLOAD
                    AccessAction.SHARE -> AuditAction.SHARE
                    AccessAction.DELETE -> AuditAction.DELETE
                    AccessAction.EDIT -> AuditAction.UPDATE
                },
                metadata = mapOf("accessType" to action.name),
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            db.collection(AUDIT_COLLECTION)
                .add(auditLog.toMap())
                .await()

            Log.d(TAG, "Document access logged: $documentId by $userId - $action")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log document access", e)
        }
    }

    /**
     * Registra uma mudança de status de agendamento.
     */
    suspend fun logScheduleStatusChange(
        dependentId: String,
        scheduleId: String,
        oldStatus: String,
        newStatus: String,
        userId: String
    ) {
        try {
            val auditLog = AuditLog(
                entityType = EntityType.AGENDAMENTO,
                entityId = scheduleId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.STATUS_CHANGE,
                changes = mapOf(
                    "status" to mapOf("old" to oldStatus, "new" to newStatus)
                ),
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            db.collection(AUDIT_COLLECTION)
                .add(auditLog.toMap())
                .await()

            Log.d(TAG, "Schedule status change logged: $scheduleId by $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log schedule status change", e)
        }
    }

    /**
     * Registra um registro de dose.
     */
    suspend fun logDoseRecording(
        dependentId: String,
        medicamentoId: String,
        doseHistoryId: String,
        userId: String,
        status: String,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        try {
            val auditLog = AuditLog(
                entityType = EntityType.DOSE_HISTORY,
                entityId = doseHistoryId,
                dependentId = dependentId,
                userId = userId,
                action = AuditAction.CREATE,
                metadata = metadata + mapOf(
                    "medicamentoId" to medicamentoId,
                    "doseStatus" to status
                ),
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            db.collection(AUDIT_COLLECTION)
                .add(auditLog.toMap())
                .await()

            Log.d(TAG, "Dose recording logged: $doseHistoryId by $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log dose recording", e)
        }
    }

    /**
     * Registra uma ação genérica.
     */
    suspend fun logAction(
        entityType: EntityType,
        entityId: String,
        dependentId: String,
        userId: String,
        action: AuditAction,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        try {
            val auditLog = AuditLog(
                entityType = entityType,
                entityId = entityId,
                dependentId = dependentId,
                userId = userId,
                action = action,
                metadata = metadata,
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            db.collection(AUDIT_COLLECTION)
                .add(auditLog.toMap())
                .await()

            Log.d(TAG, "Action logged: $action on $entityType:$entityId by $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log action", e)
        }
    }

    /**
     * Obtém logs de auditoria para um dependente específico.
     */
    suspend fun getAuditLogs(
        dependentId: String,
        limit: Int = 100
    ): List<AuditLog> {
        return try {
            val snapshot = db.collection(AUDIT_COLLECTION)
                .whereEqualTo("dependentId", dependentId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    AuditLog.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse audit log: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get audit logs", e)
            emptyList()
        }
    }

    /**
     * Obtém logs de auditoria para uma entidade específica.
     */
    suspend fun getEntityAuditLogs(
        entityType: EntityType,
        entityId: String,
        limit: Int = 50
    ): List<AuditLog> {
        return try {
            val snapshot = db.collection(AUDIT_COLLECTION)
                .whereEqualTo("entityType", entityType.name)
                .whereEqualTo("entityId", entityId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    AuditLog.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse audit log: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get entity audit logs", e)
            emptyList()
        }
    }
}

/**
 * Representa um log de auditoria.
 */
data class AuditLog(
    val entityType: EntityType,
    val entityId: String,
    val dependentId: String,
    val userId: String,
    val action: AuditAction,
    val changes: Map<String, Map<String, Any?>> = emptyMap(),
    val metadata: Map<String, Any?> = emptyMap(),
    val timestamp: String
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "entityType" to entityType.name,
            "entityId" to entityId,
            "dependentId" to dependentId,
            "userId" to userId,
            "action" to action.name,
            "changes" to changes,
            "metadata" to metadata,
            "timestamp" to timestamp,
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): AuditLog {
            return AuditLog(
                entityType = EntityType.valueOf(map["entityType"] as? String ?: "UNKNOWN"),
                entityId = map["entityId"] as? String ?: "",
                dependentId = map["dependentId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                action = AuditAction.valueOf(map["action"] as? String ?: "UNKNOWN"),
                changes = (map["changes"] as? Map<String, Map<String, Any?>>)?.mapValues { (_, value) ->
                    value
                } ?: emptyMap(),
                metadata = map["metadata"] as? Map<String, Any?> ?: emptyMap(),
                timestamp = map["timestamp"] as? String ?: ""
            )
        }
    }
}

/**
 * Tipos de entidades que podem ser auditadas.
 */
enum class EntityType {
    MEDICAMENTO,
    DOCUMENTO,
    AGENDAMENTO,
    DOSE_HISTORY,
    DEPENDENTE,
    USUARIO,
    TIMELINE_EVENT,
    UNKNOWN
}

/**
 * Ações que podem ser auditadas.
 */
enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    DOWNLOAD,
    SHARE,
    STATUS_CHANGE,
    ARCHIVE,
    UNARCHIVE,
    UNKNOWN
}

/**
 * Tipos de acesso a documentos.
 */
enum class AccessAction {
    VIEW,
    DOWNLOAD,
    SHARE,
    DELETE,
    EDIT
}

