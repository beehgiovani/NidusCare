// src/main/java/com/developersbeeh/medcontrol/ui/education/EducationViewModel.kt

package com.developersbeeh.medcontrol.ui.education

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developersbeeh.medcontrol.data.model.ArtigoEducativo
import com.developersbeeh.medcontrol.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class EducationUiState(
    val artigos: List<ArtigoEducativo> = emptyList(),
    val categorias: List<String> = emptyList(),
    val filtroAtual: String? = null
)

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val repository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<EducationUiState>()
    val uiState: LiveData<EducationUiState> = _uiState

    private val allArtigos: List<ArtigoEducativo>

    init {
        allArtigos = repository.getArtigos()
        val categorias = listOf("Todos") + allArtigos.map { it.categoria }.distinct()
        _uiState.value = EducationUiState(artigos = allArtigos, categorias = categorias)
    }

    fun applyFilter(categoria: String?) {
        val filtro = if (categoria == "Todos") null else categoria
        val filteredList = if (filtro == null) {
            allArtigos
        } else {
            allArtigos.filter { it.categoria == filtro }
        }
        _uiState.value = _uiState.value?.copy(
            artigos = filteredList,
            filtroAtual = filtro
        )
    }
}