
// Template de Parse Seguro de Datas

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.util.Log

object DateUtils {
    private const val TAG = "DateUtils"
    
    /**
     * Parse seguro de LocalDate
     * Retorna null se parsing falhar
     */
    fun parseLocalDateOrNull(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "Erro ao fazer parse de data: $dateString", e)
            null
        }
    }
    
    /**
     * Parse seguro de LocalDate com fallback
     * Retorna data padrão se parsing falhar
     */
    fun parseLocalDateOrDefault(
        dateString: String?,
        default: LocalDate = LocalDate.now()
    ): LocalDate {
        return parseLocalDateOrNull(dateString) ?: default
    }
    
    /**
     * Parse seguro de LocalDateTime
     * Retorna null se parsing falhar
     */
    fun parseLocalDateTimeOrNull(dateTimeString: String?): LocalDateTime? {
        if (dateTimeString.isNullOrBlank()) return null
        
        return try {
            LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "Erro ao fazer parse de data/hora: $dateTimeString", e)
            null
        }
    }
    
    /**
     * Parse seguro de LocalDateTime com fallback
     * Retorna data/hora padrão se parsing falhar
     */
    fun parseLocalDateTimeOrDefault(
        dateTimeString: String?,
        default: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime {
        return parseLocalDateTimeOrNull(dateTimeString) ?: default
    }
}

