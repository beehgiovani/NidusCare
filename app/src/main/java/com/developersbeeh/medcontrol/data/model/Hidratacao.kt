package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDate // Importar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Hidratacao(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val quantidadeMl: Int = 0,
    var timestampString: String = "",
    // ✅ CAMPO ADICIONADO
    var dateString: String = ""
) {
    constructor() : this(timestampString = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    @get:Exclude
    @set:Exclude
    var timestamp: LocalDateTime
        get() = if (timestampString.isNotBlank()) LocalDateTime.parse(timestampString) else LocalDateTime.now()
        set(value) {
            timestampString = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // ✅ ATUALIZA O NOVO CAMPO AUTOMATICAMENTE
            dateString = value.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

    // ✅ Inicializador para garantir que ambos os campos sejam definidos
    init {
        if (timestampString.isNotBlank() && dateString.isBlank()) {
            timestamp = LocalDateTime.parse(timestampString)
        } else if (dateString.isNotBlank() && timestampString.isBlank()) {
            timestamp = LocalDate.parse(dateString).atStartOfDay()
        } else {
            // Define ambos se estiverem vazios
            timestamp = LocalDateTime.now()
        }
    }
}