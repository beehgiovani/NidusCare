package com.developersbeeh.medcontrol.ui.common

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.developersbeeh.medcontrol.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * ✅ NOVO: Gerenciador de estados de UI padronizado
 * Facilita a exibição de loading, empty, error e success states
 */
class StateManager(
    private val contentView: View,
    private val loadingView: View? = null,
    private val emptyView: View? = null,
    private val errorView: View? = null
) {

    /**
     * Exibe o estado de carregamento
     */
    fun showLoading(message: String? = null) {
        contentView.isVisible = false
        emptyView?.isVisible = false
        errorView?.isVisible = false
        loadingView?.isVisible = true
        
        loadingView?.let { view ->
            view.findViewById<TextView>(R.id.textViewLoadingMessage)?.text = 
                message ?: "Carregando..."
        }
    }

    /**
     * Exibe o conteúdo principal
     */
    fun showContent() {
        contentView.isVisible = true
        loadingView?.isVisible = false
        emptyView?.isVisible = false
        errorView?.isVisible = false
    }

    /**
     * Exibe o estado vazio
     */
    fun showEmpty(
        title: String = "Nenhum dado encontrado",
        message: String = "Comece adicionando seus primeiros registros",
        actionText: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        contentView.isVisible = false
        loadingView?.isVisible = false
        errorView?.isVisible = false
        emptyView?.isVisible = true
        
        emptyView?.let { view ->
            view.findViewById<TextView>(R.id.tvEmptyTitle)?.text = title
            view.findViewById<TextView>(R.id.tvEmptyMessage)?.text = message
            
            val actionButton = view.findViewById<MaterialButton>(R.id.btnEmptyAction)
            if (actionText != null && onActionClick != null) {
                actionButton?.isVisible = true
                actionButton?.text = actionText
                actionButton?.setOnClickListener { onActionClick() }
            } else {
                actionButton?.isVisible = false
            }
        }
    }

    /**
     * Exibe o estado de erro
     */
    fun showError(
        message: String,
        canRetry: Boolean = true,
        onRetryClick: (() -> Unit)? = null
    ) {
        contentView.isVisible = false
        loadingView?.isVisible = false
        emptyView?.isVisible = false
        errorView?.isVisible = true
        
        errorView?.let { view ->
            view.findViewById<TextView>(R.id.tvErrorMessage)?.text = message
            
            val retryButton = view.findViewById<MaterialButton>(R.id.btnRetry)
            if (canRetry && onRetryClick != null) {
                retryButton?.isVisible = true
                retryButton?.setOnClickListener { onRetryClick() }
            } else {
                retryButton?.isVisible = false
            }
        }
    }

    /**
     * Manipula automaticamente um UiState
     */
    fun <T> handleState(
        state: UiState<T>,
        onSuccess: (T) -> Unit,
        emptyConfig: EmptyConfig? = null,
        errorConfig: ErrorConfig? = null
    ) {
        when (state) {
            is UiState.Idle -> {
                // Não faz nada, mantém estado atual
            }
            is UiState.Loading -> {
                showLoading(state.message)
            }
            is UiState.Success -> {
                showContent()
                onSuccess(state.data)
            }
            is UiState.Empty -> {
                showEmpty(
                    title = emptyConfig?.title ?: "Nenhum dado encontrado",
                    message = state.message,
                    actionText = state.actionText ?: emptyConfig?.actionText,
                    onActionClick = emptyConfig?.onActionClick
                )
            }
            is UiState.Error -> {
                showError(
                    message = state.message,
                    canRetry = state.canRetry,
                    onRetryClick = errorConfig?.onRetryClick
                )
            }
        }
    }

    data class EmptyConfig(
        val title: String? = null,
        val actionText: String? = null,
        val onActionClick: (() -> Unit)? = null
    )

    data class ErrorConfig(
        val onRetryClick: (() -> Unit)? = null
    )
}

/**
 * Extensão para criar StateManager facilmente
 */
fun ViewGroup.createStateManager(
    contentViewId: Int,
    loadingViewId: Int? = null,
    emptyViewId: Int? = null,
    errorViewId: Int? = null
): StateManager {
    return StateManager(
        contentView = findViewById(contentViewId),
        loadingView = loadingViewId?.let { findViewById(it) },
        emptyView = emptyViewId?.let { findViewById(it) },
        errorView = errorViewId?.let { findViewById(it) }
    )
}

