// src/main/java/com/developersbeeh/medcontrol/data/repository/VaccineRepository.kt

package com.developersbeeh.medcontrol.data.repository

import com.developersbeeh.medcontrol.data.model.RegistroVacina
import com.developersbeeh.medcontrol.data.model.Vacina
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccineRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    /**
     * Retorna o calendário de vacinação infantil padrão do Brasil (simplificado).
     */
    fun getCalendarioVacinal(): List<Vacina> {
        return listOf(
            Vacina(id = "bcg", nome = "BCG", previne = "Formas graves de tuberculose", dose = "Dose Única", idadeRecomendadaMeses = 0),
            Vacina(id = "hep_b", nome = "Hepatite B", previne = "Hepatite B", dose = "1ª Dose", idadeRecomendadaMeses = 0),
            Vacina(id = "penta_1", nome = "Pentavalente", previne = "Difteria, tétano, coqueluche, hepatite B, Haemophilus influenzae B", dose = "1ª Dose", idadeRecomendadaMeses = 2),
            Vacina(id = "vip_1", nome = "VIP (Poliomielite)", previne = "Poliomielite", dose = "1ª Dose", idadeRecomendadaMeses = 2),
            Vacina(id = "pnm_1", nome = "Pneumocócica 10V", previne = "Pneumonia, otite, meningite", dose = "1ª Dose", idadeRecomendadaMeses = 2),
            Vacina(id = "rota_1", nome = "Rotavírus", previne = "Gastroenterite por rotavírus", dose = "1ª Dose", idadeRecomendadaMeses = 2),
            Vacina(id = "penta_2", nome = "Pentavalente", previne = "Difteria, tétano, coqueluche, hepatite B, Haemophilus influenzae B", dose = "2ª Dose", idadeRecomendadaMeses = 4),
            Vacina(id = "vip_2", nome = "VIP (Poliomielite)", previne = "Poliomielite", dose = "2ª Dose", idadeRecomendadaMeses = 4),
            Vacina(id = "pnm_2", nome = "Pneumocócica 10V", previne = "Pneumonia, otite, meningite", dose = "2ª Dose", idadeRecomendadaMeses = 4),
            Vacina(id = "rota_2", nome = "Rotavírus", previne = "Gastroenterite por rotavírus", dose = "2ª Dose", idadeRecomendadaMeses = 4),
            Vacina(id = "penta_3", nome = "Pentavalente", previne = "Difteria, tétano, coqueluche, hepatite B, Haemophilus influenzae B", dose = "3ª Dose", idadeRecomendadaMeses = 6),
            Vacina(id = "vip_3", nome = "VIP (Poliomielite)", previne = "Poliomielite", dose = "3ª Dose", idadeRecomendadaMeses = 6),
            Vacina(id = "meningo_c_1", nome = "Meningocócica C", previne = "Meningite C", dose = "1ª Dose", idadeRecomendadaMeses = 3),
            Vacina(id = "meningo_c_2", nome = "Meningocócica C", previne = "Meningite C", dose = "2ª Dose", idadeRecomendadaMeses = 5),
            Vacina(id = "fa_1", nome = "Febre Amarela", previne = "Febre Amarela", dose = "Dose Inicial", idadeRecomendadaMeses = 9),
            Vacina(id = "mmr_1", nome = "Tríplice Viral (SCR)", previne = "Sarampo, Caxumba, Rubéola", dose = "1ª Dose", idadeRecomendadaMeses = 12),
            Vacina(id = "pnm_r", nome = "Pneumocócica 10V", previne = "Pneumonia, otite, meningite", dose = "Reforço", idadeRecomendadaMeses = 12),
            Vacina(id = "meningo_c_r", nome = "Meningocócica C", previne = "Meningite C", dose = "Reforço", idadeRecomendadaMeses = 12),
            Vacina(id = "dtp_r1", nome = "DTP (Tríplice Bacteriana)", previne = "Difteria, tétano, coqueluche", dose = "1º Reforço", idadeRecomendadaMeses = 15),
            Vacina(id = "vop_r1", nome = "VOP (Poliomielite)", previne = "Poliomielite", dose = "1º Reforço", idadeRecomendadaMeses = 15),
            Vacina(id = "hep_a", nome = "Hepatite A", previne = "Hepatite A", dose = "Dose Única", idadeRecomendadaMeses = 15),
            Vacina(id = "tetra", nome = "Tetra Viral (SCRV)", previne = "Sarampo, Caxumba, Rubéola, Varicela", dose = "Dose Única", idadeRecomendadaMeses = 15)
        )
    }

    private fun getCollectionRef(dependentId: String) =
        db.collection("dependentes").document(dependentId).collection("vacinas")

    /**
     * Salva ou atualiza um registro de vacina no Firestore.
     */
    suspend fun saveRegistroVacina(dependentId: String, registro: RegistroVacina): Result<Unit> {
        return try {
            getCollectionRef(dependentId).document(registro.id).set(registro).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retorna um fluxo com todos os registros de vacinas de um dependente.
     */
    fun getRegistrosVacina(dependentId: String): Flow<List<RegistroVacina>> = callbackFlow {
        if (dependentId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = getCollectionRef(dependentId)
            .orderBy("dataAplicacao", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(RegistroVacina::class.java))
                }
            }
        awaitClose { listener.remove() }
    }
}