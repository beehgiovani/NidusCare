package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modelo de dados para medicamentos armazenados no Realtime Database
 * Representa a estrutura da base de dados de medicamentos da ANVISA
 */
@Parcelize
data class MedicamentoDatabase(
    var id: String = "",
    val CATEGORIA_REGULATORIA: String = "",
    val CLASSE_TERAPEUTICA: String = "",
    val NOME_PRODUTO: String = "",
    val PRINCIPIO_ATIVO: String = "",
    val TIPO_PRODUTO: String = ""
) : Parcelable {

    /**
     * Converte um medicamento da base de dados para um medicamento do usuário
     */
    fun toMedicamentoUsuario(): Medicamento {
        return Medicamento(
            nome = NOME_PRODUTO.trim(),
            dosagem = "", // Será preenchido pelo usuário
            principioAtivo = PRINCIPIO_ATIVO,
            classeTerapeutica = CLASSE_TERAPEUTICA,
            laboratorio = null, // Não disponível em MedicamentoDatabase
            registroAnvisa = null, // Não disponível em MedicamentoDatabase
            apresentacao = null, // Não disponível em MedicamentoDatabase
            bulaLink = null, // Não disponível em MedicamentoDatabase
            anotacoes = "Categoria: $CATEGORIA_REGULATORIA"
        )
    }

    /**
     * Retorna uma descrição completa do medicamento
     */
    fun getDescricaoCompleta(): String {
        return buildString {
            append(NOME_PRODUTO)
            if (PRINCIPIO_ATIVO.isNotBlank()) {
                append("\nPrincípio Ativo: $PRINCIPIO_ATIVO")
            }
            if (CLASSE_TERAPEUTICA.isNotBlank()) {
                append("\nClasse Terapêutica: $CLASSE_TERAPEUTICA")
            }
            if (CATEGORIA_REGULATORIA.isNotBlank()) {
                append("\nCategoria: $CATEGORIA_REGULATORIA")
            }
        }
    }

    /**
     * Verifica se o medicamento contém o termo de busca
     */
    fun containsSearchTerm(searchTerm: String): Boolean {
        val term = searchTerm.lowercase()
        return NOME_PRODUTO.lowercase().contains(term) ||
                PRINCIPIO_ATIVO.lowercase().contains(term) ||
                CLASSE_TERAPEUTICA.lowercase().contains(term)
    }
}
