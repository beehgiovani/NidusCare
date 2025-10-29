package com.developersbeeh.medcontrol.validation

/**
 * Representa o resultado de uma validação, podendo ser sucesso ou erro com lista de problemas.
 */
sealed class ValidationResult {
    /**
     * Validação bem-sucedida sem erros.
     */
    object Success : ValidationResult()

    /**
     * Validação falhou com uma lista de erros.
     * @param errors Lista de erros encontrados durante a validação
     */
    data class Error(val errors: List<ValidationError>) : ValidationResult()

    /**
     * Verifica se a validação foi bem-sucedida.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Verifica se a validação falhou.
     */
    fun isError(): Boolean = this is Error

    /**
     * Obtém os erros se houver, ou lista vazia se for sucesso.
     */
    fun errorList(): List<ValidationError> = when (this) {
        is Error -> errors
        is Success -> emptyList()
    }

    /**
     * Obtém apenas os erros críticos.
     */
    fun getCriticalErrors(): List<ValidationError> = errorList().filter { it.severity == Severity.CRITICAL }

    /**
     * Obtém apenas os erros (não warnings).
     */
    fun getErrorsOnly(): List<ValidationError> = errorList().filter { it.severity == Severity.ERROR || it.severity == Severity.CRITICAL }

    /**
     * Obtém apenas os warnings.
     */
    fun getWarnings(): List<ValidationError> = errorList().filter { it.severity == Severity.WARNING }

    /**
     * Verifica se há erros críticos.
     */
    fun hasCriticalErrors(): Boolean = getCriticalErrors().isNotEmpty()
}

/**
 * Representa um erro de validação específico.
 * @param field Campo que falhou na validação
 * @param message Mensagem descritiva do erro
 * @param severity Severidade do erro (WARNING, ERROR, CRITICAL)
 */
data class ValidationError(
    val field: String,
    val message: String,
    val severity: Severity = Severity.ERROR
) {
    /**
     * Retorna uma representação em string formatada do erro.
     */
    override fun toString(): String = "[$severity] $field: $message"
}

/**
 * Níveis de severidade para erros de validação.
 */
enum class Severity {
    /**
     * Aviso - não impede a operação, mas requer atenção do usuário.
     */
    WARNING,

    /**
     * Erro - impede a operação e requer correção.
     */
    ERROR,

    /**
     * Crítico - erro grave que pode causar perda de dados ou comportamento inesperado.
     */
    CRITICAL
}

/**
 * Builder para construir resultados de validação de forma fluente.
 */
class ValidationResultBuilder {
    private val errors = mutableListOf<ValidationError>()

    /**
     * Adiciona um erro de validação.
     */
    fun addError(field: String, message: String, severity: Severity = Severity.ERROR): ValidationResultBuilder {
        errors.add(ValidationError(field, message, severity))
        return this
    }

    /**
     * Adiciona um erro se a condição for verdadeira.
     */
    fun addErrorIf(condition: Boolean, field: String, message: String, severity: Severity = Severity.ERROR): ValidationResultBuilder {
        if (condition) {
            errors.add(ValidationError(field, message, severity))
        }
        return this
    }

    /**
     * Adiciona um warning.
     */
    fun addWarning(field: String, message: String): ValidationResultBuilder {
        errors.add(ValidationError(field, message, Severity.WARNING))
        return this
    }

    /**
     * Adiciona um warning se a condição for verdadeira.
     */
    fun addWarningIf(condition: Boolean, field: String, message: String): ValidationResultBuilder {
        if (condition) {
            errors.add(ValidationError(field, message, Severity.WARNING))
        }
        return this
    }

    /**
     * Constrói o resultado final da validação.
     */
    fun build(): ValidationResult {
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

