// src/main/java/com/developersbeeh/niduscare/data/model/EstoqueLote.kt

package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class EstoqueLote(
    var id: String = UUID.randomUUID().toString(),
    var quantidade: Double = 0.0,
    var quantidadeInicial: Double = 0.0,
    var dataValidadeString: String = "",
    // --- NOVO CAMPO DE CONTROLE ADICIONADO ---
    var alertaValidadeEnviado: Boolean = false,
    var lote: String? = null
) : Parcelable {

    @get:Exclude
    @set:Exclude
    var dataValidade: LocalDate
        get() = if (dataValidadeString.isNotBlank()) LocalDate.parse(dataValidadeString, DateTimeFormatter.ISO_LOCAL_DATE) else LocalDate.now()
        set(value) {
            dataValidadeString = value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
}