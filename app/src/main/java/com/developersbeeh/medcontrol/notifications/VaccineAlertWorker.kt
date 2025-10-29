package com.developersbeeh.medcontrol.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.VaccineRepository
import com.developersbeeh.medcontrol.ui.vaccination.VacinaStatus
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private const val TAG = "VaccineAlertWorker"

@HiltWorker
class VaccineAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestoreRepository: FirestoreRepository,
    private val vaccineRepository: VaccineRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando verificação diária de alertas de vacina.")

        if (!userPreferences.getVaccineAlertsEnabled()) {
            Log.i(TAG, "Alertas de vacina desativados pelo usuário. Trabalho cancelado.")
            return Result.success()
        }

        try {
            val dependents = firestoreRepository.getDependentes().first()
            val scheduler = NotificationScheduler(applicationContext)

            dependents.forEach { dependent ->
                val birthDate = parseDate(dependent.dataDeNascimento)
                if (birthDate != null) {
                    val ageInMonths = Period.between(birthDate, LocalDate.now()).toTotalMonths()
                    if (ageInMonths <= 120) {
                        val registros = vaccineRepository.getRegistrosVacina(dependent.id).first()
                        val calendario = vaccineRepository.getCalendarioVacinal()

                        var atrasadasCount = 0
                        var proximasCount = 0

                        calendario.forEach { vacina ->
                            val registro = registros.find { it.vacinaId == vacina.id }
                            // ✅ PASSA A DATA DE CRIAÇÃO DO PERFIL PARA A LÓGICA
                            val status = determineStatus(vacina, registro, ageInMonths.toInt(), birthDate, dependent.dataCriacaoLocalDateTime.toLocalDate())
                            when (status) {
                                VacinaStatus.ATRASADA -> atrasadasCount++
                                VacinaStatus.PROXIMA -> proximasCount++
                                else -> {}
                            }
                        }

                        if (atrasadasCount > 0 || proximasCount > 0) {
                            val title = "Lembrete de Vacinas para ${dependent.nome}"
                            val message = mutableListOf<String>()
                            if (atrasadasCount > 0) message.add("$atrasadasCount atrasada(s)")
                            if (proximasCount > 0) message.add("$proximasCount próxima(s)")

                            val notificationId = "vaccine_alert_${dependent.id}".hashCode()
                            scheduler.showVaccineAlertNotification(title, message.joinToString(" e "), notificationId)
                            Log.i(TAG, "Notificação de alerta de vacina enviada para ${dependent.nome}")
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar o VaccineAlertWorker.", e)
            return Result.retry()
        }
    }

    // ✅ FUNÇÃO ATUALIZADA COM A NOVA LÓGICA
    private fun determineStatus(
        vacina: com.developersbeeh.medcontrol.data.model.Vacina,
        registro: com.developersbeeh.medcontrol.data.model.RegistroVacina?,
        ageInMonths: Int,
        birthDate: LocalDate,
        profileCreationDate: LocalDate
    ): VacinaStatus {
        if (registro != null) {
            return VacinaStatus.TOMADA
        }

        val recommendedDate = birthDate.plusMonths(vacina.idadeRecomendadaMeses.toLong())

        return when {
            // Atrasada: Se a idade já passou E a data recomendada for depois da criação do perfil.
            ageInMonths > vacina.idadeRecomendadaMeses && !recommendedDate.isBefore(profileCreationDate) -> VacinaStatus.ATRASADA
            // Próxima: Se a idade recomendada for no mês atual ou no próximo.
            ageInMonths >= vacina.idadeRecomendadaMeses - 1 -> VacinaStatus.PROXIMA
            else -> VacinaStatus.OK
        }
    }

    private fun parseDate(dateString: String): LocalDate? {
        if (dateString.isBlank()) return null
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: DateTimeParseException) {
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e2: DateTimeParseException) {
                null
            }
        }
    }
}