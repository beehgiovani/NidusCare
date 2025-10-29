package com.developersbeeh.medcontrol.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.LayoutEmptyStateBinding
import com.developersbeeh.medcontrol.databinding.LayoutErrorStateBinding
import com.developersbeeh.medcontrol.databinding.LayoutStateViewBinding
import com.developersbeeh.medcontrol.util.UiState

/**
 * Uma Custom View que gerencia a exibição de diferentes estados da UI (Loading, Success, Error, Empty).
 * O conteúdo principal (ex: RecyclerView) deve ser declarado como filho direto desta view no XML.
 */
class StateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutStateViewBinding
    private val emptyStateBinding: LayoutEmptyStateBinding
    private val errorStateBinding: LayoutErrorStateBinding

    private var contentView: View? = null

    var onRetry: (() -> Unit)? = null

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutStateViewBinding.inflate(inflater, this)
        emptyStateBinding = LayoutEmptyStateBinding.bind(binding.emptyStateLayout.root)
        errorStateBinding = LayoutErrorStateBinding.bind(binding.errorStateLayout.root)

        errorStateBinding.btnRetry.setOnClickListener {
            onRetry?.invoke()
        }
    }

    /**
     * Chamado depois que o layout e seus filhos são inflados.
     * Identifica a view de conteúdo principal.
     */
    override fun onFinishInflate() {
        super.onFinishInflate()

        // ✅ CORREÇÃO: A lógica agora identifica corretamente a view de conteúdo.
        // O layout interno do StateLayout tem 3 filhos (shimmer, error, empty).
        // Qualquer filho adicional é considerado a view de conteúdo principal.
        val internalViewCount = 3
        if (childCount > internalViewCount) {
            // Garante que apenas UMA view de conteúdo foi adicionada.
            if (childCount > internalViewCount + 1) {
                throw IllegalStateException("StateLayout can host only one direct child content view.")
            }
            // A view de conteúdo é a que vem depois das views internas.
            contentView = getChildAt(internalViewCount)
        }
    }

    /**
     * Configura o título, subtítulo e ação do estado vazio.
     */
    fun setEmptyState(
        title: String,
        subtitle: String,
        buttonText: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        emptyStateBinding.tvEmptyTitle.text = title
        emptyStateBinding.tvEmptyMessage.text = subtitle
        if (buttonText != null && onActionClick != null) {
            emptyStateBinding.btnEmptyAction.text = buttonText
            emptyStateBinding.btnEmptyAction.setOnClickListener { onActionClick.invoke() }
            emptyStateBinding.btnEmptyAction.isVisible = true
        } else {
            emptyStateBinding.btnEmptyAction.isVisible = false
        }
    }

    /**
     * Atualiza a UI para refletir o estado fornecido pelo UiState.
     * @param state O estado atual da UI (Loading, Success, Error).
     * @param hasData Uma lambda que determina se o estado Success contém dados para exibir.
     */
    fun <T> setState(state: UiState<T>, hasData: (T) -> Boolean = { true }) {
        hideAll()
        when (state) {
            is UiState.Loading -> {
                binding.shimmerLayout.startShimmer()
                binding.shimmerLayout.isVisible = true
            }
            is UiState.Success -> {
                if (hasData(state.data)) {
                    contentView?.isVisible = true
                } else {
                    binding.emptyStateLayout.root.isVisible = true
                }
            }
            is UiState.Error -> {
                binding.errorStateLayout.root.isVisible = true
                errorStateBinding.tvErrorMessage.text = state.message
            }
        }
    }

    private fun hideAll() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        binding.errorStateLayout.root.isVisible = false
        contentView?.isVisible = false
    }
}