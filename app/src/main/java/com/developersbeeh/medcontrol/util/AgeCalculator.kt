// src/main/java/com/developersbeeh/medcontrol/util/AgeCalculator.kt
package com.developersbeeh.medcontrol.util

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object AgeCalculator {

    fun calculateAge(dataNascimento: String): Int? {
        if (dataNascimento.isBlank()) return null

        // Lista de formatos de data que o app pode encontrar
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
        )

        for (formatter in formatters) {
            try {
                val birthDate = LocalDate.parse(dataNascimento, formatter)
                return Period.between(birthDate, LocalDate.now()).years
            } catch (e: DateTimeParseException) {
                // Tenta o próximo formato se o atual falhar
            }
        }
        // Retorna nulo se nenhum formato for válido
        return null
    }
}