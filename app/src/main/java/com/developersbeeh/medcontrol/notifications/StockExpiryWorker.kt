package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "StockExpiryWorker"

@HiltWorker
class StockExpiryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository,
    private val medicationRepository: MedicationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação diária de ESTOQUE e VALIDADE...")

        try {
            val scheduler = NotificationScheduler(applicationContext)
            val dependents = firestoreRepository.getDependentes().first()

            for (dependent in dependents) {
                val medicamentos = medicationRepository.getMedicamentos(dependent.id).first()

                for (med in medicamentos) {
                    var medNeedsUpdate = false
                    var updatedMed = med.copy()

                    // --- Bloco 1: Verificação de Validade (lógica existente) ---
                    val today = LocalDate.now()
                    val thirtyDaysFromNow = today.plusDays(30)
                    val updatedLotes = updatedMed.lotes.map { lote ->
                        if (!lote.alertaValidadeEnviado && lote.dataValidade.isAfter(today.minusDays(1)) && lote.dataValidade.isBefore(thirtyDaysFromNow)) {
                            val title = "Validade Próxima: ${med.nome}"
                            val body = "O lote de ${med.nome} (${lote.quantidade} ${med.unidadeDeEstoque}) para ${dependent.nome} vence em ${lote.dataValidade.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}."
                            scheduler.showStockOrExpiryNotification(title, body, (med.id + lote.id + "_expiry").hashCode())
                            Log.i(TAG, "Notificação de validade enviada para o lote ${lote.id} do medicamento ${med.nome}")
                            medNeedsUpdate = true
                            lote.copy(alertaValidadeEnviado = true)
                        } else {
                            lote
                        }
                    }
                    if (medNeedsUpdate) {
                        updatedMed = updatedMed.copy(lotes = updatedLotes)
                    }

                    // --- Bloco 2: Verificação de Estoque Baixo (nova lógica) ---
                    val needsStockAlert = updatedMed.nivelDeAlertaEstoque > 0 &&
                            updatedMed.estoqueAtualTotal <= updatedMed.nivelDeAlertaEstoque &&
                            !updatedMed.alertaDeEstoqueEnviado

                    if (needsStockAlert) {
                        val title = "Estoque Baixo: ${updatedMed.nome}"
                        val body = "O estoque de ${updatedMed.nome} para ${dependent.nome} está baixo. Restam apenas ${updatedMed.estoqueAtualTotal} ${updatedMed.unidadeDeEstoque}."
                        scheduler.showStockOrExpiryNotification(title, body, (updatedMed.id + "_stock").hashCode())
                        Log.i(TAG, "Notificação de estoque baixo enviada para ${updatedMed.nome}")
                        updatedMed = updatedMed.copy(alertaDeEstoqueEnviado = true)
                        medNeedsUpdate = true
                    }

                    // Salva o medicamento no Firestore apenas uma vez se houver qualquer alteração
                    if (medNeedsUpdate) {
                        medicationRepository.saveMedicamento(dependent.id, updatedMed)
                    }
                }
            }
            Log.d(TAG, "Verificação diária de ESTOQUE e VALIDADE concluída com sucesso.")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Falha na verificação diária de estoque/validade.", e)
            return Result.retry() // Usar retry() é melhor para falhas transitórias
        }
    }
}