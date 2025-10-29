package com.developersbeeh.medcontrol.util.advanced

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Utilitários avançados para manipulação de datas e horários.
 * Fornece funções auxiliares para validação, formatação e cálculos.
 */
object DateTimeUtils {

    // Formatadores comuns
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    private val ISO_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME
    private val ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    private val DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Verifica se uma data está no futuro.
     */
    fun isInFuture(date: LocalDate): Boolean {
        return date.isAfter(LocalDate.now())
    }

    /**
     * Verifica se uma data está no passado.
     */
    fun isInPast(date: LocalDate): Boolean {
        return date.isBefore(LocalDate.now())
    }

    /**
     * Verifica se uma data está muito no futuro (mais que o número especificado de dias).
     */
    fun isTooFarInFuture(date: LocalDate, maxDays: Int = 365): Boolean {
        val maxDate = LocalDate.now().plusDays(maxDays.toLong())
        return date.isAfter(maxDate)
    }

    /**
     * Verifica se uma data está muito no passado (mais que o número especificado de dias).
     */
    fun isTooFarInPast(date: LocalDate, maxDays: Int = 365): Boolean {
        val minDate = LocalDate.now().minusDays(maxDays.toLong())
        return date.isBefore(minDate)
    }

    /**
     * Verifica se um LocalDateTime está no futuro.
     */
    fun isInFuture(dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(LocalDateTime.now())
    }

    /**
     * Verifica se um LocalDateTime está no passado.
     */
    fun isInPast(dateTime: LocalDateTime): Boolean {
        return dateTime.isBefore(LocalDateTime.now())
    }

    /**
     * Formata uma data para exibição (dd/MM/yyyy).
     */
    fun formatForDisplay(date: LocalDate): String {
        return date.format(DISPLAY_DATE_FORMATTER)
    }

    /**
     * Formata um horário para exibição (HH:mm).
     */
    fun formatForDisplay(time: LocalTime): String {
        return time.format(DISPLAY_TIME_FORMATTER)
    }

    /**
     * Formata uma data/hora para exibição (dd/MM/yyyy HH:mm).
     */
    fun formatForDisplay(dateTime: LocalDateTime): String {
        return dateTime.format(DISPLAY_DATETIME_FORMATTER)
    }

    /**
     * Formata uma data para formato ISO (yyyy-MM-dd).
     */
    fun formatToISO(date: LocalDate): String {
        return date.format(ISO_DATE_FORMATTER)
    }

    /**
     * Formata um horário para formato ISO (HH:mm:ss).
     */
    fun formatToISO(time: LocalTime): String {
        return time.format(ISO_TIME_FORMATTER)
    }

    /**
     * Formata uma data/hora para formato ISO (yyyy-MM-ddTHH:mm:ss).
     */
    fun formatToISO(dateTime: LocalDateTime): String {
        return dateTime.format(ISO_DATETIME_FORMATTER)
    }

