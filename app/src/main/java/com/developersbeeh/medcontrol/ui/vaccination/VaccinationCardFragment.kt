// src/main/java/com/developersbeeh/medcontrol/ui/vaccination/VaccinationCardFragment.kt

package com.developersbeeh.medcontrol.ui.vaccination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.DialogRegisterVaccineBinding
import com.developersbeeh.medcontrol.databinding.FragmentVaccinationCardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaccinationCardFragment : Fragment() {

    private var _binding: FragmentVaccinationCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaccinationViewModel by viewModels()
    private val args: VaccinationCardFragmentArgs by navArgs()
    private lateinit var adapter: VaccinationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaccinationCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Carteira de Vacinação"

        if (args.dependentDob.isBlank()) {
            binding.recyclerViewVaccines.isVisible = false
            binding.emptyStateLayout.isVisible = true
        } else {
            viewModel.initialize(args.dependentId, args.dependentDob)
            setupRecyclerView()
            observeViewModel()
        }
    }

    private fun setupRecyclerView() {
        adapter = VaccinationAdapter { uiItem ->
            showRegisterVaccineDialog(uiItem)
        }
        binding.recyclerViewVaccines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewVaccines.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.groupedVaccines.observe(viewLifecycleOwner) { groupedMap ->
            val listItems = mutableListOf<VaccinationListItem>()
            groupedMap.forEach { (age, items) ->
                listItems.add(VaccinationListItem.GroupHeader(age))
                items.sortedBy { it.vacina.nome }.forEach { uiItem ->
                    listItems.add(VaccinationListItem.VaccineItem(uiItem))
                }
            }
            adapter.submitList(listItems)
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRegisterVaccineDialog(uiItem: VacinaUiItem) {
        val dialogBinding = DialogRegisterVaccineBinding.inflate(layoutInflater)

        // Preenche os dados se já houver um registro
        uiItem.registro?.let {
            dialogBinding.editTextLot.setText(it.lote)
            dialogBinding.editTextLocation.setText(it.localAplicacao)
            dialogBinding.editTextNotes.setText(it.notas)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Registrar ${uiItem.vacina.nome}")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val lote = dialogBinding.editTextLot.text.toString().trim().takeIf { it.isNotEmpty() }
                val local = dialogBinding.editTextLocation.text.toString().trim().takeIf { it.isNotEmpty() }
                val notas = dialogBinding.editTextNotes.text.toString().trim().takeIf { it.isNotEmpty() }
                viewModel.saveVaccineRecord(uiItem.vacina, lote, local, notas)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}