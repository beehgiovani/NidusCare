// src/main/java/com/developersbeeh/medcontrol/data/repository/ActivityLogRepository.kt
package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Atividade
import com.developersbeeh.medcontrol.data.model.TipoAtividade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLogRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferences
) {

    /**
     * Busca e observa em tempo real a lista de atividades de um dependente.
     * @param dependentId O ID do dependente.
     * @return Um Flow que emite a lista de atividades ordenada pela mais recente.
     */
    fun getActivityLogs(dependentId: String): Flow<List<Atividade>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(IllegalArgumentException("O ID do dependente não pode ser vazio."))
            return@callbackFlow
        }

        val listener = db.collection("dependentes").document(dependentId)
            .collection("atividades")
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .limit(200) // Limita aos 200 eventos mais recentes para performance
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ActivityLogRepository", "Erro ao escutar logs de atividade.", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Atividade::class.java))
                }
            }

        // Garante que o listener seja removido quando o Flow for cancelado
        awaitClose { listener.remove() }
    }

    /**
     * Salva um novo log de atividade no Firestore.
     * @param dependentId O ID do dependente relacionado ao log.
     * @param descricao A descrição da atividade.
     * @param tipo O tipo da atividade (enum).
     * @return Um Result indicando sucesso ou falha.
     */
    /**
     * Salva um novo log de atividade no Firestore.
     * @param dependentId O ID do dependente relacionado ao log.
     * @param descricao A descrição da atividade.
     * @param tipo O tipo da atividade (enum).
     * @return Um Result indicando sucesso ou falha.
     */
    suspend fun saveLog(dependentId: String, descricao: String, tipo: TipoAtividade): Result<Unit> {
        return try {
            val autorNome = userPreferences.getUserName()
            val autorId = auth.currentUser?.uid ?: "user_not_found"

            val log = Atividade(
                // ✅ CORREÇÃO: Descrição limpa, sem o nome do autor
                descricao = descricao,
                tipo = tipo,
                autorId = autorId,
                autorNome = autorNome // O backend usará este campo
            )

            db.collection("dependentes").document(dependentId).collection("atividades").add(log).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ActivityLogRepository", "Erro ao salvar log de atividade", e)
            Result.failure(e)
        }
    }
}