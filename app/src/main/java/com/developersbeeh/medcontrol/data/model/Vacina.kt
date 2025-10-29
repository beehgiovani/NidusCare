// src/main/java/com/developersbeeh/medcontrol/data/model/Vacina.kt

package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

/**
 * Representa uma vacina do calendário de vacinação padrão.
 * Esta classe é usada para exibir a lista completa de vacinas.
 */
@Parcelize
data class Vacina(
    val id: String = "",
    val nome: String = "",
    val previne: String = "",
    val dose: String = "", // Ex: "1ª Dose", "Reforço", "Dose Única"
    val idadeRecomendadaMeses: Int = 0 // Idade em meses para a aplicação
) : Parcelable
@Parcelize
data class RegistroVacina(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var vacinaId: String = "", // Corresponde ao 'id' da classe Vacina
    var lote: String? = null,
    var localAplicacao: String? = null,
    var notas: String? = null,
    // ✅ CORREÇÃO: Campo 'dataAplicacao' do tipo Date trocado por 'timestampString'
    var timestampString: String = ""
) : Parcelable {
    // Construtor vazio para o Firestore
    constructor() : this(timestampString = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    // ✅ CORREÇÃO: Getter/setter para conversão automática para LocalDateTime
    @get:Exclude
    @set:Exclude
    var timestamp: LocalDateTime
        get() = if (timestampString.isNotBlank()) LocalDateTime.parse(timestampString) else LocalDateTime.now()
        set(value) {
            timestampString = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}