package com.developersbeeh.medcontrol.ui.analysis

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentAnalysisResultBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AnalysisResultFragment : Fragment() {

    private var _binding: FragmentAnalysisResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalysisResultViewModel by viewModels()
    private val args: AnalysisResultFragmentArgs by navArgs()

    private var tempPdfFile: File? = null
    private val savePdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { destinationUri ->
                copyFileToUri(tempPdfFile, destinationUri)
            }
        }
    }

    private lateinit var analysisAdapter: AnalysisSectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        if (viewModel.uiState.value == null) {
            viewModel.fetchAnalysis(args.prompt ?: "", args.analysisResult, args.dependentId)
        }
    }

    private fun setupRecyclerView() {
        analysisAdapter = AnalysisSectionAdapter()
        binding.recyclerViewAnalysis.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = analysisAdapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonRetry.setOnClickListener {
            if (args.analysisResult == null) {
                viewModel.fetchAnalysis(args.prompt ?: "", null, args.dependentId)
            }
        }

        binding.buttonShare.setOnClickListener {
            val state = viewModel.uiState.value
            if (state is AnalysisUiState.Success) {
                val shareText = viewModel.generateShareableText(state.parsedAnalysis, args.dependentName)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }

        binding.buttonSavePdf.setOnClickListener {
            try {
                val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_logo)
                viewModel.generateAnalysisPdf(args.dependentId, logoBitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar a imagem do logo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AnalysisUiState.Loading -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                    binding.successLayout.visibility = View.GONE
                }
                is AnalysisUiState.Success -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    binding.successLayout.visibility = View.VISIBLE

                    val analysis = state.parsedAnalysis
                    val sections = listOf(
                        AnalysisSection("Correlações", analysis.correlations),
                        AnalysisSection("Interações Medicamentosas", analysis.interactions),
                        AnalysisSection("Efeitos Colaterais", analysis.sideEffects),
                        AnalysisSection("Observações Adicionais", analysis.observations),
                        AnalysisSection("Nível de Urgência", analysis.urgencyLevel),
                        AnalysisSection("Pontos para Discussão Médica", analysis.discussionPoints)
                    ).filter { it.content.isNotBlank() && it.content != "Dados insuficientes para esta análise." }

                    analysisAdapter.submitList(sections)

                }
                is AnalysisUiState.Error -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.successLayout.visibility = View.GONE
                    binding.textViewError.text = state.message
                }
            }
        }

        // ✅ BLOCO DE CÓDIGO REMOVIDO
        // O observer para viewModel.saveStatus foi removido daqui.

        viewModel.pdfGenerationState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is PdfGenerationState.Loading -> {
                        Toast.makeText(context, "Gerando PDF...", Toast.LENGTH_SHORT).show()
                    }
                    is PdfGenerationState.Success -> {
                        tempPdfFile = state.file
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"))
                        val fileName = "Analise_${args.dependentName}_${timestamp}.pdf"

                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_TITLE, fileName)
                        }
                        savePdfLauncher.launch(intent)
                    }
                    is PdfGenerationState.Error -> {
                        Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    is PdfGenerationState.Idle -> {
                        // Não faz nada, o estado inicial.
                    }
                }
            }
        }
    }

    private fun copyFileToUri(sourceFile: File?, destinationUri: Uri) {
        if (sourceFile == null) return
        try {
            requireContext().contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "PDF salvo com sucesso!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Falha ao salvar o arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            sourceFile.delete()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}