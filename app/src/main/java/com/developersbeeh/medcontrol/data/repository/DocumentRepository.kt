package com.developersbeeh.medcontrol.data.repository

import android.net.Uri
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private suspend fun uploadDocumentFile(dependentId: String, fileUri: Uri, fileName: String): Result<String> {
        return try {
            val fileId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("health_documents/$dependentId/$fileId-$fileName")
            val uploadTask = storageRef.putFile(fileUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDocument(documento: DocumentoSaude, fileUri: Uri?): Result<Unit> {
        return try {
            var finalDocument = documento
            if (fileUri != null) {
                // Se um novo arquivo foi fornecido, faz o upload e pega a URL
                val uploadResult = uploadDocumentFile(documento.dependentId, fileUri, documento.fileName)
                uploadResult.onSuccess { url ->
                    finalDocument = documento.copy(fileUrl = url)
                }.onFailure {
                    // Se o upload falhar, retorna o erro
                    return Result.failure(it)
                }
            }

            // Salva o documento (com a nova URL, se houver) no Firestore
            db.collection("dependentes").document(documento.dependentId)
                .collection("documentos_saude").document(finalDocument.id)
                .set(finalDocument).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun deleteDocument(documento: DocumentoSaude): Result<Unit> {
        return try {
            // Deleta o arquivo no Storage
            if (documento.fileUrl.isNotEmpty()) {
                val storageRef = storage.getReferenceFromUrl(documento.fileUrl)
                storageRef.delete().await()
            }
            // Deleta o documento no Firestore
            db.collection("dependentes").document(documento.dependentId)
                .collection("documentos_saude").document(documento.id)
                .delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}