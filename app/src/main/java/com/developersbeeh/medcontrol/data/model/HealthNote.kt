    package com.developersbeeh.medcontrol.data.model

    import android.os.Parcelable
    import com.google.firebase.firestore.DocumentId
    import com.google.firebase.firestore.Exclude
    import kotlinx.parcelize.Parcelize
    import java.time.LocalDateTime
    import java.time.format.DateTimeFormatter
    import java.util.UUID

    @Parcelize
    data class HealthNote(
        @DocumentId
        var id: String = UUID.randomUUID().toString(),
        var dependentId: String = "",
        var userId: String = "",
        var type: HealthNoteType = HealthNoteType.GENERAL,
        var values: Map<String, String> = emptyMap(),
        var note: String? = null,
        @get:Exclude @set:Exclude
        var timestamp: LocalDateTime = LocalDateTime.now()
    ) : Parcelable {
        var timestampString: String
            get() = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            set(value) {
                timestamp = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
    }