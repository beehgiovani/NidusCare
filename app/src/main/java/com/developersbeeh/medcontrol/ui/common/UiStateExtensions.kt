package com.developersbeeh.medcontrol.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extensões para facilitar o uso de UiState em ViewModels.
 */

/**
 * Executa uma operação suspensa e atualiza o LiveData com os estados apropriados.
 * 
 * @param liveData LiveData que será atualizado com os estados
 * @param loadingMessage Mensagem opcional para exibir durante o carregamento
 * @param emptyMessage Mensagem para exibir se o resultado for vazio
 * @param emptyCheck Função para verificar se o resultado está vazio (padrão: lista vazia ou null)
 * @param block Bloco de código suspendido que retorna o resultado
 */
fun <T> ViewModel.launchWithState(
    liveData: MutableLiveData<UiState<T>>,
    loadingMessage: String? = null,
    emptyMessage: String = "Nenhum dado encontrado",
    emptyCheck: (T) -> Boolean = { result ->
        when (result) {
            is List<*> -> result.isEmpty()
            null -> true
            else -> false
        }
    },
    block: suspend CoroutineScope.() -> T
) {
    viewModelScope.launch {
        try {
            liveData.value = UiState.Loading(loadingMessage)
            val result = block()
            
            if (emptyCheck(result)) {
                liveData.value = UiState.Empty(emptyMessage)
            } else {
                liveData.value = UiState.Success(result)
            }
        } catch (e: Exception) {
            liveData.value = UiState.Error(
                message = e.message ?: "Ocorreu um erro inesperado",
                exception = e,
                canRetry = true
            )
        }
    }
}

/**
 * Executa uma operação suspensa que retorna Result<T> e atualiza o LiveData com os estados apropriados.
 * 
 * @param liveData LiveData que será atualizado com os estados
 * @param loadingMessage Mensagem opcional para exibir durante o carregamento
 * @param emptyMessage Mensagem para exibir se o resultado for vazio
 * @param emptyCheck Função para verificar se o resultado está vazio
 * @param block Bloco de código suspendido que retorna Result<T>
 */
fun <T> ViewModel.launchWithResultState(
    liveData: MutableLiveData<UiState<T>>,
    loadingMessage: String? = null,
    emptyMessage: String = "Nenhum dado encontrado",
    emptyCheck: (T) -> Boolean = { result ->
        when (result) {
            is List<*> -> result.isEmpty()
            null -> true
            else -> false
        }
    },
    block: suspend CoroutineScope.() -> Result<T>
) {
    viewModelScope.launch {
        try {
            liveData.value = UiState.Loading(loadingMessage)
            val result = block()
            
            result.onSuccess { data ->
                if (emptyCheck(data)) {
                    liveData.value = UiState.Empty(emptyMessage)
                } else {
                    liveData.value = UiState.Success(data)
                }
            }.onFailure { error ->
                liveData.value = UiState.Error(
                    message = error.message ?: "Ocorreu um erro inesperado",
                    exception = error,
                    canRetry = true
                )
            }
        } catch (e: Exception) {
            liveData.value = UiState.Error(
                message = e.message ?: "Ocorreu um erro inesperado",
                exception = e,
                canRetry = true
            )
        }
    }
}

/**
 * Define o estado como Loading.
 */
fun <T> MutableLiveData<UiState<T>>.setLoading(message: String? = null, progress: Float? = null) {
    value = UiState.Loading(message, progress)
}

/**
 * Define o estado como Success.
 */
fun <T> MutableLiveData<UiState<T>>.setSuccess(data: T) {
    value = UiState.Success(data)
}

/**
 * Define o estado como Empty.
 */
fun <T> MutableLiveData<UiState<T>>.setEmpty(
    message: String = "Nenhum dado encontrado",
    actionText: String? = null,
    actionIcon: Int? = null
) {
    value = UiState.Empty(message, actionText, actionIcon)
}

/**
 * Define o estado como Error.
 */
fun <T> MutableLiveData<UiState<T>>.setError(
    message: String,
    exception: Throwable? = null,
    canRetry: Boolean = true
) {
    value = UiState.Error(message, exception, canRetry)
}

/**
 * Define o estado como Idle.
 */
fun <T> MutableLiveData<UiState<T>>.setIdle() {
    value = UiState.Idle
}

/**
 * Observa um LiveData<UiState<T>> e executa callbacks para cada estado.
 */
inline fun <T> LiveData<UiState<T>>.observeState(
    crossinline onLoading: (String?, Float?) -> Unit = { _, _ -> },
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onEmpty: (String) -> Unit = {},
    crossinline onError: (String, Throwable?) -> Unit = { _, _ -> }
): (UiState<T>) -> Unit {
    return { state ->
        when (state) {
            is UiState.Loading -> onLoading(state.message, state.progress)
            is UiState.Success -> onSuccess(state.data)
            is UiState.Empty -> onEmpty(state.message)
            is UiState.Error -> onError(state.message, state.exception)
            is UiState.Idle -> { /* Nada a fazer */ }
        }
    }
}

/**
 * Converte um Result<T> em UiState<T>.
 */
fun <T> Result<T>.toUiState(
    emptyMessage: String = "Nenhum dado encontrado",
    emptyCheck: (T) -> Boolean = { result ->
        when (result) {
            is List<*> -> result.isEmpty()
            null -> true
            else -> false
        }
    }
): UiState<T> {
    return fold(
        onSuccess = { data ->
            if (emptyCheck(data)) {
                UiState.Empty(emptyMessage)
            } else {
                UiState.Success(data)
            }
        },
        onFailure = { error ->
            UiState.Error(
                message = error.message ?: "Ocorreu um erro inesperado",
                exception = error,
                canRetry = true
            )
        }
    )
}

/**
 * Combina múltiplos UiState em um único estado.
 * Retorna Loading se algum estiver Loading.
 * Retorna Error se algum estiver Error.
 * Retorna Success apenas se todos forem Success.
 */
fun <T> combineUiStates(vararg states: UiState<*>): UiState<T> {
    // Se algum está carregando, retorna loading
    if (states.any { it.isLoading() }) {
        return UiState.Loading()
    }
    
    // Se algum tem erro, retorna o primeiro erro
    states.firstOrNull { it.isError() }?.let { errorState ->
        return UiState.Error(
            message = (errorState as UiState.Error).message,
            exception = errorState.exception,
            canRetry = errorState.canRetry
        )
    }
    
    // Se todos são success ou empty, retorna idle
    return UiState.Idle
}

