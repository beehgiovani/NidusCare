// src/main/java/com/developersbeeh/medcontrol/ui/documents/DocumentViewerFragment.kt
package com.developersbeeh.medcontrol.ui.documents

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.request.ImageRequest
import com.developersbeeh.medcontrol.databinding.FragmentDocumentViewerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class DocumentViewerFragment : Fragment() {

    private var _binding: FragmentDocumentViewerBinding? = null
    private val binding get() = _binding!!

    private val args: DocumentViewerFragmentArgs by navArgs()

    private val TAG = "DocumentViewerFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = args.documentoTitulo
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        loadFileSafely()
    }

    private fun loadFileSafely() {
        val urlString = args.documentoUrl.trim()

        if (urlString.isEmpty()) {
            showLoadError("URL inválida ou vazia.")
            return
        }

        binding.progressBar.isVisible = true
        binding.pdfView.isVisible = false
        binding.photoView.isVisible = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withTimeout(20000L) { // Timeout de 20 segundos
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val contentType = connection.contentType ?: "application/octet-stream"
                        val inputStream = connection.inputStream

                        when {
                            contentType.contains("application/pdf") -> {
                                loadPdfFromStream(inputStream)
                            }
                            contentType.startsWith("image/") -> {
                                loadImageWithCoil(urlString)
                            }
                            else -> {
                                withContext(Dispatchers.Main) {
                                    showLoadError("Tipo de arquivo não suportado: $contentType")
                                }
                            }
                        }
                    } else {
                        throw Exception("Falha na conexão (HTTP ${connection.responseCode})")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout ao carregar documento: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoadError("Tempo limite excedido ao carregar o documento.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar documento: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoadError("Erro ao abrir o documento.")
                }
            }
        }
    }

    private suspend fun loadPdfFromStream(inputStream: InputStream) {
        withContext(Dispatchers.Main) {
            binding.pdfView.fromStream(inputStream)
                .enableAntialiasing(true) // Melhora a qualidade da renderização
                .spacing(10) // Espaçamento entre as páginas
                .onLoad {
                    binding.progressBar.isVisible = false
                    binding.pdfView.isVisible = true
                }
                .onError { throwable ->
                    Log.e(TAG, "Erro ao renderizar PDF: ${throwable.message}", throwable)
                    showLoadError("Falha ao renderizar o PDF.")
                }
                .onPageError { page, t ->
                    Log.e(TAG, "Erro na página $page: ${t.message}", t)
                    showLoadError("Erro ao carregar a página $page do PDF.")
                }
                .load()
        }
    }

    private suspend fun loadImageWithCoil(url: String) {
        withContext(Dispatchers.Main) {
            val imageLoader = ImageLoader(requireContext())
            val request = ImageRequest.Builder(requireContext())
                .data(url)
                .crossfade(true)
                .allowHardware(false) // Desativa aceleração de hardware para máxima compatibilidade
                .target(
                    onStart = {
                        // O progressBar já está visível
                    },
                    onSuccess = { result ->
                        binding.photoView.setImageDrawable(result)
                        binding.progressBar.isVisible = false
                        binding.photoView.isVisible = true
                    },
                    onError = {
                        showLoadError("Erro ao carregar a imagem.")
                    }
                )
                .listener(
                    onError = { _, result ->
                        Log.e(TAG, "Erro no Coil: ${result.throwable.message}", result.throwable)
                    }
                )
                .build()
            imageLoader.enqueue(request)
        }
    }

    private fun showLoadError(message: String) {
        if (!isAdded) return
        binding.progressBar.isVisible = false
        binding.pdfView.isVisible = false
        binding.photoView.isVisible = false
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}