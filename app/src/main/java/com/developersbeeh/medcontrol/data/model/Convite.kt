package com.developersbeeh.medcontrol.data.model

import com.google.firebase.firestore.DocumentId

// Enum para o status do convite
enum class StatusConvite {
    PENDENTE,
    ACEITO,
    RECUSADO
}

data class Convite(
    @DocumentId
    var id: String = "",
    val dependenteId: String = "",
    val dependenteNome: String = "",
    val remetenteId: String = "",
    val remetenteNome: String = "",
    val destinatarioEmail: String = "", // O e-mail de quem está sendo convidado
    val status: StatusConvite = StatusConvite.PENDENTE
) {
    // Função auxiliar para verificar se o convite está vinculado a um dependente válido
    fun hasDependente(): Boolean {
        return dependenteId.isNotBlank() && dependenteNome.isNotBlank()
    }

    // Texto amigável para exibição, mesmo quando não há dependente
    fun getDisplayDependenteNome(): String {
        return if (dependenteNome.isNotBlank()) dependenteNome else "um dependente"
    }
}