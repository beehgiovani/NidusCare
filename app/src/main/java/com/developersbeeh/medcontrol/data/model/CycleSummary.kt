package com.developersbeeh.medcontrol.data.model

import java.time.LocalDate

/**
 * Representa um único ciclo menstrual de forma resumida para ser exibido em uma lista de histórico.
 *
 * @param startDate A data de início do ciclo (primeiro dia de menstruação).
 * @param cycleLength A duração total do ciclo em dias (do primeiro dia de uma menstruação até o dia anterior à próxima).
 * @param periodLength A duração do período menstrual em dias.
 */
data class CycleSummary(
    val startDate: LocalDate,
    val cycleLength: Int,
    val periodLength: Int
)