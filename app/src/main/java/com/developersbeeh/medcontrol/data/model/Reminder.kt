package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Reminder(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var type: String = "",
    var isActive: Boolean = true,

    // Este campo de texto será salvo no Firestore em vez do objeto LocalTime
    var timeString: String = ""
) {
    // Construtor vazio que o Firestore precisa para recriar o objeto
    constructor() : this("", "", "", true, "")

    // Esta propriedade 'time' será usada no seu código, mas ignorada pelo Firestore
    @get:Exclude @set:Exclude
    var time: LocalTime
        get() {
            // Converte a String de volta para LocalTime quando o código precisa ler
            return if (timeString.isNotBlank()) LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME) else LocalTime.now()
        }
        set(value) {
            // Converte o LocalTime para String quando o código precisa salvar
            timeString = value.format(DateTimeFormatter.ISO_LOCAL_TIME)
        }
}