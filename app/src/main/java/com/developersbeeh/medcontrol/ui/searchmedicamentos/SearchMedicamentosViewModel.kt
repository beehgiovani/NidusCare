package com.developersbeeh.medcontrol.ui.searchmedicamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.model.MedicamentoDatabase
import com.developersbeeh.medcontrol.data.repository.RealtimeDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// AQUI ESTÁ A DEFINIÇÃO QUE FALTAVA
enum class SearchType { NOME, PRINCIPIO_ATIVO, CLASSE_TERAPEUTICA }

data class SearchMedicamentosUiState(
    val isLoading: Boolean = false,
    val medicamentos: List<MedicamentoDatabase> = emptyList(),
    val searchType: SearchType = SearchType.NOME
)

@HiltViewModel
class SearchMedicamentosViewModel @Inject constructor(
    private val realtimeDatabaseRepository: RealtimeDatabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchMedicamentosUiState())
    val uiState: StateFlow<SearchMedicamentosUiState> = _uiState

    fun setSearchType(type: SearchType) {
        _uiState.value = _uiState.value.copy(searchType = type)
    }

    fun searchMedicamentos(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Lógica restaurada para buscar no campo correto
            val orderByField = getOrderByKey(_uiState.value.searchType)
            val searchTerm = query.lowercase().trim()

            realtimeDatabaseRepository.searchMedicamentos(query, orderByField)
                .collectLatest { candidates ->
                    // Lógica de filtro restaurada para considerar o tipo de busca
                    val filteredList = candidates.filter { med ->
                        when (_uiState.value.searchType) {
                            SearchType.NOME -> med.NOME_PRODUTO.trim().lowercase().contains(searchTerm)
                            SearchType.PRINCIPIO_ATIVO -> med.PRINCIPIO_ATIVO.trim().lowercase().contains(searchTerm)
                            SearchType.CLASSE_TERAPEUTICA -> med.CLASSE_TERAPEUTICA.trim().lowercase().contains(searchTerm)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        medicamentos = filteredList
                    )
                }
        }
    }

    private fun getOrderByKey(type: SearchType): String {
        return when (type) {
            SearchType.NOME -> "NOME_PRODUTO"
            SearchType.PRINCIPIO_ATIVO -> "PRINCIPIO_ATIVO"
            SearchType.CLASSE_TERAPEUTICA -> "CLASSE_TERAPEUTICA"
        }
    }
}