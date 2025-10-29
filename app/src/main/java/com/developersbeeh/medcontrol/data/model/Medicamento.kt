package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.developersbeeh.medcontrol.util.separarDosagem
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class Medicamento(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var nome: String = "",
    var dosagem: String = "",
    var principioAtivo: String? = null,
    var classeTerapeutica: String? = null,
    var laboratorio: String? = null,
    var registroAnvisa: String? = null,
    var apresentacao: String? = null,
    var bulaLink: String? = null,
    var anotacoes: String? = null,
    var tipo: TipoMedicamento = TipoMedicamento.ORAL,

    @get:Exclude @set:Exclude
    var dataInicioTratamento: LocalDate = LocalDate.now(),
    var duracaoDias: Int = 0,
    var isUsoContinuo: Boolean = false,
    var isUsoEsporadico: Boolean = false,
    var isPaused: Boolean = false,

    var frequenciaTipo: FrequenciaTipo = FrequenciaTipo.DIARIA,
    var frequenciaValor: Int = 1,
    var diasSemana: List<Int> = emptyList(),
    @get:Exclude @set:Exclude
    var horarios: List<LocalTime> = emptyList(),

    var usaNotificacao: Boolean = true,
    var missedDoseAlertSent: Boolean = false,

    var lotes: List<EstoqueLote> = emptyList(),
    var nivelDeAlertaEstoque: Int = 0,
    var alertaDeEstoqueEnviado: Boolean = false,
    var locaisDeAplicacao: List<String> = emptyList(),

    var tipoDosagem: TipoDosagem = TipoDosagem.FIXA,
    var glicemiaAlvo: Double? = null,
    var fatorSensibilidade: Double? = null,
    var ratioCarboidrato: Double? = null,

    var userId: String = "",
    var dataCriacao: String = "",

    var isArchived: Boolean = false,

    @get:Exclude @set:Exclude
    var posologia: String = ""

) : Parcelable {

    constructor() : this(id = UUID.randomUUID().toString())

    val estoqueAtualTotal: Double
        @Exclude get() = lotes.sumOf { it.quantidade }

    val estoqueInicialTotal: Double
        @Exclude get() = lotes.sumOf { it.quantidadeInicial }

    val unidadeDeEstoque: String
        @Exclude get() {
            val (_, unidade) = dosagem.separarDosagem()
            return unidade.ifBlank { "unidades" }
        }

    var dataInicioTratamentoString: String
        get() = dataInicioTratamento.format(DateTimeFormatter.ISO_LOCAL_DATE)
        set(value) {
            dataInicioTratamento = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        }

    var horariosString: List<String>
        get() = horarios.map { it.format(DateTimeFormatter.ISO_LOCAL_TIME) }
        set(value) {
            horarios = value.map { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
        }

    @get:Exclude @set:Exclude
    var dataCriacaoLocalDateTime: LocalDateTime
        get() {
            return try {
                // ✅ CORREÇÃO: Tenta parsear. Se falhar ou estiver em branco, usa o início do dia.
                if (dataCriacao.isNotBlank()) {
                    LocalDateTime.parse(dataCriacao)
                } else {
                    this.dataInicioTratamento.atStartOfDay()
                }
            } catch (e: Exception) {
                this.dataInicioTratamento.atStartOfDay()
            }
        }
        set(value) {
            dataCriacao = value.toString()
        }
}