package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ImageAnalysisRepository"

@Singleton
class ImageAnalysisRepository @Inject constructor(
    private val functions: FirebaseFunctions,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun analyzePrescription(
        dependentId: String,
        imageUri: Uri
    ): Result<List<Medicamento>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception(context.getString(R.string.error_user_not_authenticated_relogin)))

            val dependente = firestoreRepository.getDependente(dependentId)
            val medicamentosAtuais = medicationRepository.getMedicamentos(dependentId).first()

            // ✅ CORREÇÃO: Lógica de upload agora gera uma GCS URI (gs://)
            val uniqueFileName = "${UUID.randomUUID()}-${imageUri.lastPathSegment}"
            val storageRef = storage.reference.child("prescription_scans/${currentUser.uid}/$uniqueFileName")
            storageRef.putFile(imageUri).await()

            // Pega a GCS URI (ex: "gs://seu-bucket/prescription_scans/...")
            // USA storageRef.toString() em vez de storageRef.downloadUrl.await().toString()
            val gcsUri = storageRef.toString()
            Log.d(TAG, "Upload da receita concluído. GCS URI: $gcsUri")

            // ✅ CORREÇÃO: O corpo da requisição agora usa 'imageGcsUri'
            val data = hashMapOf(
                "imageGcsUri" to gcsUri, // Padronizado com a outra função de IA
                "alergiasConhecidas" to (dependente?.alergias ?: context.getString(R.string.none_informed)),
                "condicoesPreexistentes" to (dependente?.condicoesPreexistentes ?: context.getString(R.string.none_informed)),
                "medicamentosAtuais" to medicamentosAtuais.map { it.nome }
            )

            Log.d(TAG, "Chamando a Cloud Function 'analisarReceita'...")
            val result = functions
                .getHttpsCallable("analisarReceita")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultMap = result.data as? Map<String, Any>
            val medicationsList = resultMap?.get("medications") as? List<Map<String, Any>> ?: emptyList()

            val medications = medicationsList.map { medMap ->
                val posologia = medMap["posologia"] as? String ?: ""
                val avisos = (medMap["avisos"] as? List<String>) ?: emptyList()

                val (frequenciaTipo, frequenciaValor, duracaoDias) = parsePosologia(posologia)
                Medicamento(
                    nome = medMap["nome"] as? String ?: context.getString(R.string.name_not_found),
                    dosagem = medMap["dosagem"] as? String ?: context.getString(R.string.dosage_not_found),
                    frequenciaTipo = frequenciaTipo,
                    frequenciaValor = frequenciaValor,
                    duracaoDias = duracaoDias,
                    isUsoContinuo = duracaoDias == 0,
                    horarios = emptyList(),
                    posologia = posologia,
                    anotacoes = if (avisos.isNotEmpty()) context.getString(R.string.analysis_warnings, avisos.joinToString("\n- ")) else null
                )
            }

            Log.i(TAG, "Análise da receita concluída com sucesso. ${medications.size} medicamentos encontrados.")
            Result.success(medications)

        } catch (e: Exception) {
            val errorMessage = e.message ?: context.getString(R.string.error_unknown_prescription_analysis)
            Log.e(TAG, "Falha ao analisar a receita: $errorMessage", e)
            Result.failure(Exception(context.getString(R.string.error_failed_prescription_analysis, errorMessage), e))
        }
    }


    private fun parsePosologia(posologia: String): Triple<FrequenciaTipo, Int, Int> {
        val lowerCasePosologia = posologia.lowercase()
        var frequenciaTipo = FrequenciaTipo.DIARIA
        var frequenciaValor = 1
        var duracaoDias = 0

        val duracaoMatch = Regex("por\\s+(\\d+)\\s+dias?").find(lowerCasePosologia)
        duracaoMatch?.let {
            duracaoDias = it.groupValues[1].toIntOrNull() ?: 0
        }

        val horasMatch = Regex("a\\s+cada\\s+(\\d+)\\s+horas?|de\\s*(\\d+)[/]\\s*(\\d+)\\s*h?").find(lowerCasePosologia)
        val intervaloEmHoras = horasMatch?.groupValues?.get(1)?.toIntOrNull()
            ?: horasMatch?.groupValues?.get(2)?.toIntOrNull()

        if (intervaloEmHoras != null && intervaloEmHoras > 0) {
            frequenciaTipo = FrequenciaTipo.DIARIA
            frequenciaValor = 24 / intervaloEmHoras
            return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
        }

        val vezesDiaMatch = Regex("(\\d+)\\s*([x]|vezes)?\\s*(ao\\s+dia|/dia)").find(lowerCasePosologia)
        if (vezesDiaMatch != null) {
            frequenciaTipo = FrequenciaTipo.DIARIA
            frequenciaValor = vezesDiaMatch.groupValues[1].toIntOrNull() ?: 1
            return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
        }

        val abreviacaoMatch = when {
            lowerCasePosologia.contains("q.i.d.") -> 4
            lowerCasePosologia.contains("t.i.d.") -> 3
            lowerCasePosologia.contains("b.i.d.") -> 2
            lowerCasePosologia.contains("q.d.") -> 1
            else -> null
        }
        if (abreviacaoMatch != null) {
            frequenciaTipo = FrequenciaTipo.DIARIA
            frequenciaValor = abreviacaoMatch
            return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
        }

        if (lowerCasePosologia.contains("uma vez ao dia")) {
            frequenciaTipo = FrequenciaTipo.DIARIA
            frequenciaValor = 1
            return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
        }

        if (lowerCasePosologia.contains("quando necessário") || lowerCasePosologia.contains("se necessário")) {
            frequenciaTipo = FrequenciaTipo.INTERVALO_DIAS
            frequenciaValor = 0
            return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
        }

        return Triple(frequenciaTipo, frequenciaValor, duracaoDias)
    }
}