package com.developersbeeh.medcontrol.ui.documents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.data.model.TipoDocumento
import com.developersbeeh.medcontrol.databinding.FragmentHealthDocumentsBinding
import com.developersbeeh.medcontrol.util.UiState
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthDocumentsFragment : Fragment() {

    private var _binding: FragmentHealthDocumentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HealthDocumentsViewModel by viewModels()
    private val args: HealthDocumentsFragmentArgs by navArgs()
    private val adapter by lazy {
        HealthDocumentsAdapter(
            onItemClick = { documento -> viewDocument(documento) },
            onMenuClick = { documento, anchorView -> showDocumentMenu(documento, anchorView) }
        )
    }
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId, args.dependentName)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Documentos de ${args.dependentName}"

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        binding.fabAddDocument.isVisible = userPreferences.temPermissao(PermissaoTipo.ADICIONAR_DOCUMENTOS)

        showGuideIfFirstTime()
    }

    private fun setupListeners() {
        binding.fabAddDocument.setOnClickListener {
            viewModel.onAddDocumentClicked()
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val type: TipoDocumento? = when (checkedId) {
                R.id.chipExamLab -> TipoDocumento.EXAME_LABORATORIAL
                R.id.chipExamImage -> TipoDocumento.EXAME_IMAGEM
                R.id.chipPrescription -> TipoDocumento.RECEITUARIO
                R.id.chipReport -> TipoDocumento.RELATORIO
                R.id.chipOther -> TipoDocumento.OUTRO
                else -> null
            }
            viewModel.applyFilter(type)
        }
    }

    private fun setupRecyclerView() {
        // ✅ CORREÇÃO: O RecyclerView agora é um filho do StateLayout
        binding.stateLayout.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewDocuments).adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.documents.observe(viewLifecycleOwner) { state ->
            // ✅ CORREÇÃO: Lógica de UI simplificada para uma única chamada
            binding.stateLayout.setState(state) { it.isNotEmpty() }

            if (state is UiState.Success && state.data.isEmpty()) {
                setupEmptyState()
            }

            if (state is UiState.Success) {
                adapter.submitList(state.data)
            }
        }

        binding.stateLayout.onRetry = {
            viewModel.initialize(args.dependentId, args.dependentName)
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { action ->
                findNavController().navigate(action)
            }
        }
        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGuideIfFirstTime() {
        if (!userPreferences.hasSeenDocumentsGuide()) {
            binding.root.doOnPreDraw {
                if (activity == null) return@doOnPreDraw

                val fabTarget = TapTarget.forView(binding.fabAddDocument, "Adicione seus documentos", "Clique aqui para fazer o upload de exames, receitas e relatórios. Mantenha tudo organizado e seguro.")
                    .outerCircleColor(R.color.md_theme_primary)
                    .targetCircleColor(R.color.white)
                    .titleTextColor(R.color.white)
                    .descriptionTextColor(R.color.white)
                    .cancelable(true)
                    .tintTarget(true)

                TapTargetSequence(activity)
                    .targets(fabTarget)
                    .listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() { userPreferences.setDocumentsGuideSeen(true) }
                        override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                        override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setDocumentsGuideSeen(true) }
                    }).start()
            }
        }
    }

    private fun setupEmptyState() {
        val canAddDocuments = userPreferences.temPermissao(PermissaoTipo.ADICIONAR_DOCUMENTOS)
        binding.stateLayout.setEmptyState(
            title = "Nenhum Documento",
            subtitle = "Adicione exames, receitas e relatórios para mantê-los organizados e sempre à mão.",
            buttonText = if (canAddDocuments) "Adicionar Documento" else null,
            onActionClick = if (canAddDocuments) { { binding.fabAddDocument.performClick() } } else null
        )
    }

    private fun showDocumentMenu(documento: DocumentoSaude, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.document_item_menu, popup.menu)

        val canEditOrDelete = userPreferences.temPermissao(PermissaoTipo.ADICIONAR_DOCUMENTOS)
        popup.menu.findItem(R.id.action_edit_document).isVisible = canEditOrDelete
        popup.menu.findItem(R.id.action_delete_document).isVisible = canEditOrDelete

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit_document -> {
                    viewModel.onDocumentSelected(documento)
                    true
                }
                R.id.action_delete_document -> {
                    showDeleteConfirmationDialog(documento)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun viewDocument(documento: DocumentoSaude) {
        if (documento.fileUrl.isBlank()) {
            Toast.makeText(context, "Este documento não possui um arquivo anexado.", Toast.LENGTH_SHORT).show()
            return
        }
        val action = HealthDocumentsFragmentDirections
            .actionHealthDocumentsFragmentToDocumentViewerFragment(
                documentoUrl = documento.fileUrl,
                documentoTitulo = documento.titulo
            )
        findNavController().navigate(action)
    }

    private fun showDeleteConfirmationDialog(documento: DocumentoSaude) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Excluir Documento?")
            .setMessage("Tem certeza que deseja excluir '${documento.titulo}'? Esta ação é permanente.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteDocument(documento)
            }
            .create()
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}