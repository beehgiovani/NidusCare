package com.developersbeeh.medcontrol.ui.analysis

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository
import com.developersbeeh.medcontrol.data.repository.UserRepository
import com.developersbeeh.medcontrol.util.AnalysisPdfGenerator
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ParsedAnalysis(
    val correlations: String = "Nenhuma informação disponível.",
    val interactions: String = "Nenhuma informação disponível.",
    val sideEffects: String = "Nenhuma informação disponível.",
    val urgencyLevel: String = "Nenhuma informação disponível.",
    val discussionPoints: String = "Nenhuma informação disponível.",
    val observations: String = "Nenhuma informação disponível.",
    val disclaimer: String = ""
)

sealed class PdfGenerationState {
    object Idle : PdfGenerationState()
    object Loading : PdfGenerationState()
    data class Success(val file: File) : PdfGenerationState()
    data class Error(val message: String) : PdfGenerationState()
}

sealed class AnalysisUiState {
    object Loading : AnalysisUiState()
    data class Success(val parsedAnalysis: ParsedAnalysis) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val userRepository: UserRepository,
    private val analysisPdfGenerator: AnalysisPdfGenerator
) : ViewModel() {

    private val _uiState = MutableLiveData<AnalysisUiState>()
    val uiState: LiveData<AnalysisUiState> = _uiState

    private val _pdfGenerationState = MutableLiveData<Event<PdfGenerationState>>(Event(PdfGenerationState.Idle))
    val pdfGenerationState: LiveData<Event<PdfGenerationState>> = _pdfGenerationState

    fun fetchAnalysis(prompt: String, existingResult: String?, dependentId: String) {
        _uiState.value = AnalysisUiState.Loading

        if (existingResult != null) {
            val parsedData = parseAnalysisText(existingResult)
            _uiState.postValue(AnalysisUiState.Success(parsedData))
        } else {
            _uiState.postValue(AnalysisUiState.Error("Nenhum resultado de análise para exibir."))
        }
    }

    fun generateAnalysisPdf(dependentId: String, logoBitmap: Bitmap) {
        val currentState = _uiState.value
        if (currentState !is AnalysisUiState.Success) {
            _pdfGenerationState.value = Event(PdfGenerationState.Error("Análise ainda não foi carregada."))
            return
        }

        _pdfGenerationState.value = Event(PdfGenerationState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cuidadorId = userRepository.getCurrentUser()?.uid
                val dependente = firestoreRepository.getDependente(dependentId)
                val cuidadorProfileResult = cuidadorId?.let { userRepository.getUserProfile(it) }

                if (dependente == null || cuidadorProfileResult == null || cuidadorProfileResult.isFailure) {
                    _pdfGenerationState.postValue(Event(PdfGenerationState.Error("Não foi possível obter os dados do usuário.")))
                    return@launch
                }

                val cuidador = cuidadorProfileResult.getOrThrow()

                val file = analysisPdfGenerator.createReport(
                    analysis = currentState.parsedAnalysis,
                    dependente = dependente,
                    cuidador = cuidador,
                    logoBitmap = logoBitmap
                )
                _pdfGenerationState.postValue(Event(PdfGenerationState.Success(file)))

            } catch (e: Exception) {
                _pdfGenerationState.postValue(Event(PdfGenerationState.Error("Falha ao criar o arquivo PDF: ${e.message}")))
            }
        }
    }

    fun generateShareableText(analysis: ParsedAnalysis, dependentName: String): String {
        return """
        Análise de Saúde para: $dependentName
        
        **Correlações:**
        ${analysis.correlations}
        
        **Interações Medicamentosas:**
        ${analysis.interactions}
        
        **Efeitos Colaterais:**
        ${analysis.sideEffects}
        
        **Nível de Urgência:**
        ${analysis.urgencyLevel}
        
        **Pontos para Discussão Médica:**
        ${analysis.discussionPoints}
        
        **Observações Adicionais:**
        ${analysis.observations}
        
        **Aviso Importante:**
        ${analysis.disclaimer}
        
        - Gerado por NidusCare App
        """.trimIndent()
    }

    private fun parseAnalysisText(text: String): ParsedAnalysis {
        val sections = mapOf(
            "Correlações" to "",
            "Interações Medicamentosas" to "",
            "Efeitos Colaterais" to "",
            "Nível de Urgência" to "",
            "Pontos para Discussão Médica" to "",
            "Observações Adicionais" to ""
        ).toMutableMap()

        val regex = Regex("\\*\\*(.*?):\\*\\*\\s*")
        val matches = regex.findAll(text).toList()

        for (i in matches.indices) {
            val title = matches[i].groupValues[1]
            val startIndex = matches[i].range.last + 1

            val endIndex = if (i + 1 < matches.size) {
                matches[i + 1].range.first
            } else {
                text.length
            }

            val content = text.substring(startIndex, endIndex).trim()
            if (sections.containsKey(title)) {
                sections[title] = content.ifBlank { "Dados insuficientes para esta análise." }
            }
        }

        val disclaimer = text.substringAfter("Importante:**", "").trim()

        return ParsedAnalysis(
            correlations = sections["Correlações"] ?: "Nenhuma informação disponível.",
            interactions = sections["Interações Medicamentosas"] ?: "Nenhuma informação disponível.",
            sideEffects = sections["Efeitos Colaterais"] ?: "Nenhuma informação disponível.",
            urgencyLevel = sections["Nível de Urgência"] ?: "Nenhuma informação disponível.",
            discussionPoints = sections["Pontos para Discussão Médica"] ?: "Nenhuma informação disponível.",
            observations = sections["Observações Adicionais"] ?: "Nenhuma informação disponível.",
            disclaimer = disclaimer
        )
    }
}