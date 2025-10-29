package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class Sender {
    USER, AI
}

data class ChatMessage(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var text: String = "",
    var sender: String = Sender.USER.name,
    // ✅ CORREÇÃO: Campo 'timestamp' do tipo Date trocado por 'timestampString'
    var timestampString: String = ""
) {
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