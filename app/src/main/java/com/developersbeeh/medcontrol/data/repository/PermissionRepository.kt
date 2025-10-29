package com.developersbeeh.medcontrol.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    /**
     * Atualiza o mapa de permissões para um dependente específico no Firestore.
     * @param dependentId O ID do dependente a ser atualizado.
     * @param permissions O novo mapa de permissões (ex: "podeRegistrarDose" to true).
     * @return Um Result indicando sucesso ou falha.
     */
    suspend fun updatePermissions(dependentId: String, permissions: Map<String, Boolean>): Result<Unit> {
        return try {
            val dependentRef = db.collection("dependentes").document(dependentId)
            dependentRef.update("permissoes", permissions).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}