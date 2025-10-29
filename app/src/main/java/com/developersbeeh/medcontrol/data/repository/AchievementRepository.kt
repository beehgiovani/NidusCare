package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.Conquista
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun getCollectionRef(dependentId: String) =
        db.collection("dependentes").document(dependentId).collection("conquistas")

    /**
     * Retorna um fluxo com a lista de conquistas já desbloqueadas por um dependente.
     */
    fun getAchievements(dependentId: String): Flow<List<Conquista>> {
        return getCollectionRef(dependentId).snapshots().map { snapshot ->
            snapshot.toObjects(Conquista::class.java)
        }
    }

    /**
     * Salva uma nova conquista para um dependente.
     * Usar .set() com o ID do TipoConquista garante que a mesma conquista não seja salva duas vezes.
     */
    suspend fun awardAchievement(dependentId: String, conquista: Conquista): Result<Unit> {
        return try {
            getCollectionRef(dependentId).document(conquista.id).set(conquista).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}