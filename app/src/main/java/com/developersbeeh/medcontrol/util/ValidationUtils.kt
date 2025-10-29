
package com.developersbeeh.medcontrol.util

import com.developersbeeh.medcontrol.data.model.Medicamento
import java.time.LocalDate

object ValidationUtils {
    
    /**
     * Valida se medicamento está configurado corretamente
     */
    fun validateMedicamento(med: Medicamento): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validar nome
        if (med.nome.isBlank()) {
            errors.add("Nome do medicamento é obrigatório")
        }
        
        // Validar dosagem
        if (med.dosagem.isBlank()) {
            errors.add("Dosagem é obrigatória")
        }
        
        // Validar horários para uso não esporádico
        if (!med.isUsoEsporadico && med.horarios.isEmpty()) {
            errors.add("Horários são obrigatórios para uso regular")
        }
        
        // Validar data de início
        if (med.dataInicioTratamento.isAfter(LocalDate.now().plusDays(1))) {
            errors.add("Data de início não pode ser muito no futuro")
        }
        
        // Validar duração para uso não contínuo
        if (!med.isUsoContinuo && med.duracaoDias <= 0) {
            errors.add("Duração do tratamento é obrigatória para uso não contínuo")
        }
        
        // Validar estoque
        if (med.estoqueAtualTotal < 0) {
            errors.add("Estoque não pode ser negativo")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Valida se há estoque suficiente para administrar dose
     */
    fun validateEstoqueSuficiente(
        estoqueAtual: Double,
        quantidadeAdministrar: Double
    ): Boolean {
        return estoqueAtual >= quantidadeAdministrar
    }
    
    /**
     * Valida divisão por zero
     */
    fun safeDivide(numerator: Double, denominator: Double, default: Double = 0.0): Double {
        return if (denominator != 0.0) {
            numerator / denominator
        } else {
            default
        }
    }
    
    /**
     * Calcula percentual com segurança
     */
    fun safePercentage(part: Int, total: Int): Double {
        return if (total > 0) {
            (part.toDouble() / total) * 100
        } else {
            0.0
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}
