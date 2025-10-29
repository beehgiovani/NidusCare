package com.developersbeeh.medcontrol.validation

import com.developersbeeh.medcontrol.data.model.AgendamentoSaude
import java.time.LocalDateTime

/**
 * Validador especializado para objetos AgendamentoSaude.
 * Implementa validações de dados, horários e lógica de negócio.
 */
class AgendamentoSaudeValidator {

    companion object {
        private const val MIN_TITULO_LENGTH = 3
        private const val MAX_TITULO_LENGTH = 200
        private const val MAX_NOTAS_LENGTH = 2000
        private const val MAX_LEMBRETES = 10
        private const val MAX_REMINDER_DAYS = 30
        private const val MIN_ADVANCE_HOURS = 1
    }

    /**
     * Valida um agendamento completo para salvamento.
     */
    fun validate(agendamento: AgendamentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()

        validateBasicInfo(agendamento, builder)
        validateDateTime(agendamento, builder)
        validateReminders(agendamento, builder)
        validateBusinessLogic(agendamento, builder)

        return builder.build()
    }

    /**
     * Valida apenas informações básicas do agendamento.
     */
    fun validateBasicInfo(agendamento: AgendamentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()
        validateBasicInfo(agendamento, builder)
        return builder.build()
    }

    /**
     * Valida se há conflito de horário com outros agendamentos.
     */
    fun validateConflict(
        agendamento: AgendamentoSaude,
        existingSchedules: List<AgendamentoSaude>,
        durationMinutes: Int = 60
    ): ValidationResult {
        val builder = ValidationResultBuilder()

        val startTime = agendamento.timestamp
        val endTime = startTime.plusMinutes(durationMinutes.toLong())

        existingSchedules.forEach { existing ->
            // Ignorar o próprio agendamento se estiver editando
            if (existing.id != agendamento.id && existing.isActive) {
                val existingStart = existing.timestamp
                val existingEnd = existingStart.plusMinutes(durationMinutes.toLong())

                // Verificar sobreposição de horários
                val hasConflict = (startTime.isBefore(existingEnd) && endTime.isAfter(existingStart))

                if (hasConflict) {
                    builder.addWarning(
                        "timestamp",
                        "Conflito de horário com agendamento existente: ${existing.titulo} às ${existingStart}"
                    )
                }
            }
        }

        return builder.build()
    }

    /**
     * Valida informações básicas (título, tipo, local, profissional).
     */
    private fun validateBasicInfo(agendamento: AgendamentoSaude, builder: ValidationResultBuilder) {
        // Validação do título
        builder.addErrorIf(
            agendamento.titulo.isBlank(),
            "titulo",
            "Título do agendamento é obrigatório",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            agendamento.titulo.length < MIN_TITULO_LENGTH,
            "titulo",
            "Título deve ter pelo menos $MIN_TITULO_LENGTH caracteres"
        )

        builder.addErrorIf(
            agendamento.titulo.length > MAX_TITULO_LENGTH,
            "titulo",
            "Título não pode exceder $MAX_TITULO_LENGTH caracteres"
        )

        // Validação de local
        agendamento.local?.let { local ->
            builder.addWarningIf(
                local.isBlank(),
                "local",
                "Local do agendamento não foi informado"
            )

            builder.addWarningIf(
                local.length < 3,
                "local",
                "Nome do local parece muito curto"
            )
        }

        // Validação de profissional
        agendamento.profissional?.let { profissional ->
            builder.addWarningIf(
                profissional.isBlank(),
                "profissional",
                "Nome do profissional não foi informado"
            )

            builder.addWarningIf(
                profissional.length < 3,
                "profissional",
                "Nome do profissional parece muito curto"
            )
        }

        // Validação de notas de preparo
        agendamento.notasDePreparo?.let { notas ->
            builder.addErrorIf(
                notas.length > MAX_NOTAS_LENGTH,
                "notasDePreparo",
                "Notas de preparo não podem exceder $MAX_NOTAS_LENGTH caracteres"
            )
        }

        // Validação do dependentId
        builder.addErrorIf(
            agendamento.dependentId.isBlank(),
            "dependentId",
            "ID do dependente é obrigatório",
            Severity.CRITICAL
        )
    }

    /**
     * Valida data e hora do agendamento.
     */
    private fun validateDateTime(agendamento: AgendamentoSaude, builder: ValidationResultBuilder) {
        val now = LocalDateTime.now()
        val timestamp = agendamento.timestamp

        // Agendamento deve ser no futuro (com margem de 1 hora)
        val minTime = now.plusHours(MIN_ADVANCE_HOURS.toLong())
        builder.addWarningIf(
            timestamp.isBefore(minTime),
            "timestamp",
            "Agendamento está muito próximo ou no passado. Recomenda-se agendar com pelo menos $MIN_ADVANCE_HOURS hora de antecedência."
        )

        // Validar horário comercial (warning)
        val hour = timestamp.hour
        builder.addWarningIf(
            hour < 6 || hour > 22,
            "timestamp",
            "Agendamento fora do horário comercial típico (6h-22h)"
        )

        // Validar se não está muito no futuro (mais de 2 anos)
        builder.addWarningIf(
            timestamp.isAfter(now.plusYears(2)),
            "timestamp",
            "Agendamento muito distante no futuro (mais de 2 anos)"
        )

        // Validar timestampString
        builder.addErrorIf(
            agendamento.timestampString.isBlank(),
            "timestampString",
            "Timestamp do agendamento é obrigatório",
            Severity.CRITICAL
        )
    }

