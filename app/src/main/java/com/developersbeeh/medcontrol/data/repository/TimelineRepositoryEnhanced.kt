package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import com.developersbeeh.medcontrol.data.model.DoseHistory
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TimelineEvent
import com.developersbeeh.medcontrol.data.model.TimelineEventType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TimelineRepositoryEnhanced"

/**
 * Filtros para timeline.
 */
data class TimelineFilters(
    val eventTypes: List<TimelineEventType>? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val searchQuery: String? = null
)

/**
 * Estatísticas da timeline.
 */
data class TimelineStats(
    val totalEvents: Int = 0,
    val dosesTaken: Int = 0,
    val dosesSkipped: Int = 0,
    val dosesMissed: Int = 0,
    val appointments: Int = 0,
    val documentsAdded: Int = 0,
    val medicationsAdded: Int = 0
)

/**
 * Repositório melhorado para gerenciar a timeline consolidada de eventos de saúde.
 * Agrega medicamentos, doses, documentos e agendamentos em uma única linha do tempo.
 */
@Singleton
class TimelineRepositoryEnhanced @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Obtém todos os eventos da timeline consolidada.
     */
    suspend fun getTimelineEvents(
        dependentId: String,
        filters: TimelineFilters = TimelineFilters(),
        limit: Int = 100
    ): List<TimelineEvent> {
        if (dependentId.isBlank()) return emptyList()

        return try {
            val events = mutableListOf<TimelineEvent>()

            // Buscar doses
            if (filters.eventTypes == null || 
                TimelineEventType.DOSE_TAKEN in filters.eventTypes ||
                TimelineEventType.DOSE_SKIPPED in filters.eventTypes ||
                TimelineEventType.DOSE_MISSED in filters.eventTypes) {
                val doses = fetchDoseEvents(dependentId, filters, limit)
                events.addAll(doses)
            }

            // Buscar agendamentos
            if (filters.eventTypes == null || TimelineEventType.APPOINTMENT in filters.eventTypes) {
                val appointments = fetchAppointmentEvents(dependentId, filters, limit)
                events.addAll(appointments)
            }

            // Buscar documentos
            if (filters.eventTypes == null || TimelineEventType.DOCUMENT_ADDED in filters.eventTypes) {
                val documents = fetchDocumentEvents(dependentId, filters, limit)
                events.addAll(documents)
            }

            // Buscar medicamentos adicionados
            if (filters.eventTypes == null || TimelineEventType.MEDICATION_ADDED in filters.eventTypes) {
                val medications = fetchMedicationEvents(dependentId, filters, limit)
                events.addAll(medications)
            }

            // Ordenar por timestamp decrescente e limitar
            events.sortedByDescending { it.timestamp?.toDate()?.time ?: 0 }
                .take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting timeline events", e)
            emptyList()
        }
    }

    /**
     * Busca eventos de doses.
     */
    private suspend fun fetchDoseEvents(
        dependentId: String,
        filters: TimelineFilters,
        limit: Int
    ): List<TimelineEvent> {
        return try {
            var query = db.collection("dependentes").document(dependentId)
                .collection("historico_doses")
                .orderBy("timestampString", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            // Aplicar filtros de data se fornecidos
            if (filters.startDate != null) {
                val startString = filters.startDate.atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                query = query.whereGreaterThanOrEqualTo("timestampString", startString)
            }

            if (filters.endDate != null) {
                val endString = filters.endDate.plusDays(1).atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                query = query.whereLessThan("timestampString", endString)
            }

            val snapshot = query.get().await()
            val doseHistories = snapshot.toObjects(DoseHistory::class.java)

            // Buscar informações dos medicamentos para enriquecer os eventos
            val medicationIds = doseHistories.map { it.medicamentoId }.distinct()
            val medications = fetchMedicationsByIds(dependentId, medicationIds)
            val medicationMap = medications.associateBy { it.id }

            doseHistories.mapNotNull { dose ->
                val medication = medicationMap[dose.medicamentoId]
                if (medication != null) {
                    val eventType = when (dose.status.name) {
                        "TAKEN" -> TimelineEventType.DOSE_TAKEN
                        "SKIPPED" -> TimelineEventType.DOSE_SKIPPED
                        "MISSED" -> TimelineEventType.DOSE_MISSED
                        else -> TimelineEventType.DOSE_TAKEN
                    }
                    
                    val timestamp = try {
                        val instant = dose.timestamp.atZone(ZoneId.systemDefault()).toInstant()
                        Timestamp(instant.epochSecond, instant.nano)
                    } catch (e: Exception) {
                        Timestamp.now()
                    }
                    
                    TimelineEvent(
                        id = dose.id,
                        timestamp = timestamp,
                        description = buildDoseDescription(dose, medication),
                        authorName = getCurrentUserId() ?: "Sistema",
                        icon = "medication",
                        type = eventType.value,
                        originalCollection = "historico_doses",
                        originalDocId = dose.id
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching dose events", e)
            emptyList()
        }
    }

    /**
     * Busca eventos de agendamentos.
     */
    private suspend fun fetchAppointmentEvents(
        dependentId: String,
        filters: TimelineFilters,
        limit: Int
    ): List<TimelineEvent> {
        return try {
            var query = db.collection("dependentes").document(dependentId)
                .collection("agendamentos")
                .orderBy("timestampString", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            // Aplicar filtros de data
            if (filters.startDate != null) {
                val startString = filters.startDate.atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                query = query.whereGreaterThanOrEqualTo("timestampString", startString)
            }

            if (filters.endDate != null) {
                val endString = filters.endDate.plusDays(1).atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                query = query.whereLessThan("timestampString", endString)
            }

            val snapshot = query.get().await()
            val appointments = snapshot.toObjects(AgendamentoSaude::class.java)

            appointments.map { appointment ->
                val timestamp = try {
                    val instant = appointment.timestamp.atZone(ZoneId.systemDefault()).toInstant()
                    Timestamp(instant.epochSecond, instant.nano)
                } catch (e: Exception) {
                    Timestamp.now()
                }
                
                TimelineEvent(
                    id = appointment.id,
                    timestamp = timestamp,
                    description = buildAppointmentDescription(appointment),
                    authorName = getCurrentUserId() ?: "Sistema",
                    icon = "calendar",
                    type = TimelineEventType.APPOINTMENT.value,
                    originalCollection = "agendamentos",
                    originalDocId = appointment.id
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching appointment events", e)
            emptyList()
        }
    }

    /**
     * Busca eventos de documentos.
     */
    private suspend fun fetchDocumentEvents(
        dependentId: String,
        filters: TimelineFilters,
        limit: Int
    ): List<TimelineEvent> {
        return try {
            var query = db.collection("dependentes").document(dependentId)
                .collection("documentos_saude")
                .orderBy("dataDocumento", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            // Aplicar filtros de data
            if (filters.startDate != null) {
                query = query.whereGreaterThanOrEqualTo("dataDocumento", filters.startDate.toString())
            }

            if (filters.endDate != null) {
                query = query.whereLessThanOrEqualTo("dataDocumento", filters.endDate.toString())
            }

            val snapshot = query.get().await()
            val documents = snapshot.toObjects(DocumentoSaude::class.java)

            documents.map { document ->
                // Converter dataDocumento (String) para Timestamp
                val timestamp = try {
                    val localDate = LocalDate.parse(document.dataDocumento)
                    val instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    Timestamp(instant.epochSecond, instant.nano)
                } catch (e: Exception) {
                    Timestamp.now()
                }

                TimelineEvent(
                    id = document.id,
                    timestamp = timestamp,
                    description = buildDocumentDescription(document),
                    authorName = getCurrentUserId() ?: "Sistema",
                    icon = "document",
                    type = TimelineEventType.DOCUMENT_ADDED.value,
                    originalCollection = "documentos_saude",
                    originalDocId = document.id
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching document events", e)
            emptyList()
        }
    }

    /**
     * Busca eventos de medicamentos adicionados.
     */
    private suspend fun fetchMedicationEvents(
        dependentId: String,
        filters: TimelineFilters,
        limit: Int
    ): List<TimelineEvent> {
        return try {
            var query = db.collection("dependentes").document(dependentId)
                .collection("medicamentos")
                .orderBy("nome", Query.Direction.ASCENDING)
                .limit(limit.toLong())

            val snapshot = query.get().await()
            val medications = snapshot.toObjects(Medicamento::class.java)

            medications.map { medication ->
                TimelineEvent(
                    id = medication.id,
                    timestamp = Timestamp.now(), // Usar timestamp atual se não houver createdAt
                    description = buildMedicationDescription(medication),
                    authorName = getCurrentUserId() ?: "Sistema",
                    icon = "pill",
                    type = TimelineEventType.MEDICATION_ADDED.value,
                    originalCollection = "medicamentos",
                    originalDocId = medication.id
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching medication events", e)
            emptyList()
        }
    }

    /**
     * Busca medicamentos por IDs.
     */
    private suspend fun fetchMedicationsByIds(
        dependentId: String,
        medicationIds: List<String>
    ): List<Medicamento> {
        if (medicationIds.isEmpty()) return emptyList()

        return try {
            val medications = mutableListOf<Medicamento>()
            
            // Firestore tem limite de 10 itens em whereIn, então dividir em lotes
            medicationIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("dependentes").document(dependentId)
                    .collection("medicamentos")
                    .whereIn("id", chunk)
                    .get()
                    .await()
                
                medications.addAll(snapshot.toObjects(Medicamento::class.java))
            }
            
            medications
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching medications by IDs", e)
            emptyList()
        }
    }

    /**
     * Obtém estatísticas da timeline.
     */
    suspend fun getTimelineStats(dependentId: String): TimelineStats {
        if (dependentId.isBlank()) return TimelineStats()

        return try {
            val events = getTimelineEvents(dependentId, limit = 1000)

            TimelineStats(
                totalEvents = events.size,
                dosesTaken = events.count { it.type == TimelineEventType.DOSE_TAKEN.value },
                dosesSkipped = events.count { it.type == TimelineEventType.DOSE_SKIPPED.value },
                dosesMissed = events.count { it.type == TimelineEventType.DOSE_MISSED.value },
                appointments = events.count { it.type == TimelineEventType.APPOINTMENT.value },
                documentsAdded = events.count { it.type == TimelineEventType.DOCUMENT_ADDED.value },
                medicationsAdded = events.count { it.type == TimelineEventType.MEDICATION_ADDED.value }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting timeline stats", e)
            TimelineStats()
        }
    }

    // Métodos auxiliares para construir descrições

    private fun buildDoseDescription(dose: DoseHistory, medication: Medicamento): String {
        val status = when (dose.status.name) {
            "TAKEN" -> "tomada"
            "SKIPPED" -> "pulada"
            "MISSED" -> "perdida"
            else -> "registrada"
        }
        
        return buildString {
            append("Dose ${status}: ${medication.nome}")
            if (medication.dosagem.isNotBlank()) {
                append(" (${medication.dosagem})")
            }
            if (!dose.localDeAplicacao.isNullOrBlank()) {
                append(" - ${dose.localDeAplicacao}")
            }
            if (dose.quantidadeAdministrada != null) {
                append(" - ${dose.quantidadeAdministrada} unidade(s)")
            }
        }
    }

    private fun buildAppointmentDescription(appointment: AgendamentoSaude): String {
        return buildString {
            append(appointment.titulo)
            append(" (${appointment.tipo.name})")
            if (!appointment.local.isNullOrBlank()) {
                append(" - ${appointment.local}")
            }
            if (!appointment.profissional.isNullOrBlank()) {
                append(" com ${appointment.profissional}")
            }
        }
    }

    private fun buildDocumentDescription(document: DocumentoSaude): String {
        return buildString {
            append("Documento adicionado: ${document.titulo}")
            append(" (${document.tipo.name})")
            if (!document.medicoSolicitante.isNullOrBlank()) {
                append(" - Dr(a). ${document.medicoSolicitante}")
            }
        }
    }

    private fun buildMedicationDescription(medication: Medicamento): String {
        return buildString {
            append("Medicamento adicionado: ${medication.nome}")
            if (medication.dosagem.isNotBlank()) {
                append(" - ${medication.dosagem}")
            }
            if (medication.isUsoContinuo) {
                append(" (Uso contínuo)")
            }
        }
    }
}

