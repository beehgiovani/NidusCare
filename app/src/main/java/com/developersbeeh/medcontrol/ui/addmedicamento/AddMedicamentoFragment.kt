package com.developersbeeh.medcontrol.ui.addmedicamento

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoMedicamento
import com.developersbeeh.medcontrol.databinding.FragmentAddMedicamentoBinding
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TAG = "AddMedicamentoFragment"

@AndroidEntryPoint
class AddMedicamentoFragment : Fragment() {

    private var _binding: FragmentAddMedicamentoBinding? = null
    internal val binding get() = _binding!!
    internal val viewModel: AddMedicamentoViewModel by viewModels()
    private val searchViewModel: SearchMedicamentosViewModel by viewModels()
    private val args: AddMedicamentoFragmentArgs by navArgs()

    // Handlers
    internal lateinit var uiHandler: AddMedicamentoUiHandler
    internal lateinit var actionHandler: AddMedicamentoActionHandler
    private lateinit var guideHandler: AddMedicamentoGuideHandler
    private lateinit var userPreferences: UserPreferences

    // Propriedades de Estado
    internal var medicamentoParaEditar: Medicamento? = null
    internal var dataInicioSelecionada: LocalDate = LocalDate.now()
    internal var dataTerminoSelecionada: LocalDate = LocalDate.now().plusDays(7)
    internal val lotesList = mutableListOf<EstoqueLote>()
    internal lateinit var lotesAdapter: LotesAdapter
    internal val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    internal val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    internal val unidadesDeDosagemPadrao = listOf("comprimido(s)", "cápsula(s)", "drágea(s)", "gota(s)", "mL", "UI", "unidade(s)")
    internal val unidadesDeDosagemTopico = listOf("Aplicação(ões)", "Fina camada", "Gota(s)", "cm")

    private lateinit var searchAdapter: MedicamentoDatabaseAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var hideSuggestionsRunnable: Runnable? = null

