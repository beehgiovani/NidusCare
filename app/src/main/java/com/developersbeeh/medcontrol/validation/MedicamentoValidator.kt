package com.developersbeeh.medcontrol.validation

import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoDosagem
import java.time.LocalDate
import java.time.LocalTime

/**
 * Validador especializado para objetos Medicamento.
 * Implementa validações completas de dados e lógica de negócio.
 */
class MedicamentoValidator {

    companion object {
        private const val MIN_NOME_LENGTH = 2
        private const val MAX_NOME_LENGTH = 200
        private const val MAX_ANOTACOES_LENGTH = 1000
        private const val MAX_FUTURE_DAYS = 365
        private const val MIN_DOSAGEM_VALUE = 0.001
        private const val MAX_DOSAGEM_VALUE = 10000.0
        private const val MAX_HORARIOS = 24
    }

    /**
     * Valida um medicamento completo para salvamento.
     * Executa todas as validações necessárias.
     */
    fun validate(medicamento: Medicamento): ValidationResult {
        val builder = ValidationResultBuilder()

        validateBasicInfo(medicamento, builder)
        validateTreatmentDates(medicamento, builder)
        validateFrequency(medicamento, builder)
        validateSchedule(medicamento, builder)
        validateStock(medicamento, builder)
        validateVariableDosage(medicamento, builder)
        validateBusinessLogic(medicamento, builder)

        return builder.build()
    }

    /**
     * Valida apenas informações básicas do medicamento.
     */
    fun validateBasicInfo(medicamento: Medicamento): ValidationResult {
        val builder = ValidationResultBuilder()
        validateBasicInfo(medicamento, builder)
        return builder.build()
    }

    /**
     * Valida se o medicamento está apto para registro de dose.
     */
    fun validateForDoseRecording(medicamento: Medicamento): ValidationResult {
        val builder = ValidationResultBuilder()

        builder.addErrorIf(
            medicamento.isPaused,
            "status",
            "Não é possível registrar dose de medicamento pausado"
        )

        builder.addErrorIf(
            medicamento.isArchived,
            "status",
            "Não é possível registrar dose de medicamento arquivado"
        )

        builder.addErrorIf(
            medicamento.horarios.isEmpty() && !medicamento.isUsoEsporadico,
            "horarios",
            "Medicamento não possui horários definidos"
        )

        // Validação de estoque (warning, não erro crítico)
        if (medicamento.lotes.isNotEmpty()) {
            val estoqueTotal = medicamento.estoqueAtualTotal
            val dosagem = extractDosageValue(medicamento.dosagem)
            
            builder.addWarningIf(
                estoqueTotal < dosagem,
                "estoque",
                "Estoque insuficiente para esta dose. Estoque atual: $estoqueTotal ${medicamento.unidadeDeEstoque}"
            )
        }

        return builder.build()
    }

    /**
     * Valida informações básicas (nome, dosagem, anotações).
     */
    private fun validateBasicInfo(medicamento: Medicamento, builder: ValidationResultBuilder) {
        // Validação do nome
        builder.addErrorIf(
            medicamento.nome.isBlank(),
            "nome",
            "Nome do medicamento é obrigatório",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            medicamento.nome.length < MIN_NOME_LENGTH,
            "nome",
            "Nome do medicamento deve ter pelo menos $MIN_NOME_LENGTH caracteres"
        )

        builder.addErrorIf(
            medicamento.nome.length > MAX_NOME_LENGTH,
            "nome",
            "Nome do medicamento não pode exceder $MAX_NOME_LENGTH caracteres"
        )

        // Validação da dosagem
        builder.addErrorIf(
            medicamento.dosagem.isBlank(),
            "dosagem",
            "Dosagem é obrigatória",
            Severity.CRITICAL
        )

        val dosagemValue = extractDosageValue(medicamento.dosagem)
        builder.addErrorIf(
            dosagemValue < MIN_DOSAGEM_VALUE,
            "dosagem",
            "Valor da dosagem inválido ou muito baixo"
        )

        builder.addWarningIf(
            dosagemValue > MAX_DOSAGEM_VALUE,
            "dosagem",
            "Dosagem muito alta. Verifique se o valor está correto."
        )

        // Validação de anotações
        medicamento.anotacoes?.let { anotacoes ->
            builder.addErrorIf(
                anotacoes.length > MAX_ANOTACOES_LENGTH,
                "anotacoes",
                "Anotações não podem exceder $MAX_ANOTACOES_LENGTH caracteres"
            )
        }
    }

