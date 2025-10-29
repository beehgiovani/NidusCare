package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp // ✅ Importe a classe Timestamp

data class Familia(
    @DocumentId
    var id: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    val activeSubscriptionToken: String? = null,
    // ✅ TIPO DO CAMPO ATUALIZADO
    val subscriptionExpiryDate: Timestamp? = null
)