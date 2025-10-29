package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Dependente(
    @DocumentId
    var id: String = "",
    val nome: String = "",
    val cuidadorIds: List<String> = emptyList(),
    val codigoDeVinculo: String = "",
    val senha: String = "",
    val usaAlarmeTelaCheia: Boolean = true,
    val dataDeNascimento: String = "",
    val peso: String = "",
    val altura: String = "",
    val pesoMeta: String = "",
    val sexo: String = Sexo.NAO_INFORMADO.name,
    val tipoSanguineo: String = TipoSanguineo.NAO_SABE.name,
    val metaAtividadeMinutos: Int = 30, // NOVO CAMPO ADICIONADO
    val metaHidratacaoMl: Int = 2000, // NOVO CAMPO ADICIONADO (com valor padrão)
    val metaCaloriasDiarias: Int = 2000, // NOVO CAMPO ADICIONADO
    val condicoesPreexistentes: String = "",
    val alergias: String = "",
    val observacoesMedicas: String = "",
    val contatoEmergenciaNome: String = "",
    val contatoEmergenciaTelefone: String = "",
    val photoUrl: String? = null,
    val permissoes: Map<String, Boolean> = mapOf(),
    @JvmField
    val isSelfCareProfile: Boolean = false,
    // ✅ NOVO CAMPO ADICIONADO
    var dataCriacao: String = ""
) : Parcelable {

    // ✅ NOVA PROPRIEDADE ADICIONADA
    @get:Exclude
    @set:Exclude
    var dataCriacaoLocalDateTime: LocalDateTime
        get() {
            return try {
                if (dataCriacao.isNotBlank()) LocalDateTime.parse(dataCriacao) else LocalDateTime.now()
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        }
        set(value) {
            dataCriacao = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
}