    /**
     * Valida datas de tratamento.
     */
    private fun validateTreatmentDates(medicamento: Medicamento, builder: ValidationResultBuilder) {
        val today = LocalDate.now()
        val startDate = medicamento.dataInicioTratamento

        // Data de início não pode ser muito no futuro
        builder.addWarningIf(
            startDate.isAfter(today.plusDays(MAX_FUTURE_DAYS.toLong())),
            "dataInicioTratamento",
            "Data de início do tratamento está muito no futuro (mais de $MAX_FUTURE_DAYS dias)"
        )

        // Se não for uso contínuo, deve ter duração
        if (!medicamento.isUsoContinuo && !medicamento.isUsoEsporadico) {
            builder.addErrorIf(
                medicamento.duracaoDias <= 0,
                "duracaoDias",
                "Duração do tratamento deve ser maior que zero para tratamentos com prazo definido"
            )

            builder.addWarningIf(
                medicamento.duracaoDias > 365,
                "duracaoDias",
                "Duração do tratamento muito longa (mais de 1 ano). Considere marcar como uso contínuo."
            )
        }
    }

    /**
     * Valida configurações de frequência.
     */
    private fun validateFrequency(medicamento: Medicamento, builder: ValidationResultBuilder) {
        when (medicamento.frequenciaTipo) {
            FrequenciaTipo.SEMANAL -> {
                builder.addErrorIf(
                    medicamento.diasSemana.isEmpty(),
                    "diasSemana",
                    "Para frequência semanal, é necessário selecionar pelo menos um dia da semana",
                    Severity.CRITICAL
                )

                builder.addErrorIf(
                    medicamento.diasSemana.any { it < 1 || it > 7 },
                    "diasSemana",
                    "Dias da semana devem estar entre 1 (segunda) e 7 (domingo)"
                )
            }

            FrequenciaTipo.INTERVALO_DIAS -> {
                builder.addErrorIf(
                    medicamento.frequenciaValor <= 0,
                    "frequenciaValor",
                    "Intervalo de dias deve ser maior que zero",
                    Severity.CRITICAL
                )

                builder.addWarningIf(
                    medicamento.frequenciaValor > 90,
                    "frequenciaValor",
                    "Intervalo de dias muito longo (mais de 90 dias). Verifique se está correto."
                )
            }

            FrequenciaTipo.DIARIA -> {
                // Nenhuma validação adicional necessária
            }
        }
    }

    /**
     * Valida horários do medicamento.
     */
    private fun validateSchedule(medicamento: Medicamento, builder: ValidationResultBuilder) {
        // Medicamentos não esporádicos devem ter horários
        if (!medicamento.isUsoEsporadico) {
            builder.addErrorIf(
                medicamento.horarios.isEmpty(),
                "horarios",
                "É necessário definir pelo menos um horário para medicamentos de uso regular",
                Severity.CRITICAL
            )
        }

        // Validar número máximo de horários
        builder.addErrorIf(
            medicamento.horarios.size > MAX_HORARIOS,
            "horarios",
            "Número máximo de horários excedido (máximo: $MAX_HORARIOS)"
        )

        // Validar horários duplicados
        val horariosUnicos = medicamento.horarios.toSet()
        builder.addErrorIf(
            horariosUnicos.size != medicamento.horarios.size,
            "horarios",
            "Existem horários duplicados na lista"
        )

        // Validar que horários estão em ordem
        if (medicamento.horarios.size > 1) {
            val sorted = medicamento.horarios.sorted()
            builder.addWarningIf(
                medicamento.horarios != sorted,
                "horarios",
                "Horários não estão em ordem cronológica"
            )
        }

        // Validar intervalo mínimo entre horários (warning se < 1 hora)
        if (medicamento.horarios.size > 1) {
            val sortedHorarios = medicamento.horarios.sorted()
            for (i in 0 until sortedHorarios.size - 1) {
                val intervalo = java.time.Duration.between(sortedHorarios[i], sortedHorarios[i + 1])
                builder.addWarningIf(
                    intervalo.toMinutes() < 60,
                    "horarios",
                    "Intervalo muito curto entre horários (menos de 1 hora entre ${sortedHorarios[i]} e ${sortedHorarios[i + 1]})"
                )
            }
        }
    }

