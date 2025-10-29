package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class  AnalysisHistory(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var dependentId: String = "",
    var userId: String = "",
    var symptomsPrompt: String = "", // O que o utilizador escreveu
    var analysisResult: String = "", // A resposta da IA
    @get:Exclude @set:Exclude
    var timestamp: LocalDateTime = LocalDateTime.now()
) : Parcelable {
    var timestampString: String
        get() = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        set(value) {
            timestamp = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}