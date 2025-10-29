package com.developersbeeh.medcontrol.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.developersbeeh.medcontrol.databinding.LayoutStateViewBinding

/**
 * Componente reutilizável para exibir diferentes estados da UI de forma padronizada.
 * Gerencia automaticamente a transição entre estados: Loading, Empty, Error e Success.
 * 
 * Uso:
 * ```xml
 * <com.developersbeeh.medcontrol.ui.common.StateView
 *     android:id="@+id/stateView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 * 
 * ```kotlin
 * stateView.setState(UiState.Loading("Carregando..."))
 * stateView.setState(UiState.Success(data))
 * stateView.setState(UiState.Empty("Nenhum item encontrado", "Adicionar", null))
 * stateView.setState(UiState.Error("Erro ao carregar", null, true))
 * ```
 */
class StateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutStateViewBinding
    private var onRetryClickListener: (() -> Unit)? = null
    private var onActionClickListener: (() -> Unit)? = null
    private var contentView: View? = null

    init {
        binding = LayoutStateViewBinding.inflate(LayoutInflater.from(context), this)
        setupListeners()
    }

    private fun setupListeners() {
        // Os listeners serão configurados dinamicamente quando necessário
    }

    /**
     * Define a view de conteúdo que será exibida no estado Success.
     */
    fun setContentView(view: View) {
        contentView = view
    }

    /**
     * Define o listener para o botão de retry.
     */
    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }

    /**
     * Define o listener para o botão de ação no estado vazio.
     */
    fun setOnActionClickListener(listener: () -> Unit) {
        onActionClickListener = listener
    }

    /**
     * Define o estado atual da view.
     */
    fun <T> setState(state: UiState<T>?) {
        when (state) {
            is UiState.Idle -> showIdle()
            is UiState.Loading -> showLoading(state.message)
            is UiState.Success -> showSuccess()
            is UiState.Empty -> showEmpty(state.message, state.actionText)
            is UiState.Error -> showError(state.message, state.canRetry)
            null -> showIdle()
        }
    }

    private fun showIdle() {
        binding.shimmerLayout.isVisible = false
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        contentView?.isVisible = false
    }

    private fun showLoading(message: String?) {
        binding.shimmerLayout.isVisible = true
        binding.shimmerLayout.startShimmer()
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        contentView?.isVisible = false
    }

    private fun showSuccess() {
        binding.shimmerLayout.isVisible = false
        binding.shimmerLayout.stopShimmer()
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        contentView?.isVisible = true
    }

    private fun showEmpty(message: String, actionText: String?) {
        binding.shimmerLayout.isVisible = false
        binding.shimmerLayout.stopShimmer()
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = true
        contentView?.isVisible = false

        // Configurar mensagem vazia se o layout tiver os campos
        // (depende da estrutura do layout_empty_state.xml)
    }

    private fun showError(message: String, canRetry: Boolean) {
        binding.shimmerLayout.isVisible = false
        binding.shimmerLayout.stopShimmer()
        binding.errorStateLayout.root.isVisible = true
        binding.emptyStateLayout.root.isVisible = false
        contentView?.isVisible = false

        // Configurar mensagem de erro e botão retry se o layout tiver os campos
        // (depende da estrutura do layout_error_state.xml)
    }
}

