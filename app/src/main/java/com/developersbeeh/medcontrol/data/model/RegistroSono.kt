// src/main/java/com/developersbeeh/medcontrol/data/model/RegistroSono.kt
package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class QualidadeSono(val displayName: String) {
    RUIM("Ruim"),
    RAZOAVEL("Razoável"),
    BOM("Bom")
}

data class RegistroSono(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var data: String = "", // Formato "AAAA-MM-DD"
    var horaDeDormir: String = "", // Formato "HH:mm"
    var horaDeAcordar: String = "", // Formato "HH:mm"
    var qualidade: String = QualidadeSono.RAZOAVEL.name,
    var notas: String? = null,
    var interrupcoes: Int = 0 // ✅ NOVO CAMPO ADICIONADO
) {
    // Funções auxiliares para facilitar o uso no código e evitar crashes
    fun getDataAsLocalDate(): LocalDate {
        return try {
            LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now() // Retorna um valor padrão em caso de erro
        }
    }

    fun getHoraDeDormirAsLocalTime(): LocalTime {
        return try {
            LocalTime.parse(horaDeDormir, DateTimeFormatter.ISO_LOCAL_TIME)
        } catch (e: Exception) {
            LocalTime.MIDNIGHT // Retorna um valor padrão em caso de erro
        }
    }

    fun getHoraDeAcordarAsLocalTime(): LocalTime {
        return try {
            LocalTime.parse(horaDeAcordar, DateTimeFormatter.ISO_LOCAL_TIME)
        } catch (e: Exception) {
            LocalTime.now() // Retorna um valor padrão em caso de erro
        }
    }
}