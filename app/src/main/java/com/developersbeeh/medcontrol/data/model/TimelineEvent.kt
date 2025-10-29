package com.developersbeeh.medcontrol.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class TimelineEvent(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @ServerTimestamp
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Timestamp? = null,
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",

    // ✅ CORREÇÃO: Este é o campo que o 'createTimelineEvent' popula
    @get:PropertyName("authorName") @set:PropertyName("authorName") var authorName: String = "",

    @get:PropertyName("icon") @set:PropertyName("icon") var icon: String = "default",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "GENERIC",
    @get:PropertyName("originalCollection") @set:PropertyName("originalCollection") var originalCollection: String = "",
    @get:PropertyName("originalDocId") @set:PropertyName("originalDocId") var originalDocId: String = ""
) {
    // Construtor vazio para o Firestore
    constructor() : this(id = "")

    fun getLocalDateTime(): LocalDateTime {
        val instant = timestamp?.toDate()?.toInstant() ?: Instant.now()
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }
}