    /**
     * Valida configurações de lembretes.
     */
    private fun validateReminders(agendamento: AgendamentoSaude, builder: ValidationResultBuilder) {
        val lembretes = agendamento.lembretes

        // Validar número máximo de lembretes
        builder.addErrorIf(
            lembretes.size > MAX_LEMBRETES,
            "lembretes",
            "Número máximo de lembretes excedido (máximo: $MAX_LEMBRETES)"
        )

        // Validar que lembretes são positivos
        lembretes.forEachIndexed { index, minutos ->
            builder.addErrorIf(
                minutos < 0,
                "lembretes[$index]",
                "Tempo de lembrete não pode ser negativo"
            )

            // Validar limite máximo (30 dias = 43200 minutos)
            val maxMinutes = MAX_REMINDER_DAYS * 24 * 60
            builder.addWarningIf(
                minutos > maxMinutes,
                "lembretes[$index]",
                "Lembrete muito antecipado (mais de $MAX_REMINDER_DAYS dias antes)"
            )
        }

        // Validar lembretes duplicados
        val lembretesUnicos = lembretes.toSet()
        builder.addWarningIf(
            lembretesUnicos.size != lembretes.size,
            "lembretes",
            "Existem lembretes duplicados"
        )

        // Validar que lembretes estão em ordem
        if (lembretes.size > 1) {
            val sorted = lembretes.sorted()
            builder.addWarningIf(
                lembretes != sorted,
                "lembretes",
                "Lembretes não estão em ordem crescente"
            )
        }

        // Sugerir lembretes padrão se não houver nenhum
        builder.addWarningIf(
            lembretes.isEmpty(),
            "lembretes",
            "Nenhum lembrete configurado. Considere adicionar lembretes para não esquecer o agendamento."
        )
    }

    /**
     * Valida lógica de negócio e regras complexas.
     */
    private fun validateBusinessLogic(agendamento: AgendamentoSaude, builder: ValidationResultBuilder) {
        // Se agendamento está inativo, alertar sobre lembretes
        builder.addWarningIf(
            !agendamento.isActive && agendamento.lembretes.isNotEmpty(),
            "status",
            "Agendamento inativo não enviará lembretes"
        )

        // Validar que ID não está vazio
        builder.addErrorIf(
            agendamento.id.isBlank(),
            "id",
            "ID do agendamento é obrigatório",
            Severity.CRITICAL
        )

        // Sugerir informações adicionais para tipos específicos
        when (agendamento.tipo.name) {
            "EXAME" -> {
                builder.addWarningIf(
                    agendamento.notasDePreparo.isNullOrBlank(),
                    "notasDePreparo",
                    "Considere adicionar notas de preparo (ex: jejum, medicamentos)"
                )
            }
            "CIRURGIA" -> {
                builder.addWarningIf(
                    agendamento.local.isNullOrBlank(),
                    "local",
                    "Local é especialmente importante para cirurgias"
                )
                builder.addWarningIf(
                    agendamento.profissional.isNullOrBlank(),
                    "profissional",
                    "Nome do cirurgião é importante"
                )
            }
            "VACINA" -> {
                builder.addWarningIf(
                    agendamento.local.isNullOrBlank(),
                    "local",
                    "Informe o local de vacinação"
                )
            }
        }
    }

    /**
     * Valida se um agendamento pode ser deletado.
     */
    fun validateForDeletion(agendamento: AgendamentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()

        builder.addErrorIf(
            agendamento.id.isBlank(),
            "id",
            "ID do agendamento é inválido",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            agendamento.dependentId.isBlank(),
            "dependentId",
            "ID do dependente é inválido",
            Severity.CRITICAL
        )

        // Warning se tentar deletar agendamento muito próximo
        val now = LocalDateTime.now()
        val hoursUntil = java.time.Duration.between(now, agendamento.timestamp).toHours()
        
        builder.addWarningIf(
            hoursUntil in 0..24,
            "timestamp",
            "Agendamento está próximo (menos de 24 horas). Tem certeza que deseja deletar?"
        )

        return builder.build()
    }

    /**
     * Valida se um agendamento pode ter seu status alterado.
     */
    fun validateStatusChange(agendamento: AgendamentoSaude, newStatus: Boolean): ValidationResult {
        val builder = ValidationResultBuilder()

        // Se está desativando um agendamento futuro, alertar
        if (!newStatus && agendamento.timestamp.isAfter(LocalDateTime.now())) {
            builder.addWarning(
                "status",
                "Desativar agendamento futuro impedirá o envio de lembretes"
            )
        }

        return builder.build()
    }
}

