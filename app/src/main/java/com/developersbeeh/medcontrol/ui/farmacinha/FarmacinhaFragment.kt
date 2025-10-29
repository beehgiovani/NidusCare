package com.developersbeeh.medcontrol.ui.farmacinha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.databinding.DialogRefillStockBinding
import com.developersbeeh.medcontrol.databinding.FragmentFarmacinhaBinding
import com.developersbeeh.medcontrol.databinding.LayoutEmptyStateBinding
import com.developersbeeh.medcontrol.util.UiState
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class FarmacinhaFragment : Fragment() {

    private var _binding: FragmentFarmacinhaBinding? = null
    private val binding get() = _binding!!

    private lateinit var emptyStateBinding: LayoutEmptyStateBinding

    private val viewModel: FarmacinhaViewModel by viewModels()
    private val args: FarmacinhaFragmentArgs by navArgs()
    private lateinit var adapter: FarmacinhaAdapter
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmacinhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyStateBinding = LayoutEmptyStateBinding.bind(binding.emptyStateLayout.root)
        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = FarmacinhaAdapter(
            onTomarDoseClick = { item -> showTakeDoseConfirmationDialog(item) },
            onReporEstoqueClick = { item -> showRefillStockDialog(item) },
            onAdicionarPosologiaClick = { item ->
                val action = FarmacinhaFragmentDirections.actionFarmacinhaFragmentToAddMedicamentoFragment(
                    medicamento = item.medicamento,
                    dependentId = item.dependentId,
                    dependentName = item.dependenteNome,
                    isCaregiver = true
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { item -> showDeleteConfirmationDialog(item) }
        )
        binding.recyclerViewFarmacinha.adapter = adapter
        binding.recyclerViewFarmacinha.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.fabScanMedication.setOnClickListener {
            if (userPreferences.isPremium()) {
                val action = FarmacinhaFragmentDirections.actionFarmacinhaFragmentToScanAndConfirmFragment(args.dependentId, args.dependentName)
                findNavController().navigate(action)
            } else {
                showPremiumDialog()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.dependents.observe(viewLifecycleOwner) { dependents ->
            if (dependents.isNotEmpty()) {
                setupFilters(dependents)
            }
        }

        viewModel.farmacinhaItens.observe(viewLifecycleOwner) { state ->
            binding.shimmerLayout.isVisible = state is UiState.Loading
            binding.recyclerViewFarmacinha.isVisible = state is UiState.Success && state.data.isNotEmpty()
            binding.emptyStateLayout.root.isVisible = state is UiState.Success && state.data.isEmpty()

            when (state) {
                is UiState.Success -> {
                    adapter.submitList(state.data)
                    if (state.data.isEmpty()) {
                        setupEmptyState()
                    }
                }
                is UiState.Error -> {
                    setupErrorState(state.message)
                    binding.emptyStateLayout.root.isVisible = true
                }
                else -> {}
            }
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilters(dependents: List<Dependente>) {
        if (dependents.size <= 1 && args.dependentId != null) {
            binding.filtersScrollView.visibility = View.GONE
            updateTitle(args.dependentId, dependents)
            return
        }

        binding.filtersScrollView.visibility = View.VISIBLE
        val chipGroup = binding.chipGroupFilterDependents
        chipGroup.removeAllViews()
        val inflater = LayoutInflater.from(context)

        val allChip = inflater.inflate(R.layout.single_filter_chip, chipGroup, false) as Chip
        allChip.id = View.generateViewId()
        allChip.text = "Todos"
        allChip.tag = null
        chipGroup.addView(allChip)

        dependents.forEach { dependent ->
            val chip = inflater.inflate(R.layout.single_filter_chip, chipGroup, false) as Chip
            chip.id = View.generateViewId()
            chip.text = dependent.nome
            chip.tag = dependent.id
            chipGroup.addView(chip)
        }

        val initialChip = chipGroup.children
            .find { it.tag == args.dependentId }?.let { it as Chip } ?: allChip
        initialChip.isChecked = true
        updateTitle(initialChip.tag as? String, dependents)

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == View.NO_ID) return@setOnCheckedChangeListener
            val selectedChip = group.findViewById<Chip>(checkedId)
            val selectedDependentId = selectedChip.tag as? String
            viewModel.setFilter(selectedDependentId)
            updateTitle(selectedDependentId, dependents)
        }
    }

    private fun updateTitle(selectedDependentId: String?, dependents: List<Dependente>) {
        val title = if (selectedDependentId == null) {
            "Caixa de Remédios (Global)"
        } else {
            val name = dependents.find { it.id == selectedDependentId }?.nome ?: args.dependentName ?: ""
            "Caixa de Remédios de $name"
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    private fun setupEmptyState() {
        emptyStateBinding.lottieAnimationView.setAnimation(R.raw.empty_list)
        emptyStateBinding.tvEmptyTitle.text = "Sua Caixa de Remédios está vazia"
        emptyStateBinding.tvEmptyMessage.text = "Adicione medicamentos de uso esporádico para tê-los sempre à mão."
        emptyStateBinding.btnEmptyAction.text = "Adicionar Medicamento"
        emptyStateBinding.btnEmptyAction.setOnClickListener {
            // ✅ CORREÇÃO: Usa o ID do dependente selecionado no filtro para a navegação
            val action = NavGraphDirections.actionGlobalToAddMedicamentoFragment(
                medicamento = null,
                dependentId = viewModel.selectedDependentId.value ?: args.dependentId ?: "",
                dependentName = args.dependentName ?: "",
                isCaregiver = true
            )
            findNavController().navigate(action)
        }
    }

    private fun setupErrorState(message: String?) {
        emptyStateBinding.lottieAnimationView.setAnimation(R.raw.error_animation)
        emptyStateBinding.tvEmptyTitle.text = "Ocorreu um erro"
        emptyStateBinding.tvEmptyMessage.text = message ?: "Não foi possível carregar os dados."
        emptyStateBinding.btnEmptyAction.text = "Tentar Novamente"
        emptyStateBinding.btnEmptyAction.setOnClickListener {
            viewModel.initialize(args.dependentId)
        }
    }

    private fun showPremiumDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.functionality_premium))
            .setMessage(getString(R.string.scan_medication_premium_desc))
            .setNegativeButton(getString(R.string.not_now), null)
            .setPositiveButton(getString(R.string.go_premium)) { _, _ ->
                findNavController().navigate(NavGraphDirections.actionGlobalToPremiumPlansFragment())
            }
            .show()
    }

    private fun showTakeDoseConfirmationDialog(item: FarmacinhaItem) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Registrar Dose")
            .setMessage("Confirmar o registro de uma dose de '${item.medicamento.nome}' para ${item.dependenteNome}?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                viewModel.markDoseAsTaken(item)
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog(item: FarmacinhaItem) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.delete_medication_title))
            .setMessage(getString(R.string.delete_medication_farmacinha_message, item.medicamento.nome, item.dependenteNome))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteMedicamento(item)
            }
            .create()
            .show()
    }

    private fun showRefillStockDialog(item: FarmacinhaItem) {
        val dialogBinding = DialogRefillStockBinding.inflate(LayoutInflater.from(context))
        dialogBinding.tilRefillAmount.hint = "Adicionar (em ${item.medicamento.unidadeDeEstoque})"
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var selectedDate: LocalDate? = null

        dialogBinding.editTextExpirationDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione a Data de Validade")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                dialogBinding.editTextExpirationDate.setText(selectedDate?.format(dateFormatter))
            }
            datePicker.show(childFragmentManager, "EXPIRATION_DATE_PICKER_REFILL")
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle("Repor Estoque de ${item.medicamento.nome}")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Adicionar") { _, _ ->
                val amountStr = dialogBinding.editTextRefillAmount.text.toString().replace(',', '.')
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(context, "Quantidade inválida.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (selectedDate == null) {
                    Toast.makeText(context, "Selecione a data de validade.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val novoLote = EstoqueLote(
                    quantidade = amount,
                    quantidadeInicial = amount,
                    dataValidadeString = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                viewModel.addStockLot(item, novoLote)
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}