    /**
     * Tenta parsear uma data de múltiplos formatos possíveis.
     * Retorna null se não conseguir parsear.
     */
    fun parseFlexible(dateString: String): LocalDate? {
        if (dateString.isBlank()) return null

        val formats = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,                    // yyyy-MM-dd
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),           // dd/MM/yyyy
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),           // dd-MM-yyyy
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),           // yyyy/MM/dd
            DateTimeFormatter.ofPattern("dd.MM.yyyy")            // dd.MM.yyyy
        )

        for (formatter in formats) {
            try {
                return LocalDate.parse(dateString.trim(), formatter)
            } catch (e: DateTimeParseException) {
                // Tenta o próximo formato
            }
        }

        return null
    }

    /**
     * Tenta parsear um horário de múltiplos formatos possíveis.
     * Retorna null se não conseguir parsear.
     */
    fun parseTimeFlexible(timeString: String): LocalTime? {
        if (timeString.isBlank()) return null

        val formats = listOf(
            DateTimeFormatter.ISO_LOCAL_TIME,                    // HH:mm:ss
            DateTimeFormatter.ofPattern("HH:mm"),                // HH:mm
            DateTimeFormatter.ofPattern("HH:mm:ss"),             // HH:mm:ss
            DateTimeFormatter.ofPattern("H:mm")                  // H:mm (sem zero à esquerda)
        )

        for (formatter in formats) {
            try {
                return LocalTime.parse(timeString.trim(), formatter)
            } catch (e: DateTimeParseException) {
                // Tenta o próximo formato
            }
        }

        return null
    }

    /**
     * Calcula o número de dias entre duas datas.
     */
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    /**
     * Calcula o número de horas entre dois LocalDateTime.
     */
    fun hoursBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.HOURS.between(start, end)
    }

    /**
     * Calcula o número de minutos entre dois LocalDateTime.
     */
    fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(start, end)
    }

    /**
     * Retorna uma descrição relativa do tempo (ex: "há 2 dias", "em 3 horas").
     */
    fun getRelativeTimeDescription(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(now, dateTime)
        val absDuration = duration.abs()

        val isPast = duration.isNegative
        val prefix = if (isPast) "há" else "em"

        return when {
            absDuration.toMinutes() < 1 -> "agora"
            absDuration.toMinutes() < 60 -> {
                val minutes = absDuration.toMinutes()
                "$prefix ${minutes} minuto${if (minutes > 1) "s" else ""}"
            }
            absDuration.toHours() < 24 -> {
                val hours = absDuration.toHours()
                "$prefix ${hours} hora${if (hours > 1) "s" else ""}"
            }
            absDuration.toDays() < 7 -> {
                val days = absDuration.toDays()
                "$prefix ${days} dia${if (days > 1) "s" else ""}"
            }
            absDuration.toDays() < 30 -> {
                val weeks = absDuration.toDays() / 7
                "$prefix ${weeks} semana${if (weeks > 1) "s" else ""}"
            }
            absDuration.toDays() < 365 -> {
                val months = absDuration.toDays() / 30
                "$prefix ${months} mês${if (months > 1) "es" else ""}"
            }
            else -> {
                val years = absDuration.toDays() / 365
                "$prefix ${years} ano${if (years > 1) "s" else ""}"
            }
        }
    }

    /**
     * Retorna uma descrição relativa da data (ex: "hoje", "amanhã", "ontem").
     */
    fun getRelativeDateDescription(date: LocalDate): String {
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(today, date)

        return when (daysDiff.toInt()) {
            0 -> "hoje"
            1 -> "amanhã"
            -1 -> "ontem"
            in 2..6 -> "em $daysDiff dias"
            in -6..-2 -> "há ${-daysDiff} dias"
            else -> formatForDisplay(date)
        }
    }

    /**
     * Verifica se uma data está dentro de um período.
     */
    fun isInRange(date: LocalDate, start: LocalDate, end: LocalDate): Boolean {
        return !date.isBefore(start) && !date.isAfter(end)
    }

    /**
     * Verifica se um horário está dentro de um intervalo.
     */
    fun isInRange(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return !time.isBefore(start) && !time.isAfter(end)
    }

    /**
     * Retorna o início do dia para uma data.
     */
    fun startOfDay(date: LocalDate): LocalDateTime {
        return date.atStartOfDay()
    }

    /**
     * Retorna o fim do dia para uma data.
     */
    fun endOfDay(date: LocalDate): LocalDateTime {
        return date.atTime(23, 59, 59)
    }

    /**
     * Verifica se dois períodos se sobrepõem.
     */
    fun periodsOverlap(
        start1: LocalDateTime,
        end1: LocalDateTime,
        start2: LocalDateTime,
        end2: LocalDateTime
    ): Boolean {
        return start1.isBefore(end2) && end1.isAfter(start2)
    }

    /**
     * Adiciona dias úteis a uma data (ignora finais de semana).
     */
    fun addBusinessDays(date: LocalDate, days: Int): LocalDate {
        var result = date
        var remainingDays = days

        while (remainingDays > 0) {
            result = result.plusDays(1)
            // Segunda = 1, Domingo = 7
            if (result.dayOfWeek.value <= 5) {
                remainingDays--
            }
        }

        return result
    }

    /**
     * Verifica se uma data é dia útil (segunda a sexta).
     */
    fun isBusinessDay(date: LocalDate): Boolean {
        return date.dayOfWeek.value <= 5
    }

    /**
     * Verifica se uma data é final de semana.
     */
    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek.value >= 6
    }

    /**
     * Retorna o próximo dia útil a partir de uma data.
     */
    fun nextBusinessDay(date: LocalDate): LocalDate {
        var result = date.plusDays(1)
        while (!isBusinessDay(result)) {
            result = result.plusDays(1)
        }
        return result
    }

    /**
     * Calcula a idade em anos a partir de uma data de nascimento.
     */
    fun calculateAge(birthDate: LocalDate): Int {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()
    }

    /**
     * Verifica se um horário está dentro do horário comercial típico (8h-18h).
     */
    fun isBusinessHours(time: LocalTime): Boolean {
        return time.hour in 8..17
    }

    /**
     * Arredonda um LocalDateTime para o minuto mais próximo.
     */
    fun roundToNearestMinute(dateTime: LocalDateTime): LocalDateTime {
        return dateTime.withSecond(0).withNano(0)
    }

    /**
     * Arredonda um LocalDateTime para a hora mais próxima.
     */
    fun roundToNearestHour(dateTime: LocalDateTime): LocalDateTime {
        return if (dateTime.minute >= 30) {
            dateTime.plusHours(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            dateTime.withMinute(0).withSecond(0).withNano(0)
        }
    }
}