    internal val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                actionHandler.checkExactAlarmPermission()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                actionHandler.handleSaveWithPermissionResult(granted = false)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddMedicamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())

        // ✅ CORREÇÃO: ViewModel é inicializado primeiro
        viewModel.initialize(args.medicamento)

        setupRecyclerViews()

        lotesAdapter = LotesAdapter(binding.autoCompleteUnidade.text.toString()) { lote ->
            lotesList.remove(lote)
            lotesAdapter.submitList(lotesList.toList())
            binding.dividerLotes.visibility = if (lotesList.isNotEmpty()) View.VISIBLE else View.GONE
        }

        uiHandler = AddMedicamentoUiHandler(this, binding, lotesAdapter, lotesList)
        actionHandler = AddMedicamentoActionHandler(this, binding, viewModel, uiHandler)
        guideHandler = AddMedicamentoGuideHandler(this, binding, userPreferences)

        setupInitialState()
        observeViewModels()
        setupListeners()

        guideHandler.showGuideIfFirstTime()
    }

    private fun setupInitialState() {
        // A lógica de inicialização foi movida para o ViewModel
        viewModel.loadAllCaregiverMedications(args.dependentId)

        // Observa o medicamento para preencher o formulário
        viewModel.medicamentoParaEditar.observe(viewLifecycleOwner) { med ->
            medicamentoParaEditar = med // Atualiza a variável local
            if (args.medicamento == null) {
                binding.textViewTitle.text = getString(R.string.add_medication_title)
                // Se não for um rascunho, preenche com o padrão (novo)
                if (!viewModel.hasDraft()) {
                    uiHandler.preencherCamposParaEdicao(med)
                } else {
                    actionHandler.checkForDraft()
                }
            } else {
                binding.textViewTitle.text = getString(R.string.edit_medication_title)
                uiHandler.preencherCamposParaEdicao(med)
            }
        }

        setupAutoComplete()
        uiHandler.setupDurationUnitDropdown()
    }

    private fun observeViewModels() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            uiHandler.updateWizardUi(state)
        }
        viewModel.saveSuccessEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), getString(R.string.medication_saved), Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.showDuplicateMedicationDialog.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { existingMedication ->
                actionHandler.showDuplicateMedicationDialog(existingMedication)
            }
        }
        lifecycleScope.launch {
            searchViewModel.uiState.collectLatest { state ->
                searchAdapter.submitList(state.medicamentos)
                binding.recyclerViewSearchResults.visibility = if (state.medicamentos.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.existingMedicationFound.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { foundPair ->
                if (foundPair != null) {
                    val (_, dependentName) = foundPair
                    binding.suggestionDetailsText.text = "Encontrado para: ${dependentName}"
                    binding.suggestionCard.visibility = View.VISIBLE
                } else {
                    binding.suggestionCard.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.headerStep1.setOnClickListener { viewModel.onStepHeaderClicked(WizardStep.STEP_1) }
        binding.headerStep2.setOnClickListener { viewModel.onStepHeaderClicked(WizardStep.STEP_2) }
        binding.headerStep3.setOnClickListener { viewModel.onStepHeaderClicked(WizardStep.STEP_3) }
        binding.headerStep4.setOnClickListener { viewModel.onStepHeaderClicked(WizardStep.STEP_4) }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateAllSummaries()
                uiHandler.updateAddTimeButtonState()
                actionHandler.saveDraft()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.editTextNome.addTextChangedListener(textWatcher)
        binding.editTextDosagemValor.addTextChangedListener(textWatcher)
        binding.autoCompleteUnidade.addTextChangedListener(textWatcher)
        binding.editTextDuracao.addTextChangedListener(textWatcher)
        binding.editTextFrequenciaValor.addTextChangedListener(textWatcher)
        binding.editTextPrincipioAtivo.addTextChangedListener(textWatcher)
        binding.editTextAnotacoes.addTextChangedListener(textWatcher)
        binding.editTextClasseTerapeutica.addTextChangedListener(textWatcher)
        binding.editTextNivelAlerta.addTextChangedListener(textWatcher)
        binding.editTextGlicemiaAlvo.addTextChangedListener(textWatcher)
        binding.editTextFatorSensibilidade.addTextChangedListener(textWatcher)
        binding.editTextRatioCarboidrato.addTextChangedListener(textWatcher)

        val hierarchyChangeListener = object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(p0: View?, p1: View?) { updateAllSummaries(); actionHandler.saveDraft() }
            override fun onChildViewRemoved(p0: View?, p1: View?) { updateAllSummaries(); actionHandler.saveDraft() }
        }
        binding.chipGroupHorarios.setOnHierarchyChangeListener(hierarchyChangeListener)
        binding.chipGroupLocais.setOnHierarchyChangeListener(hierarchyChangeListener)

        lotesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() { updateAllSummaries(); actionHandler.saveDraft() }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { updateAllSummaries(); actionHandler.saveDraft() }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) { updateAllSummaries(); actionHandler.saveDraft() }
        })

        binding.radioGroupTipoMedicamento.setOnCheckedChangeListener { _, _ -> updateAllSummaries(); uiHandler.updateUiForMedicamentoType(); actionHandler.saveDraft() }
        binding.chipGroupFrequenciaTipo.setOnCheckedStateChangeListener { _, _ -> updateAllSummaries(); uiHandler.updateFrequencyControlsVisibility(); uiHandler.updateAddTimeButtonState(); actionHandler.saveDraft() }
        binding.chipGroupDiasSemana.setOnCheckedStateChangeListener { _, _ -> updateAllSummaries(); uiHandler.updateAddTimeButtonState(); actionHandler.saveDraft() }
        binding.radioGroupTermino.setOnCheckedChangeListener { _, checkedId ->
            updateAllSummaries()
            binding.layoutDuracao.visibility = if (checkedId == R.id.radioTerminoDuracao) View.VISIBLE else View.GONE
            binding.tilDataTermino.visibility = if (checkedId == R.id.radioTerminoDataFim) View.VISIBLE else View.GONE
            actionHandler.saveDraft()
        }
        binding.checkboxNotificacao.setOnCheckedChangeListener { _, _ -> updateAllSummaries(); actionHandler.saveDraft() }
        binding.switchUsoEsporadico.setOnCheckedChangeListener { _, _ -> updateAllSummaries(); actionHandler.saveDraft() }
        binding.radioGroupTipoDosagem.setOnCheckedChangeListener { _, _ -> updateAllSummaries(); uiHandler.updateDosageUI(); actionHandler.saveDraft() }

        binding.buttonSalvar.setOnClickListener { actionHandler.handleSave() }
        binding.buttonCancelar.setOnClickListener {
            viewModel.clearDraft()
            findNavController().popBackStack()
        }
        binding.buttonAddHorario.setOnClickListener { actionHandler.showTimePickerDialog() }
        binding.buttonAddIntervalo.setOnClickListener { actionHandler.showIntervalsDialog() }
        binding.editTextDataInicio.setOnClickListener { actionHandler.showDatePickerDialogForTreatmentStart() }
        binding.tilDataInicio.setEndIconOnClickListener { actionHandler.showDatePickerDialogForTreatmentStart() }
        binding.editTextDataTermino.setOnClickListener { actionHandler.showDatePickerDialogForTreatmentEnd() }
        binding.tilDataTermino.setEndIconOnClickListener { actionHandler.showDatePickerDialogForTreatmentEnd() }
        binding.buttonAddLote.setOnClickListener {
            if (!actionHandler.tryAddLoteFromInlineForm()) {
                Toast.makeText(context, getString(R.string.lote_invalid_data), Toast.LENGTH_SHORT).show()
            }
        }
        binding.editTextLoteValidade.setOnClickListener { actionHandler.showDatePickerDialogForLote() }
        binding.tilLoteValidade.setEndIconOnClickListener { actionHandler.showDatePickerDialogForLote() }
        binding.buttonAddLocal.setOnClickListener {
            val nomeLocal = binding.editTextNovoLocal.text.toString().trim()
            if (nomeLocal.isNotBlank()) {
                uiHandler.addLocalChip(nomeLocal)
                binding.editTextNovoLocal.text = null
            } else {
                Toast.makeText(requireContext(), getString(R.string.local_required), Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonCopyData.setOnClickListener {
            viewModel.existingMedicationFound.value?.peekContent()?.let { foundPair ->
                uiHandler.preencherCamposParaEdicao(foundPair.first)
                binding.suggestionCard.visibility = View.GONE
                binding.recyclerViewSearchResults.visibility = View.GONE
                Toast.makeText(context, getString(R.string.data_copied_success), Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonDismissSuggestion.setOnClickListener {
            binding.suggestionCard.visibility = View.GONE
        }
    }

    internal fun updateAllSummaries() {
        val med = actionHandler.getCurrentMedicationStateFromUi()
        val tipoAdmin = when(med.tipo) {
            TipoMedicamento.ORAL -> "Oral"
            TipoMedicamento.TOPICO -> "Tópico"
            TipoMedicamento.INJETAVEL -> "Injetável"
        }
        val horarios = med.horarios.joinToString(", ") { it.format(timeFormatter) }
        val frequencia = when (med.frequenciaTipo) {
            FrequenciaTipo.SEMANAL -> if (horarios.isNotBlank()) "Semanalmente ($horarios)" else "Semanalmente"
            FrequenciaTipo.INTERVALO_DIAS -> if (horarios.isNotBlank()) "A cada ${med.frequenciaValor} dia(s) ($horarios)" else "A cada ${med.frequenciaValor} dia(s)"
            else -> if (horarios.isNotBlank()) "Diariamente ($horarios)" else ""
        }
        val duracao = when {
            med.isUsoContinuo -> "Uso contínuo"
            med.duracaoDias > 0 -> "Por ${med.duracaoDias} dias"
            else -> ""
        }
        val estoque = if (lotesList.isNotEmpty()) "${lotesList.sumOf { it.quantidade }} unidades em estoque" else ""
        val opcionais = if (!med.principioAtivo.isNullOrBlank() || !med.anotacoes.isNullOrBlank()) "Preenchido" else ""
        viewModel.updateSummaries(med.nome, tipoAdmin, med.dosagem, frequencia, duracao, med.usaNotificacao, estoque, opcionais)
    }

    private fun setupRecyclerViews() {
        searchAdapter = MedicamentoDatabaseAdapter { medicamentoDatabase ->
            binding.editTextNome.setText(medicamentoDatabase.NOME_PRODUTO.trim().capitalizeWords())
            binding.editTextPrincipioAtivo.setText(medicamentoDatabase.PRINCIPIO_ATIVO.trim().capitalizeWords())
            binding.editTextClasseTerapeutica.setText(medicamentoDatabase.CLASSE_TERAPEUTICA.trim().capitalizeWords())
            binding.editTextAnotacoes.setText("")
            val anotacoesExtras = StringBuilder()
            if (medicamentoDatabase.CLASSE_TERAPEUTICA.isNotBlank()) {
                anotacoesExtras.append(medicamentoDatabase.CLASSE_TERAPEUTICA.trim().capitalizeWords())
            }
            binding.editTextAnotacoes.setText(anotacoesExtras.toString())
            binding.recyclerViewSearchResults.visibility = View.GONE
            viewModel.onStepHeaderClicked(WizardStep.STEP_2)
            binding.editTextDosagemValor.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.editTextDosagemValor, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.recyclerViewSearchResults.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupAutoComplete() {
        binding.editTextNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()
                if (!query.isNullOrEmpty() && query.length >= 3) {
                    searchViewModel.searchMedicamentos(query)
                    viewModel.searchExistingMedication(query)
                } else {
                    binding.recyclerViewSearchResults.visibility = View.GONE
                    binding.suggestionCard.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.editTextNome.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideSuggestionsRunnable = Runnable {
                    if (_binding != null) {
                        binding.recyclerViewSearchResults.visibility = View.GONE
                    }
                }
                handler.postDelayed(hideSuggestionsRunnable!!, 200)
            }
        }
    }

    override fun onDestroyView() {
        hideSuggestionsRunnable?.let { handler.removeCallbacks(it) }
        super.onDestroyView()
        _binding = null
    }

    // Funções auxiliares para os Handlers acessarem
    internal fun isEditing() = medicamentoParaEditar != null
    internal fun getDependentIdArgs() = args.dependentId
    internal fun getDependentNameArgs() = args.dependentName
    internal fun isCaregiverArgs() = args.isCaregiver
    internal fun isUsoEsporadico() = binding.switchUsoEsporadico.isChecked
    internal fun isNotificationChecked() = binding.checkboxNotificacao.isChecked
    internal fun setLoteValidadeText(text: String) = binding.editTextLoteValidade.setText(text)
    internal fun clearHorarioChips() = binding.chipGroupHorarios.removeAllViews()
}