    /**
     * Valida informações de estoque.
     */
    private fun validateStock(medicamento: Medicamento, builder: ValidationResultBuilder) {
        medicamento.lotes.forEachIndexed { index, lote ->
            // Quantidade deve ser positiva
            builder.addErrorIf(
                lote.quantidade < 0,
                "lotes[$index].quantidade",
                "Quantidade do lote não pode ser negativa"
            )

            // Quantidade inicial deve ser >= quantidade atual
            builder.addErrorIf(
                lote.quantidadeInicial < lote.quantidade,
                "lotes[$index].quantidadeInicial",
                "Quantidade inicial não pode ser menor que a quantidade atual"
            )

            // Data de validade não deve estar no passado (warning)
            builder.addWarningIf(
                lote.dataValidade.isBefore(LocalDate.now()),
                "lotes[$index].dataValidade",
                "Lote ${lote.lote ?: lote.id} está vencido"
            )

            // Alerta de vencimento próximo (30 dias)
            builder.addWarningIf(
                lote.dataValidade.isBefore(LocalDate.now().plusDays(30)) && 
                lote.dataValidade.isAfter(LocalDate.now()),
                "lotes[$index].dataValidade",
                "Lote ${lote.lote ?: lote.id} vence em breve (${lote.dataValidade})"
            )
        }

        // Validar nível de alerta de estoque
        builder.addErrorIf(
            medicamento.nivelDeAlertaEstoque < 0,
            "nivelDeAlertaEstoque",
            "Nível de alerta de estoque não pode ser negativo"
        )

        // Alerta se estoque atual está abaixo do nível de alerta
        if (medicamento.nivelDeAlertaEstoque > 0) {
            builder.addWarningIf(
                medicamento.estoqueAtualTotal < medicamento.nivelDeAlertaEstoque,
                "estoque",
                "Estoque atual (${medicamento.estoqueAtualTotal}) está abaixo do nível de alerta (${medicamento.nivelDeAlertaEstoque})"
            )
        }
    }

    /**
     * Valida configurações de dosagem variável (ex: insulina).
     */
    private fun validateVariableDosage(medicamento: Medicamento, builder: ValidationResultBuilder) {
        if (medicamento.tipoDosagem == TipoDosagem.CALCULADA) {
            builder.addErrorIf(
                medicamento.glicemiaAlvo == null || medicamento.glicemiaAlvo!! <= 0,
                "glicemiaAlvo",
                "Glicemia alvo é obrigatória para dosagem variável"
            )

            builder.addErrorIf(
                medicamento.fatorSensibilidade == null || medicamento.fatorSensibilidade!! <= 0,
                "fatorSensibilidade",
                "Fator de sensibilidade é obrigatório para dosagem variável"
            )

            builder.addErrorIf(
                medicamento.ratioCarboidrato == null || medicamento.ratioCarboidrato!! <= 0,
                "ratioCarboidrato",
                "Ratio de carboidrato é obrigatório para dosagem variável"
            )

            // Validar valores razoáveis
            medicamento.glicemiaAlvo?.let { alvo ->
                builder.addWarningIf(
                    alvo < 70 || alvo > 180,
                    "glicemiaAlvo",
                    "Glicemia alvo fora da faixa típica (70-180 mg/dL)"
                )
            }
        }
    }

    /**
     * Valida lógica de negócio e regras complexas.
     */
    private fun validateBusinessLogic(medicamento: Medicamento, builder: ValidationResultBuilder) {
        // Um medicamento não pode ser simultaneamente uso contínuo e esporádico
        builder.addErrorIf(
            medicamento.isUsoContinuo && medicamento.isUsoEsporadico,
            "tipo_uso",
            "Medicamento não pode ser simultaneamente de uso contínuo e esporádico",
            Severity.CRITICAL
        )

        // Se for uso esporádico, não deve ter horários fixos
        builder.addWarningIf(
            medicamento.isUsoEsporadico && medicamento.horarios.isNotEmpty(),
            "horarios",
            "Medicamentos de uso esporádico geralmente não possuem horários fixos"
        )

        // Se estiver pausado, alertar sobre notificações
        builder.addWarningIf(
            medicamento.isPaused && medicamento.usaNotificacao,
            "notificacoes",
            "Medicamento pausado não receberá notificações"
        )

        // Validar que userId está presente
        builder.addErrorIf(
            medicamento.userId.isBlank(),
            "userId",
            "ID do usuário é obrigatório",
            Severity.CRITICAL
        )
    }

    /**
     * Extrai o valor numérico de uma string de dosagem.
     * Exemplo: "500mg" -> 500.0
     */
    private fun extractDosageValue(dosageString: String): Double {
        return try {
            val cleanedString = dosageString.replace(",", ".").trim()
            val regex = Regex("^(\\d*\\.?\\d+)")
            val matchResult = regex.find(cleanedString)
            matchResult?.value?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}

