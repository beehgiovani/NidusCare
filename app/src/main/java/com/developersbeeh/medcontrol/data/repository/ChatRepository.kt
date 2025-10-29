// src/main/java/com/developersbeeh/medcontrol/data/repository/ChatRepository.kt

package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {

    fun getChatHistory(dependentId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("chat_history")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ChatMessage::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveChatMessage(dependentId: String, message: ChatMessage): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("chat_history")
                .document(message.id)
                .set(message)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatResponse(prompt: String, dependentId: String): Result<String> {
        return try {
            val data = hashMapOf(
                "prompt" to prompt,
                "dependentId" to dependentId
            )

            val result = functions
                .getHttpsCallable("getChatResponse")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val responseText = (result.data as? Map<String, Any>)?.get("response") as? String

            if (responseText != null) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("A resposta da IA estava vazia."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao chamar a Cloud Function 'getChatResponse'", e)
            Result.failure(Exception("Não foi possível conectar ao assistente de IA. Tente novamente."))
        }
    }
}