package com.developersbeeh.medcontrol.ui.common

/**
 * ✅ NOVO: Sistema padronizado de estados de UI
 * Usado em todo o aplicativo para consistência
 */
sealed class UiState<out T> {
    /**
     * Estado inicial - nenhuma ação foi tomada ainda
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Estado de carregamento
     * @param message Mensagem opcional para exibir durante o carregamento
     */
    data class Loading(
        val message: String? = null,
        val progress: Float? = null
    ) : UiState<Nothing>()
    
    /**
     * Estado de sucesso com dados
     * @param data Os dados carregados com sucesso
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Estado vazio - operação bem-sucedida mas sem dados
     * @param message Mensagem para exibir no estado vazio
     * @param actionText Texto do botão de ação (opcional)
     */
    data class Empty(
        val message: String = "Nenhum dado encontrado",
        val actionText: String? = null,
        val actionIcon: Int? = null
    ) : UiState<Nothing>()
    
    /**
     * Estado de erro
     * @param message Mensagem de erro amigável
     * @param exception Exceção original (opcional)
     * @param canRetry Se true, mostra botão de tentar novamente
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : UiState<Nothing>()
}

/**
 * Extensões úteis para UiState
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error
fun <T> UiState<T>.isEmpty(): Boolean = this is UiState.Empty

fun <T> UiState<T>.getDataOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}

fun <T> UiState<T>.getErrorOrNull(): String? = when (this) {
    is UiState.Error -> message
    else -> null
}

/**
 * Executa um bloco se o estado for Success.
 */
inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) {
        block(data)
    }
    return this
}

/**
 * Executa um bloco se o estado for Error.
 */
inline fun <T> UiState<T>.onError(block: (String, Throwable?) -> Unit): UiState<T> {
    if (this is UiState.Error) {
        block(message, exception)
    }
    return this
}

/**
 * Executa um bloco se o estado for Loading.
 */
inline fun <T> UiState<T>.onLoading(block: (String?, Float?) -> Unit): UiState<T> {
    if (this is UiState.Loading) {
        block(message, progress)
    }
    return this
}

/**
 * Executa um bloco se o estado for Empty.
 */
inline fun <T> UiState<T>.onEmpty(block: (String) -> Unit): UiState<T> {
    if (this is UiState.Empty) {
        block(message)
    }
    return this
}

/**
 * Transforma os dados de um UiState.Success mantendo os outros estados inalterados.
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Loading -> UiState.Loading(message, progress)
        is UiState.Error -> UiState.Error(message, exception, canRetry)
        is UiState.Empty -> UiState.Empty(message, actionText, actionIcon)
        is UiState.Idle -> UiState.Idle
    }
}

