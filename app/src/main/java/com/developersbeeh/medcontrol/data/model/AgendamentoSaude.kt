// src/main/java/com/developersbeeh/medcontrol/data/model/AgendamentoSaude.kt
package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class AgendamentoSaude(
    var id: String = UUID.randomUUID().toString(),
    var dependentId: String = "",
    var titulo: String = "",
    var tipo: TipoAgendamento = TipoAgendamento.CONSULTA,
    var local: String? = null,
    var profissional: String? = null,
    var notasDePreparo: String? = null,
    var lembretes: List<Int> = listOf(120, 1440),
    var isActive: Boolean = true,
    var timestampString: String = ""
) : Parcelable {
    constructor() : this(id = UUID.randomUUID().toString())

    @get:Exclude
    @set:Exclude
    var timestamp: LocalDateTime
        get() = if (timestampString.isNotBlank()) LocalDateTime.parse(timestampString) else LocalDateTime.now()
        set(value) {
            timestampString = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}