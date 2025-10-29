// src/main/java/com/developersbeeh/medcontrol/data/model/Insight.kt
package com.developersbeeh.medcontrol.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class Insight(
    @DocumentId
    var id: String = "",
    val type: String = "",
    val title: String = "",
    val description: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimestampAsLocalDateTime(): LocalDateTime {
        val instant = timestamp?.toInstant() ?: Instant.now()
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    }
}