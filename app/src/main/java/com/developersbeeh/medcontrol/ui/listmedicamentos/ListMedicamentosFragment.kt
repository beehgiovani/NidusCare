package com.developersbeeh.medcontrol.ui.listmedicamentos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.developersbeeh.medcontrol.databinding.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ListMedicamentosFragment : Fragment() {

    private val args: ListMedicamentosFragmentArgs by navArgs()
    private val viewModel: ListMedicamentosViewModel by viewModels()
    private var _binding: FragmentListMedicamentosBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MedicamentoAdapter
    private var medicamentos: List<Medicamento> = emptyList()
    private var dependentName: String = ""
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListMedicamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.shimmerLayout.startShimmer()
        viewModel.initialize(args.dependentId)
        userPreferences = UserPreferences(requireContext())
        setupSearch()
        initializeComponents()
        observeViewModelEvents()
        setupMenu()
        setupAds()
    }

    private fun setupRecyclerView() {
        val podeRegistrarDose = userPreferences.temPermissao(PermissaoTipo.REGISTRAR_DOSE)

        adapter = MedicamentoAdapter(
            isCaregiver = args.isCaregiver,
            podeRegistrarDose = podeRegistrarDose,
            onDeleteClick = { uiState -> showDeleteConfirmationDialog(uiState.medicamento) },
            onEditClick = { uiState -> navigateToEditMedicamento(uiState.medicamento) },
            onMarkAsTakenClick = { uiState ->
                if (podeRegistrarDose) {
                    viewModel.markDoseAsTaken(uiState.medicamento, uiState)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.permission_denied_register_doses), Toast.LENGTH_SHORT).show()
                }
            },
            onPausePlayClick = { uiState -> viewModel.togglePauseState(uiState.medicamento) },
            onRefillClick = { uiState -> showRefillStockDialog(uiState.medicamento) },
            onSkipDoseClick = { uiState ->
                if (podeRegistrarDose) {
                    showSkipDoseDialog(uiState.medicamento)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.permission_denied_skip_doses), Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerViewMedicamentos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ListMedicamentosFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupAds() {
        MobileAds.initialize(requireContext()) {}
        if (!userPreferences.isPremium()) {
            binding.adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        } else {
            binding.adView.visibility = View.GONE
        }
    }

    private fun observeViewModelEvents() {
        viewModel.dependentName.observe(viewLifecycleOwner) { name ->
            dependentName = name
            val title = getString(R.string.medications_of, name)
            (activity as? AppCompatActivity)?.supportActionBar?.title = title
            binding.textViewDependentName.text = title
        }

        viewModel.summaryText.observe(viewLifecycleOwner) { summary ->
            if (summary.isNotBlank() && !summary.contains(getString(R.string.no_scheduled_tasks_today))) {
                binding.cardSummary.visibility = View.VISIBLE
                binding.textViewSummary.text = summary
            } else {
                binding.cardSummary.visibility = View.GONE
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiStateList ->
            uiStateList?.let {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                updateUI(it)
            }
        }

        viewModel.doseConfirmationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { confirmationEvent ->
                handleDoseConfirmationEvent(confirmationEvent)
            }
        }

        viewModel.doseTakenFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fabAddMedicamento)
                    .show()
            }
        }

        viewModel.doseRegistrationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { registrationEvent ->
                when (registrationEvent) {
                    is DoseRegistrationEvent.ShowLocationSelector -> showSelectLocationDialog(registrationEvent.medicamento, registrationEvent.proximoLocalSugerido, registrationEvent.quantidade, registrationEvent.glicemia, registrationEvent.notas)
                    is DoseRegistrationEvent.ShowManualDoseInput -> showManualDoseInputDialog(registrationEvent.medicamento)
                    is DoseRegistrationEvent.ShowCalculatedDoseInput -> showCalculatedDoseDialog(registrationEvent.medicamento)
                    is DoseRegistrationEvent.ShowEarlyDoseReasonDialog -> showEarlyDoseReasonDialog(registrationEvent.medicamento, registrationEvent.nextDoseTimeToCancel)
                }
            }
        }

        viewModel.showUndoDeleteSnackbar.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { medicamento ->
                showUndoSnackbar(medicamento)
            }
        }
    }

    private fun showSkipDoseDialog(medicamento: Medicamento) {
        val dialogBinding = DialogAddNoteEarlyDoseBinding.inflate(layoutInflater)
        dialogBinding.tilNote.hint = getString(R.string.reason_optional)
        dialogBinding.textViewMessage.text = getString(R.string.skip_dose_message, medicamento.nome)

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.skip_dose_title))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val reason = dialogBinding.editTextNote.text.toString().trim().takeIf { it.isNotEmpty() }
                viewModel.skipDose(medicamento, reason)
            }
            .create()
            .show()
    }

    private fun handleDoseConfirmationEvent(confirmationEvent: DoseConfirmationEvent) {
        when (confirmationEvent) {
            is DoseConfirmationEvent.ConfirmSlightlyEarlyDose -> showSlightlyEarlyDoseDialog(confirmationEvent)
            is DoseConfirmationEvent.ConfirmVeryEarlyDose -> showVeryEarlyDoseDialog(confirmationEvent)
            is DoseConfirmationEvent.ConfirmExtraDose -> showExtraDoseDialog(confirmationEvent)
            is DoseConfirmationEvent.ConfirmSporadicDose -> showSporadicDoseDialog(confirmationEvent)
            is DoseConfirmationEvent.ConfirmLateDose -> showLateDoseDialog(confirmationEvent)
            is DoseConfirmationEvent.ConfirmLateDoseLogging -> showLateDoseLoggingDialog(confirmationEvent)
        }
    }

    private fun showLateDoseLoggingDialog(event: DoseConfirmationEvent.ConfirmLateDoseLogging) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.register_late_dose_title))
            .setMessage(getString(R.string.register_late_dose_message))
            .setPositiveButton(getString(R.string.register_late_dose_option_now)) { _, _ ->
                viewModel.logLateDoseNow(event.medicamento, event.scheduledTime)
            }
            .setNegativeButton(getString(R.string.register_late_dose_option_forgot)) { _, _ ->
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(event.scheduledTime.hour)
                    .setMinute(event.scheduledTime.minute)
                    .setTitleText(getString(R.string.register_late_dose_timepicker_title))
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    val selectedTime = LocalTime.of(timePicker.hour, timePicker.minute)
                    val actualDateTime = LocalDateTime.of(event.scheduledTime.toLocalDate(), selectedTime)
                    viewModel.logForgottenDoseAtPastTime(event.medicamento, actualDateTime)
                }
                timePicker.show(parentFragmentManager, "FORGOTTEN_DOSE_TIME_PICKER")
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showLateDoseDialog(event: DoseConfirmationEvent.ConfirmLateDose) {
        val plural = if (event.hoursLate > 1) "s" else ""
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.reschedule_title))
            .setMessage(getString(R.string.reschedule_message, event.hoursLate, plural, event.medicamento.nome))
            .setNegativeButton(getString(R.string.reschedule_option_no)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.reschedule_option_yes)) { _, _ -> viewModel.readjustSchedule(event.medicamento, event.lateDoseTime) }
            .create()
            .show()
    }

    private fun showEarlyDoseReasonDialog(medicamento: Medicamento, nextDoseTimeToCancel: LocalDateTime) {
        val dialogBinding = DialogAddNoteEarlyDoseBinding.inflate(layoutInflater)
        dialogBinding.tilNote.hint = getString(R.string.reason_early_dose_optional)
        dialogBinding.textViewMessage.text = getString(R.string.register_early_dose_message, medicamento.nome)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.register_early_dose_title))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val reason = dialogBinding.editTextNote.text.toString()
                viewModel.confirmEarlyDoseWithReason(medicamento, nextDoseTimeToCancel, reason)
            }
            .create()
            .show()
    }

    private fun showSelectLocationDialog(medicamento: Medicamento, proximoLocalSugerido: String?, quantidade: Double?, glicemia: Double?, notas: String?) {
        val dialogBinding = DialogSelectLocationBinding.inflate(layoutInflater)
        val radioGroup = dialogBinding.radioGroupLocations
        dialogBinding.textViewNextSuggested.text = proximoLocalSugerido?.let { "${getString(R.string.suggestion_prefix)}$it" } ?: getString(R.string.where_was_application)
        var selectedRadioButton: RadioButton? = null
        medicamento.locaisDeAplicacao.forEach { local ->
            val radioButton = RadioButton(context).apply { text = local; id = View.generateViewId(); textSize = 18f; tag = local }
            radioGroup.addView(radioButton)
            if (local == proximoLocalSugerido) {
                selectedRadioButton = radioButton
            }
        }
        selectedRadioButton?.isChecked = true
        if (radioGroup.checkedRadioButtonId == -1 && radioGroup.childCount > 0) {
            (radioGroup.getChildAt(0) as RadioButton).isChecked = true
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.where_was_application))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val selectedId = radioGroup.checkedRadioButtonId
                if (selectedId != -1) {
                    val checkedRadioButton = radioGroup.findViewById<RadioButton>(selectedId)
                    viewModel.confirmDoseWithDetails(medicamento, checkedRadioButton.text.toString(), quantidade, glicemia, notas)
                }
            }
            .create()
            .show()
    }

    private fun showCalculatedDoseDialog(medicamento: Medicamento) {
        val dialogBinding = DialogCalculatedDoseBinding.inflate(layoutInflater)
        var doseSugeridaFinal = 0
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val glicemiaAtual = dialogBinding.editTextGlicemiaAtual.text.toString().toDoubleOrNull() ?: 0.0
                val carboidratos = dialogBinding.editTextCarboidratos.text.toString().toDoubleOrNull() ?: 0.0
                doseSugeridaFinal = viewModel.calculateInsulinDose(medicamento, glicemiaAtual, carboidratos)
                dialogBinding.textViewDoseSugerida.text = "${getString(R.string.suggested_dose_prefix)}$doseSugeridaFinal${getString(R.string.suggested_dose_suffix)}"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        dialogBinding.editTextGlicemiaAtual.addTextChangedListener(textWatcher)
        dialogBinding.editTextCarboidratos.addTextChangedListener(textWatcher)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.calculate_and_register_dose))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(R.string.next) { _, _ ->
                val glicemiaAtual = dialogBinding.editTextGlicemiaAtual.text.toString().toDoubleOrNull()
                if (glicemiaAtual == null) {
                    Toast.makeText(context, getString(R.string.error_insert_current_glycemia), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.confirmDoseTaking(medicamento, null, doseSugeridaFinal.toDouble(), glicemiaAtual)
            }
            .create()
            .show()
    }

    private fun showManualDoseInputDialog(medicamento: Medicamento) {
        val dialogBinding = DialogManualDoseBinding.inflate(layoutInflater)
        dialogBinding.tilQuantidadeAdministrada.hint = getString(R.string.hint_quantity_in, medicamento.unidadeDeEstoque)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.register_manual_dose_title))
            .setMessage(getString(R.string.register_manual_dose_message, medicamento.nome))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(R.string.next) { _, _ ->
                val quantidadeStr = dialogBinding.editTextQuantidadeAdministrada.text.toString()
                val quantidade = quantidadeStr.toDoubleOrNull()
                if (quantidade == null || quantidade <= 0) {
                    Toast.makeText(context, getString(R.string.error_invalid_quantity), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.confirmDoseTaking(medicamento, null, quantidade)
                }
            }
            .create()
            .show()
    }

    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_share_schedule) {
                    shareTodaysSchedule()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initializeComponents() {
        setupRecyclerView()
    }

    private fun showSporadicDoseDialog(event: DoseConfirmationEvent.ConfirmSporadicDose) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.register_dose_title))
            .setMessage(getString(R.string.register_dose_sporadic_message, event.medicamento.nome))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewModel.confirmSporadicDoseTaken(event.medicamento)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showSlightlyEarlyDoseDialog(event: DoseConfirmationEvent.ConfirmSlightlyEarlyDose) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val nextDoseTimeFormatted = event.nextDoseTime.format(timeFormatter)
        val message = getString(R.string.confirm_early_dose_message, nextDoseTimeFormatted)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.confirm_early_dose_title))
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewModel.confirmDoseTaking(event.medicamento, event.nextDoseTime, null)
            }
            .create()
            .show()
    }

    private fun showVeryEarlyDoseDialog(event: DoseConfirmationEvent.ConfirmVeryEarlyDose) {
        val dialogBinding = DialogAddNoteEarlyDoseBinding.inflate(layoutInflater)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val nextDoseTimeFormatted = event.nextDoseTime.format(timeFormatter)
        dialogBinding.textViewMessage.text = getString(R.string.very_early_dose_message, event.medicamento.nome, nextDoseTimeFormatted)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.very_early_dose_title))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm), null)
            .create()
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val note = dialogBinding.editTextNote.text.toString().trim()
                if (note.isEmpty() && event.noteRequired) {
                    dialogBinding.tilNote.error = getString(R.string.justification_required)
                } else {
                    viewModel.confirmDoseWithNote(event.medicamento, event.nextDoseTime, note)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun showExtraDoseDialog(event: DoseConfirmationEvent.ConfirmExtraDose) {
        val message = getString(R.string.overdose_message, event.medicamento.nome)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.overdose_title))
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewModel.confirmDoseTaking(event.medicamento, null, null)
            }
            .create()
            .show()
    }

    private fun navigateToEditMedicamento(medicamento: Medicamento) {
        val action = ListMedicamentosFragmentDirections.actionListMedicamentosFragmentToAddMedicamentoFragment(
            medicamento = medicamento,
            dependentId = args.dependentId,
            dependentName = dependentName,
            isCaregiver = args.isCaregiver
        )
        findNavController().navigate(action)
    }

    private fun navigateToAddMedicamento() {
        val action = NavGraphDirections.actionGlobalToAddMedicamentoFragment(
            medicamento = null,
            dependentId = args.dependentId,
            dependentName = dependentName,
            isCaregiver = args.isCaregiver
        )
        findNavController().navigate(action)
    }

    private fun showMedicamentoSelectionDialog() {
        val medicamentoNomes = medicamentos.map { it.nome }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_dose_title))
            .setItems(medicamentoNomes) { dialog, which ->
                val medicamentoSelecionado = medicamentos[which]
                val uiState = adapter.currentList.find { it.medicamento.id == medicamentoSelecionado.id }
                if (uiState != null) {
                    viewModel.markDoseAsTaken(medicamentoSelecionado, uiState)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateUI(uiStateList: List<MedicamentoUiState>) {
        val hadMedsBefore = medicamentos.isNotEmpty()
        medicamentos = uiStateList.map { it.medicamento }
        adapter.submitList(uiStateList)
        val searchQuery = binding.editTextSearch.text.toString()
        val isEmpty = uiStateList.isEmpty()
        binding.recyclerViewMedicamentos.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (isEmpty) {
            binding.emptyStateTitle.text = if (searchQuery.isBlank()) getString(R.string.no_meds_registered) else getString(R.string.no_search_results)
            binding.emptyStateSubtitle.text = if (searchQuery.isBlank()) getString(R.string.no_meds_registered_subtitle) else getString(R.string.no_search_results_subtitle, searchQuery)
        }
        if (args.isCaregiver) {
            binding.fabAddMedicamento.visibility = View.VISIBLE
            binding.fabAddMedicamento.text = getString(R.string.fab_add)
            binding.fabAddMedicamento.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_add)
            binding.fabAddMedicamento.setOnClickListener { navigateToAddMedicamento() }
            val shouldShowGuide = !userPreferences.hasSeenMedListGuide()
            if (shouldShowGuide) {
                view?.post {
                    if (isEmpty && searchQuery.isBlank()) {
                        showEmptyStateGuide()
                    } else if (!hadMedsBefore && uiStateList.isNotEmpty()) {
                        val firstViewHolder = binding.recyclerViewMedicamentos.findViewHolderForAdapterPosition(0)
                        firstViewHolder?.itemView?.let { showFirstMedicationGuide(it) }
                    }
                }
            }
        } else {
            if (medicamentos.isNotEmpty() && userPreferences.temPermissao(PermissaoTipo.REGISTRAR_DOSE)) {
                binding.fabAddMedicamento.visibility = View.VISIBLE
                binding.fabAddMedicamento.text = getString(R.string.fab_register_dose)
                binding.fabAddMedicamento.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_circle)
                binding.fabAddMedicamento.setOnClickListener { showMedicamentoSelectionDialog() }
            } else {
                binding.fabAddMedicamento.visibility = View.GONE
            }
        }
    }

    private fun showEmptyStateGuide() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed
            val userPreferences = UserPreferences(requireContext())
            TapTarget.forView(binding.fabAddMedicamento, getString(R.string.guide_empty_list_title), getString(R.string.guide_empty_list_subtitle, dependentName))
                .outerCircleColor(R.color.md_theme_primary)
                .targetCircleColor(R.color.white)
                .textColor(R.color.white)
                .cancelable(false)
                .tintTarget(true)
                .let {
                    TapTargetView.showFor(requireActivity(), it, object : TapTargetView.Listener() {
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view)
                            userPreferences.setMedListGuideSeen(true)
                            binding.fabAddMedicamento.performClick()
                        }
                        override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                            userPreferences.setMedListGuideSeen(true)
                        }
                    })
                }
        }, 300)
    }

    private fun showFirstMedicationGuide(itemView: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded || activity == null) return@postDelayed
            val userPreferences = UserPreferences(requireContext())
            val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
            val targets = mutableListOf<TapTarget>()

            targets.add(
                TapTarget.forView(itemView, getString(R.string.guide_first_med_details), getString(R.string.guide_first_med_details_subtitle))
                    .outerCircleColor(R.color.md_theme_primary)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.white)
                    .cancelable(false)
            )
            TapTargetSequence(requireActivity())
                .targets(targets)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() { userPreferences.setMedListGuideSeen(true) }
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                    override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setMedListGuideSeen(true) }
                }).start()
        }, 500)
    }

    private fun showDeleteConfirmationDialog(medicamento: Medicamento) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message, medicamento.nome))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteMedicamento(medicamento)
            }
            .create()
            .show()
    }

    private fun showUndoSnackbar(medicamento: Medicamento) {
        Snackbar.make(binding.root, getString(R.string.deleted_snackbar_message, medicamento.nome), Snackbar.LENGTH_LONG)
            .setAnchorView(binding.fabAddMedicamento)
            .setAction(getString(R.string.undo)) {
                viewModel.undoDeleteMedicamento(medicamento)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        viewModel.permanentlyDeleteMedicamento(medicamento)
                    }
                }
            })
            .show()
    }

    fun shareTodaysSchedule() {
        val scheduleText = viewModel.generateShareableScheduleForToday()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, scheduleText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_schedule_title))
        startActivity(shareIntent)
    }

    private fun showRefillStockDialog(medicamento: Medicamento) {
        val dialogBinding = DialogRefillStockBinding.inflate(LayoutInflater.from(context))
        dialogBinding.tilRefillAmount.hint = getString(R.string.refill_stock_hint, medicamento.unidadeDeEstoque)
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var selectedDate: LocalDate? = null
        dialogBinding.editTextExpirationDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_expiration_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
                dialogBinding.editTextExpirationDate.setText(selectedDate?.format(dateFormatter))
            }
            datePicker.show(childFragmentManager, "EXPIRATION_DATE_PICKER_REFILL")
        }
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.refill_stock_title, medicamento.nome))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(R.string.add) { _, _ ->
                val amountStr = dialogBinding.editTextRefillAmount.text.toString().replace(',', '.')
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(context, getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (selectedDate == null) {
                    Toast.makeText(context, getString(R.string.select_date), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val novoLote = EstoqueLote(
                    quantidade = amount,
                    quantidadeInicial = amount,
                    dataValidadeString = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
                viewModel.addStockLot(medicamento, novoLote)
                Toast.makeText(context, getString(R.string.stock_updated), Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}