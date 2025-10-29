package com.developersbeeh.medcontrol.util.advanced

import java.text.Normalizer
import java.util.Locale

/**
 * Utilitários avançados para manipulação de texto.
 * Fornece funções para normalização, extração e formatação de strings.
 */
object TextUtils {

    /**
     * Normaliza texto para comparação (remove acentos, converte para minúsculas, remove espaços extras).
     */
    fun normalizeForComparison(text: String): String {
        return removeAccents(text)
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Remove acentos de um texto.
     */
    fun removeAccents(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    /**
     * Extrai o valor numérico de uma string de dosagem.
     * Exemplo: "500mg" -> 500.0, "2,5 ml" -> 2.5
     */
    fun extractDosageValue(dosageString: String): Double? {
        return try {
            val cleanedString = dosageString
                .replace(",", ".")
                .replace(Regex("[^0-9.]"), " ")
                .trim()
            
            val regex = Regex("(\\d+\\.?\\d*)")
            val matchResult = regex.find(cleanedString)
            matchResult?.value?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extrai a unidade de uma string de dosagem.
     * Exemplo: "500mg" -> "mg", "2,5 ml" -> "ml"
     */
    fun extractDosageUnit(dosageString: String): String? {
        return try {
            val cleanedString = dosageString
                .replace(",", ".")
                .trim()
            
            val regex = Regex("\\d+\\.?\\d*\\s*([a-zA-Zµμ]+)")
            val matchResult = regex.find(cleanedString)
            matchResult?.groupValues?.getOrNull(1)?.lowercase(Locale.getDefault())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Separa dosagem em valor e unidade.
     * Retorna um Pair<Double?, String?>
     */
    fun parseDosage(dosageString: String): Pair<Double?, String?> {
        val value = extractDosageValue(dosageString)
        val unit = extractDosageUnit(dosageString)
        return Pair(value, unit)
    }

    /**
     * Formata um valor de dosagem com sua unidade.
     * Exemplo: formatDosage(500.0, "mg") -> "500 mg"
     */
    fun formatDosage(value: Double, unit: String): String {
        val formattedValue = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", value).replace(",", ".")
        }
        return "$formattedValue $unit"
    }

    /**
     * Verifica se dois textos são similares (ignorando acentos, maiúsculas e espaços).
     */
    fun areSimilar(text1: String, text2: String): Boolean {
        return normalizeForComparison(text1) == normalizeForComparison(text2)
    }

    /**
     * Calcula a similaridade entre dois textos usando distância de Levenshtein.
     * Retorna um valor entre 0.0 (completamente diferentes) e 1.0 (idênticos).
     */
    fun calculateSimilarity(text1: String, text2: String): Double {
        val s1 = normalizeForComparison(text1)
        val s2 = normalizeForComparison(text2)
        
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        
        val distance = levenshteinDistance(s1, s2)
        val maxLength = maxOf(s1.length, s2.length)
        
        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Calcula a distância de Levenshtein entre duas strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }

    /**
     * Trunca um texto para um tamanho máximo, adicionando reticências se necessário.
     */
    fun truncate(text: String, maxLength: Int, ellipsis: String = "..."): String {
        if (text.length <= maxLength) return text
        val truncateLength = maxLength - ellipsis.length
        return if (truncateLength > 0) {
            text.substring(0, truncateLength) + ellipsis
        } else {
            text.substring(0, maxLength)
        }
    }

    /**
     * Capitaliza a primeira letra de cada palavra.
     */
    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
    }

    /**
     * Remove espaços extras (múltiplos espaços, tabs, newlines).
     */
    fun removeExtraSpaces(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Valida se um texto contém apenas letras e espaços.
     */
    fun isAlphabeticWithSpaces(text: String): Boolean {
        return text.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$"))
    }

    /**
     * Valida se um texto contém apenas números.
     */
    fun isNumeric(text: String): Boolean {
        return text.matches(Regex("^\\d+$"))
    }

    /**
     * Valida se um texto é um número decimal válido.
     */
    fun isDecimal(text: String): Boolean {
        return text.replace(",", ".").matches(Regex("^\\d+\\.?\\d*$"))
    }

    /**
     * Extrai todos os números de um texto.
     */
    fun extractNumbers(text: String): List<Double> {
        val regex = Regex("\\d+\\.?\\d*")
        return regex.findAll(text.replace(",", "."))
            .mapNotNull { it.value.toDoubleOrNull() }
            .toList()
    }

    /**
     * Formata um número de telefone brasileiro.
     * Exemplo: "11987654321" -> "(11) 98765-4321"
     */
    fun formatPhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when (digits.length) {
            11 -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
            10 -> "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}"
            else -> phone
        }
    }

    /**
     * Formata um CPF brasileiro.
     * Exemplo: "12345678901" -> "123.456.789-01"
     */
    fun formatCPF(cpf: String): String {
        val digits = cpf.filter { it.isDigit() }
        return if (digits.length == 11) {
            "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9)}"
        } else {
            cpf
        }
    }

    /**
     * Valida se um CPF é válido (apenas formato, não verifica dígitos verificadores).
     */
    fun isValidCPFFormat(cpf: String): Boolean {
        val digits = cpf.filter { it.isDigit() }
        return digits.length == 11
    }

    /**
     * Gera iniciais de um nome.
     * Exemplo: "João da Silva" -> "JS"
     */
    fun getInitials(name: String): String {
        return name.split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
    }

    /**
     * Conta o número de palavras em um texto.
     */
    fun wordCount(text: String): Int {
        return text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    /**
     * Verifica se um texto está vazio ou contém apenas espaços em branco.
     */
    fun isBlankOrEmpty(text: String?): Boolean {
        return text.isNullOrBlank()
    }

    /**
     * Retorna o texto ou um valor padrão se estiver vazio.
     */
    fun getOrDefault(text: String?, default: String): String {
        return if (text.isNullOrBlank()) default else text
    }

    /**
     * Mascara informações sensíveis.
     * Exemplo: maskSensitive("12345678901", 3, 2) -> "123******01"
     */
    fun maskSensitive(text: String, visibleStart: Int = 3, visibleEnd: Int = 2, maskChar: Char = '*'): String {
        if (text.length <= visibleStart + visibleEnd) return text
        
        val start = text.substring(0, visibleStart)
        val end = text.substring(text.length - visibleEnd)
        val maskLength = text.length - visibleStart - visibleEnd
        val mask = maskChar.toString().repeat(maskLength)
        
        return start + mask + end
    }

    /**
     * Converte um texto para slug (URL-friendly).
     * Exemplo: "Meu Título Especial!" -> "meu-titulo-especial"
     */
    fun toSlug(text: String): String {
        return removeAccents(text)
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
    }

    /**
     * Verifica se um texto contém uma substring (case-insensitive).
     */
    fun containsIgnoreCase(text: String, substring: String): Boolean {
        return text.lowercase(Locale.getDefault()).contains(substring.lowercase(Locale.getDefault()))
    }

    /**
     * Substitui múltiplas ocorrências de strings.
     */
    fun replaceMultiple(text: String, replacements: Map<String, String>): String {
        var result = text
        replacements.forEach { (old, new) ->
            result = result.replace(old, new)
        }
        return result
    }
}

