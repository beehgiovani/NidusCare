package com.developersbeeh.medcontrol.data.repository

import android.net.Uri
import android.util.Log
import com.developersbeeh.medcontrol.audit.AccessAction
import com.developersbeeh.medcontrol.audit.AuditAction
import com.developersbeeh.medcontrol.audit.AuditLogger
import com.developersbeeh.medcontrol.audit.EntityType
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.data.model.TipoDocumento
import com.developersbeeh.medcontrol.util.advanced.TextUtils
import com.developersbeeh.medcontrol.validation.DocumentoSaudeValidator
import com.developersbeeh.medcontrol.validation.ValidationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DocumentRepositoryEnhanced"

/**
 * Filtros para busca de documentos.
 */
data class DocumentFilters(
    val tipo: TipoDocumento? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val medicoSolicitante: String? = null,
    val laboratorio: String? = null
)

/**
 * Versão melhorada do DocumentRepository com validações, busca avançada e auditoria.
 */
@Singleton
class DocumentRepositoryEnhanced @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val auditLogger: AuditLogger
) {

    private val validator = DocumentoSaudeValidator()

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ==================== MÉTODOS BÁSICOS (MANTIDOS) ====================

    fun getDocuments(dependentId: String): Flow<List<DocumentoSaude>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("documentos_saude")
            .orderBy("dataDocumento", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val documents = snapshot.toObjects(DocumentoSaude::class.java)
                    trySend(documents)
                }
            }
        awaitClose { listener.remove() }
    }

    // ==================== MÉTODOS MELHORADOS ====================

    /**
     * Valida um documento sem salvá-lo.
     */
    fun validateDocumento(documento: DocumentoSaude): ValidationResult {
        return validator.validate(documento)
    }

    /**
     * Valida um arquivo antes do upload.
     */
    fun validateFileForUpload(
        fileUri: Uri?,
        fileName: String?,
        fileSizeBytes: Long?,
        mimeType: String?
    ): ValidationResult {
        return validator.validateFileForUpload(fileUri, fileName, fileSizeBytes, mimeType)
    }

    /**
     * Faz upload de um arquivo com validação.
     */
    private suspend fun uploadDocumentFile(
        dependentId: String,
        fileUri: Uri,
        fileName: String,
        fileSizeBytes: Long?,
        mimeType: String?
    ): Result<String> {
        // Validar arquivo antes do upload
        val validationResult = validateFileForUpload(fileUri, fileName, fileSizeBytes, mimeType)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        return try {
            val fileId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("health_documents/$dependentId/$fileId-$fileName")
            val uploadTask = storageRef.putFile(fileUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading document file", e)
            Result.failure(e)
        }
    }

    /**
     * Salva um documento com validação e auditoria.
     */
    suspend fun saveDocument(
        documento: DocumentoSaude,
        fileUri: Uri?,
        fileSizeBytes: Long? = null,
        mimeType: String? = null
    ): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar documento
        val validationResult = validator.validate(documento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        return try {
            var finalDocument = documento
            val isUpdate = getDocument(documento.dependentId, documento.id) != null

            // Se um novo arquivo foi fornecido, faz o upload
            if (fileUri != null) {
                val uploadResult = uploadDocumentFile(
                    documento.dependentId,
                    fileUri,
                    documento.fileName,
                    fileSizeBytes,
                    mimeType
                )
                
                uploadResult.onSuccess { url ->
                    finalDocument = documento.copy(fileUrl = url)
                }.onFailure {
                    return Result.failure(it)
                }
            }

            // Salva o documento no Firestore
            db.collection("dependentes").document(documento.dependentId)
                .collection("documentos_saude").document(finalDocument.id)
                .set(finalDocument).await()

            // Auditoria
            auditLogger.logAction(
                entityType = EntityType.DOCUMENTO,
                entityId = finalDocument.id,
                dependentId = documento.dependentId,
                userId = userId,
                action = if (isUpdate) AuditAction.UPDATE else AuditAction.CREATE,
                metadata = mapOf(
                    "titulo" to finalDocument.titulo,
                    "tipo" to finalDocument.tipo.name
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving document", e)
            Result.failure(e)
        }
    }

    /**
     * Obtém um documento específico.
     */
    suspend fun getDocument(dependentId: String, documentId: String): DocumentoSaude? {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("documentos_saude").document(documentId)
                .get().await()
            
            snapshot.toObject(DocumentoSaude::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document", e)
            null
        }
    }

    /**
     * Busca documentos com filtros avançados.
     */
    suspend fun searchDocuments(
        dependentId: String,
        query: String,
        filters: DocumentFilters = DocumentFilters()
    ): List<DocumentoSaude> {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("documentos_saude")
                .get().await()

            val allDocuments = snapshot.toObjects(DocumentoSaude::class.java)
            
            allDocuments.filter { doc ->
                // Filtro de texto
                val matchesQuery = query.isBlank() || 
                    TextUtils.containsIgnoreCase(doc.titulo, query) ||
                    TextUtils.containsIgnoreCase(doc.anotacoes ?: "", query) ||
                    TextUtils.containsIgnoreCase(doc.medicoSolicitante ?: "", query) ||
                    TextUtils.containsIgnoreCase(doc.laboratorio ?: "", query)

                // Filtro de tipo
                val matchesTipo = filters.tipo == null || doc.tipo == filters.tipo

                // Filtro de data
                val matchesDate = try {
                    if (filters.startDate == null && filters.endDate == null) {
                        true
                    } else {
                        val docDate = LocalDate.parse(doc.dataDocumento, DateTimeFormatter.ISO_LOCAL_DATE)
                        val afterStart = filters.startDate == null || !docDate.isBefore(filters.startDate)
                        val beforeEnd = filters.endDate == null || !docDate.isAfter(filters.endDate)
                        afterStart && beforeEnd
                    }
                } catch (e: Exception) {
                    true // Se não conseguir parsear a data, inclui o documento
                }

                // Filtro de médico
                val matchesMedico = filters.medicoSolicitante == null ||
                    TextUtils.areSimilar(doc.medicoSolicitante ?: "", filters.medicoSolicitante)

                // Filtro de laboratório
                val matchesLab = filters.laboratorio == null ||
                    TextUtils.areSimilar(doc.laboratorio ?: "", filters.laboratorio)

                matchesQuery && matchesTipo && matchesDate && matchesMedico && matchesLab
            }.sortedByDescending { it.dataDocumento }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching documents", e)
            emptyList()
        }
    }

    /**
     * Deleta um documento com auditoria.
     */
    suspend fun deleteDocument(documento: DocumentoSaude): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar se pode deletar
        val validationResult = validator.validateForDeletion(documento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        return try {
            // Deleta o arquivo no Storage
            if (documento.fileUrl.isNotEmpty()) {
                try {
                    val storageRef = storage.getReferenceFromUrl(documento.fileUrl)
                    storageRef.delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete file from storage", e)
                    // Continua mesmo se falhar ao deletar o arquivo
                }
            }

            // Deleta o documento no Firestore
            db.collection("dependentes").document(documento.dependentId)
                .collection("documentos_saude").document(documento.id)
                .delete().await()

            // Auditoria
            auditLogger.logDocumentAccess(
                dependentId = documento.dependentId,
                documentId = documento.id,
                userId = userId,
                action = AccessAction.DELETE
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document", e)
            Result.failure(e)
        }
    }

    /**
     * Registra visualização de documento para auditoria.
     */
    suspend fun logDocumentView(documento: DocumentoSaude) {
        val userId = getCurrentUserId() ?: return
        
        try {
            auditLogger.logDocumentAccess(
                dependentId = documento.dependentId,
                documentId = documento.id,
                userId = userId,
                action = AccessAction.VIEW
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error logging document view", e)
        }
    }

    /**
     * Registra download de documento para auditoria.
     */
    suspend fun logDocumentDownload(documento: DocumentoSaude) {
        val userId = getCurrentUserId() ?: return
        
        try {
            auditLogger.logDocumentAccess(
                dependentId = documento.dependentId,
                documentId = documento.id,
                userId = userId,
                action = AccessAction.DOWNLOAD
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error logging document download", e)
        }
    }

    /**
     * Gera link temporário para compartilhamento (placeholder - implementação futura).
     */
    suspend fun shareDocument(
        documento: DocumentoSaude,
        expirationHours: Int = 24
    ): Result<String> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))

        // Validar se pode compartilhar
        val validationResult = validator.validateForSharing(documento)
        if (validationResult.isError() && validationResult.hasCriticalErrors()) {
            val errors = validationResult.getCriticalErrors()
                .joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Erros de validação: $errors"))
        }

        return try {
            // Por enquanto, retorna a URL direta do arquivo
            // Em uma implementação futura, criar um token temporário
            
            // Auditoria
            auditLogger.logDocumentAccess(
                dependentId = documento.dependentId,
                documentId = documento.id,
                userId = userId,
                action = AccessAction.SHARE
            )

            Result.success(documento.fileUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing document", e)
            Result.failure(e)
        }
    }

    /**
     * Obtém estatísticas de documentos por tipo.
     */
    suspend fun getDocumentStatsByType(dependentId: String): Map<TipoDocumento, Int> {
        return try {
            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("documentos_saude")
                .get().await()

            val documents = snapshot.toObjects(DocumentoSaude::class.java)
            documents.groupingBy { it.tipo }.eachCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document stats", e)
            emptyMap()
        }
    }

    /**
     * Obtém documentos recentes (últimos N dias).
     */
    suspend fun getRecentDocuments(dependentId: String, days: Int = 30): List<DocumentoSaude> {
        return try {
            val cutoffDate = LocalDate.now().minusDays(days.toLong())
            val cutoffString = cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

            val snapshot = db.collection("dependentes").document(dependentId)
                .collection("documentos_saude")
                .whereGreaterThanOrEqualTo("dataDocumento", cutoffString)
                .orderBy("dataDocumento", Query.Direction.DESCENDING)
                .get().await()

            snapshot.toObjects(DocumentoSaude::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent documents", e)
            emptyList()
        }
    }
}

