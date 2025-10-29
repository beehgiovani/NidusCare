// src/main/java/com/developersbeeh/medcontrol/data/model/Atividade.kt
package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class TipoAtividade {
    DOSE_REGISTRADA,
    ANOTACAO_CRIADA,
    DOCUMENTO_ADICIONADO,
    AGENDAMENTO_CRIADO,
    MEDICAMENTO_CRIADO,
    MEDICAMENTO_EDITADO,
    MEDICAMENTO_EXCLUIDO,
    TRATAMENTO_PAUSADO,
    TRATAMENTO_REATIVADO,
    LEMBRETE_CRIADO,
    AI_CHAT_USED,
    PREDICTIVE_ANALYSIS_USED,
    RECIPE_SCANNER_USED,
    MEAL_ANALYSIS_USED,
    REPORT_CREATED
}

data class Atividade(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var descricao: String = "",
    var tipo: TipoAtividade = TipoAtividade.DOSE_REGISTRADA,
    var autorId: String = "",
    var autorNome: String = "",
    // O timestamp é salvo como String
    var timestampString: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
) {
    // Construtor vazio para o Firestore
    constructor() : this(id = UUID.randomUUID().toString())

    // Propriedade de conveniência para usar no código, ignorada pelo Firestore
    @get:Exclude
    @set:Exclude
    var timestamp: LocalDateTime
        get() = LocalDateTime.parse(timestampString)
        set(value) {
            timestampString = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}