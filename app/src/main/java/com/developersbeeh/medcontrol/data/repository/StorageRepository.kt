package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StorageRepository"

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context // Injetado
) {

    /**
     * Faz o upload de uma imagem para a pasta de scans da farmacinha e retorna a GCS URI.
     * @param imageUri O URI local da imagem (content:// ou file://).
     * @param userId O ID do usuário que está fazendo o upload.
     * @return Um Result contendo a GCS URI (gs://bucket-name/path/to/image.jpg) em caso de sucesso.
     */
    suspend fun uploadImageForScan(imageUri: Uri, userId: String): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child("farmacinha_scans/$userId/$fileName")

            storageRef.putFile(imageUri).await()

            val gcsUri = storageRef.toString()
            Log.d(TAG, "Upload bem-sucedido. GCS URI: $gcsUri")
            Result.success(gcsUri)
        } catch (e: Exception) {
            Log.e(TAG, "Falha no upload da imagem para o Storage", e)
            Result.failure(Exception(context.getString(R.string.error_uploading_image_to_storage), e))
        }
    }
}