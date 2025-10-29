package com.developersbeeh.medcontrol.ui.healthnotes

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentHealthNotesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HealthNotesFragment : Fragment() {

    private var _binding: FragmentHealthNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HealthNotesViewModel by viewModels()
    private val args: HealthNotesFragmentArgs by navArgs()
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId)

        setupRecyclerView()
        setupListenersAndVisibility()
        observeViewModel()
        setupMenu()
    }

    private fun setupRecyclerView() {
        val onEditClick: (HealthNote) -> Unit = { note ->
            val action = HealthNotesFragmentDirections
                .actionHealthNotesFragmentToAddHealthNoteFragment(
                    dependentId = args.dependentId,
                    dependentName = args.dependentName,
                    healthNoteToEdit = note
                )
            findNavController().navigate(action)
        }
        val onDeleteClick: (HealthNote) -> Unit = { note ->
            showDeleteConfirmationDialog(note)
        }

        val adapter = HealthNotesAdapter(onEditClick, onDeleteClick)
        binding.recyclerViewHealthNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHealthNotes.adapter = adapter

        viewModel.healthNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            binding.emptyStateLayout.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupListenersAndVisibility() {
        // ✅ CORREÇÃO: Lógica de visibilidade simplificada com a nova função
        val canAddNotes = userPreferences.temPermissao(PermissaoTipo.REGISTRAR_ANOTACOES)
        binding.fabAddHealthNote.visibility = if (canAddNotes) View.VISIBLE else View.GONE

        binding.fabAddHealthNote.setOnClickListener {
            val action = HealthNotesFragmentDirections
                .actionHealthNotesFragmentToAddHealthNoteFragment(
                    dependentId = args.dependentId,
                    dependentName = args.dependentName
                )
            findNavController().navigate(action)
        }
    }

    private fun showDeleteConfirmationDialog(note: HealthNote) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir esta anotação?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deleteHealthNote(note)
            }
            .create()
            .show()
    }

    private fun observeViewModel() {
        viewModel.deleteStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Anotação excluída!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Falha ao excluir anotação.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.health_notes_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_view_charts -> {
                        val action = HealthNotesFragmentDirections.actionHealthNotesFragmentToHealthChartsFragment(args.dependentId)
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}