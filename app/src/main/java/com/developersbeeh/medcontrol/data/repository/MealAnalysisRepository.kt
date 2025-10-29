package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.MealAnalysisResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MealAnalysisRepository"

@Singleton
class MealAnalysisRepository @Inject constructor(
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val firestoreRepository: FirestoreRepository,
    private val moshi: Moshi,
    @ApplicationContext private val context: Context
) {

    suspend fun analyzeMealPhoto(dependentId: String, imageUri: Uri): Result<MealAnalysisResult> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception(context.getString(R.string.error_user_not_authenticated)))

        return try {
            // ✅ CORREÇÃO: Lógica de upload agora gera uma GCS URI (gs://)
            Log.d(TAG, "Iniciando upload da imagem...")
            val uniqueFileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child("meal_photos/${currentUser.uid}/$uniqueFileName")
            storageRef.putFile(imageUri).await()

            val gcsUri = storageRef.toString()
            Log.d(TAG, "Upload concluído. GCS URI: $gcsUri")

            val dependente = firestoreRepository.getDependente(dependentId)

            // ✅ CORREÇÃO: Chamar a Cloud Function com a GCS URI
            val data = hashMapOf(
                "imageGcsUri" to gcsUri, // ✅ CORREÇÃO: Envia 'imageGcsUri'
                "healthProfile" to mapOf(
                    "idade" to (dependente?.dataDeNascimento ?: ""),
                    "peso" to (dependente?.peso ?: ""),
                    "altura" to (dependente?.altura ?: ""),
                    "sexo" to (dependente?.sexo ?: "")
                )
            )

            Log.d(TAG, "Chamando a Cloud Function 'analisarRefeicao'...")
            val result = functions
                .getHttpsCallable("analisarRefeicao")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultMap = result.data as? Map<String, Any>
            if (resultMap != null && resultMap.containsKey("error")) {
                Result.failure(Exception(resultMap["error"] as String))
            } else {
                val adapter = moshi.adapter(MealAnalysisResult::class.java)
                val analysisResult = adapter.fromJsonValue(resultMap)

                if (analysisResult != null) {
                    Log.i(TAG, "Análise da refeição concluída com sucesso.")
                    Result.success(analysisResult)
                } else {
                    Result.failure(Exception(context.getString(R.string.error_cannot_process_ai_response)))
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: context.getString(R.string.error_failed_meal_analysis, e.message)
            Log.e(TAG, "Falha ao analisar a refeição: $errorMessage", e)
            Result.failure(Exception(errorMessage, e))
        }
    }
}