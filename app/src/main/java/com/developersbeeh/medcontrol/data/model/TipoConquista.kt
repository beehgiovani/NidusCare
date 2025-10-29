// src/main/java/com/developersbeeh/medcontrol/data/model/TipoConquista.kt

package com.developersbeeh.medcontrol.data.model

import com.developersbeeh.medcontrol.R

// Enum para mapear todos os tipos de conquistas possíveis no app
enum class TipoConquista(
    val titulo: String,
    val descricao: String,
    val iconRes: Int
) {
    PRIMEIRO_MEDICAMENTO(
        "Novato Engajado",
        "Você adicionou seu primeiro medicamento!",
        R.drawable.ic_medicine
    ),
    PRIMEIRO_DOCUMENTO(
        "Tudo Organizado",
        "Você adicionou seu primeiro documento de saúde.",
        R.drawable.ic_file_document
    ),
    DEZ_DOSES_REGISTRADAS(
        "Bom Começo",
        "Você registrou suas primeiras 10 doses.",
        R.drawable.ic_check_circle
    ),
    CEM_DOSES_REGISTRADAS(
        "Maratonista do Cuidado",
        "Parabéns por registrar 100 doses!",
        R.drawable.ic_military_tech // Um ícone de medalha
    ),
    SETE_DIAS_ADESAO_PERFEITA(
        "Semana Perfeita",
        "Você atingiu 7 dias seguidos com 100% de adesão.",
        R.drawable.ic_whatshot
    ),
    PERFIL_COMPLETO(
        "Tudo Pronto!",
        "Você preencheu todas as informações de saúde do perfil.",
        R.drawable.ic_person
    )
    // Podemos adicionar muitas outras conquistas aqui no futuro!
}