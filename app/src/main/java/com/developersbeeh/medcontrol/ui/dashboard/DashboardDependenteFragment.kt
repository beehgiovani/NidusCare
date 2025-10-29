package com.developersbeeh.medcontrol.ui.dashboard

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.databinding.DialogPredictiveAnalysisBinding
import com.developersbeeh.medcontrol.databinding.FragmentDashboardDependenteBinding
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment
import com.developersbeeh.medcontrol.util.AgeCalculator
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class DashboardDependenteFragment : Fragment() {

    private var _binding: FragmentDashboardDependenteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardDependenteViewModel by viewModels()
    private val args: DashboardDependenteFragmentArgs by navArgs()

    private lateinit var userPreferences: UserPreferences
    private lateinit var remindersAdapter: DashboardRemindersAdapter
    private var loadingDialog: LoadingDialogFragment? = null

    // ✅ ROBUSTEZ: Locale definido para formatação
    private val locale = Locale("pt", "BR")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300
            scrimColor = Color.TRANSPARENT
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardDependenteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.transitionName = "dependent_card_transition_${args.dependentId}"

        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId)

        binding.recyclerViewCareManagement.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewHealthData.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewAdvancedTools.layoutManager = GridLayoutManager(context, 3)
        binding.recyclerViewProfileManagement.layoutManager = GridLayoutManager(context, 3)

        // ✅ CORREÇÃO: IDs acessados diretamente do binding principal
        binding.labelCareManagement.text = getString(R.string.care_management_title)
        binding.labelHealthData.text = getString(R.string.health_data_title)
        binding.labelAdvancedTools.text = getString(R.string.advanced_tools_title)
        binding.labelProfileManagement.text = getString(R.string.profile_management_title)
        binding.labelWeightTracker.text = getString(R.string.weight_tracker_title)
        // Os IDs 'labelImc' e 'labelGoal' não existem no XML principal, eles estão dentro do card de peso
        // Vou remover a tentativa de setar o texto deles aqui

        // Os IDs 'labelWeekly...' também estão dentro do card semanal
        // Vou remover a tentativa de setar o texto deles aqui

        setupAds()
        setupRemindersRecyclerView()
        observeViewModel()
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

    private fun setupRemindersRecyclerView() {
        remindersAdapter = DashboardRemindersAdapter()
        binding.layoutReminders.recyclerViewReminders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = remindersAdapter
        }
        binding.layoutReminders.headerReminders.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToReminders(args.dependentId)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.showLoading.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                loadingDialog = LoadingDialogFragment.newInstance(message)
                loadingDialog?.show(childFragmentManager, LoadingDialogFragment.TAG)
            }
        }

        viewModel.hideLoading.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                loadingDialog?.dismissAllowingStateLoss()
                loadingDialog = null
            }
        }

        viewModel.dependente.observe(viewLifecycleOwner) { dependente ->
            dependente?.let {
                if (binding.imageViewAvatar.visibility != View.VISIBLE) {
                    val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    binding.imageViewAvatar.visibility = View.VISIBLE
                    binding.textViewDependentName.visibility = View.VISIBLE
                    binding.textViewDependentAge.visibility = View.VISIBLE
                    binding.linearLayoutInfo.visibility = View.VISIBLE
                    binding.imageViewAvatar.startAnimation(fadeIn)
                    binding.textViewDependentName.startAnimation(fadeIn)
                    binding.textViewDependentAge.startAnimation(fadeIn)
                    binding.linearLayoutInfo.startAnimation(fadeIn)
                }

                (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.dashboard_title_prefix, it.nome)
                binding.textViewDependentName.text = it.nome

                val age = AgeCalculator.calculateAge(it.dataDeNascimento)
                binding.textViewDependentAge.text = if (age != null) getString(R.string.age_format_years, age) else getString(R.string.age_not_informed)

                binding.imageViewAvatar.load(it.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                    transformations(CircleCropTransformation())
                }

                binding.textViewBloodType.text = TipoSanguineo.valueOf(it.tipoSanguineo).displayName
                binding.textViewWeight.text = if (it.peso.isNotBlank()) getString(R.string.weight_format_kg, it.peso) else ""
                binding.textViewHeight.text = if (it.altura.isNotBlank()) getString(R.string.height_format_cm, it.altura) else ""
            }
        }

        viewModel.missedDoseAlert.observe(viewLifecycleOwner) { missedMeds ->
            if (missedMeds.isNotEmpty()) {
                if (binding.cardMissedDoseAlert.visibility != View.VISIBLE) {
                    binding.cardMissedDoseAlert.visibility = View.VISIBLE
                    binding.cardMissedDoseAlert.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
                }
                binding.textViewMissedDosesDetails.text = getString(R.string.missed_doses_details, missedMeds.joinToString(", "))
            } else {
                binding.cardMissedDoseAlert.visibility = View.GONE
            }
        }

        viewModel.summaryText.observe(viewLifecycleOwner) { summary ->
            if (summary.isNotBlank()) {
                if (binding.cardSummary.visibility != View.VISIBLE) {
                    binding.cardSummary.visibility = View.VISIBLE
                    binding.cardSummary.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in))
                }
                binding.textViewSummary.text = summary
            } else {
                binding.cardSummary.visibility = View.GONE
            }
        }

        viewModel.remindersSummary.observe(viewLifecycleOwner) { reminders ->
            val hasReminders = !reminders.isNullOrEmpty()
            if (hasReminders) {
                if (!binding.layoutReminders.cardReminders.isVisible) {
                    binding.layoutReminders.cardReminders.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in))
                }
                binding.layoutReminders.cardReminders.isVisible = true
                binding.layoutReminders.textViewNoReminders.isVisible = false
                binding.layoutReminders.recyclerViewReminders.isVisible = true
                remindersAdapter.submitList(reminders)
            } else {
                binding.layoutReminders.cardReminders.isVisible = true
                binding.layoutReminders.textViewNoReminders.isVisible = true
                binding.layoutReminders.textViewNoReminders.text = getString(R.string.reminders_empty_state)
                binding.layoutReminders.recyclerViewReminders.isVisible = false
            }
        }

        viewModel.imcResult.observe(viewLifecycleOwner) { updateWeightTrackerCard() }
        viewModel.weightGoalStatus.observe(viewLifecycleOwner) { updateWeightTrackerCard() }

        viewModel.weeklySummaryState.observe(viewLifecycleOwner) { summary ->
            if (summary != null) {
                if (binding.cardWeeklySummary.visibility != View.VISIBLE) {
                    binding.cardWeeklySummary.visibility = View.VISIBLE
                    binding.cardWeeklySummary.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in))
                }
                // ✅ CORREÇÃO: Acessa os IDs dentro do cardWeeklySummary
                binding.textViewWeeklyHydration.text = getString(R.string.weekly_summary_hydration, summary.mediaDiariaAguaMl / 1000.0)
                binding.textViewWeeklyActivity.text = getString(R.string.weekly_summary_activity, summary.totalMinutosAtividade)
            } else {
                binding.cardWeeklySummary.visibility = View.GONE
            }
        }

        viewModel.careManagementCategories.observe(viewLifecycleOwner) { categories ->
            val adapter = DashboardCategoryAdapter(categories, ::navigateToCategory)
            binding.recyclerViewCareManagement.adapter = adapter
        }

        viewModel.healthDataCategories.observe(viewLifecycleOwner) { categories ->
            val adapter = DashboardCategoryAdapter(categories, ::navigateToCategory)
            binding.recyclerViewHealthData.adapter = adapter
        }

        viewModel.profileManagementCategories.observe(viewLifecycleOwner) { categories ->
            val adapter = DashboardCategoryAdapter(categories, ::navigateToCategory)
            binding.recyclerViewProfileManagement.adapter = adapter
            binding.labelProfileManagement.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.advancedToolsCategories.observe(viewLifecycleOwner) { categories ->
            val adapter = DashboardCategoryAdapter(categories, ::navigateToCategory)
            binding.recyclerViewAdvancedTools.adapter = adapter
            binding.labelAdvancedTools.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
            binding.recyclerViewAdvancedTools.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.showAnalysisPromptDialog.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { showPredictiveAnalysisDialog() }
        }

        viewModel.showEmergencyConfirmation.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { showEmergencyConfirmationDialog() }
        }

        viewModel.showCredentials.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { credentials ->
                showDependentCredentialsDialog(credentials.first, credentials.second)
            }
        }

        viewModel.showDeleteConfirmation.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val dependente = viewModel.dependente.value
                if (dependente != null) {
                    showDeleteConfirmationDialog(dependente)
                }
            }
        }
    }

    private fun updateWeightTrackerCard() {
        val imcResult = viewModel.imcResult.value
        val goalStatus = viewModel.weightGoalStatus.value
        // ✅ CORREÇÃO: Acessa o card de peso pelo ID correto
        val card = binding.cardWeightTracker

        if (imcResult == null && goalStatus == null) {
            card.visibility = View.GONE
            return
        }
        if (card.visibility != View.VISIBLE) {
            card.visibility = View.VISIBLE
            card.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in))
        }

        // ✅ CORREÇÃO: Acessa os IDs de UI dentro do card de peso
        if (imcResult != null) {
            binding.layoutImc.visibility = View.VISIBLE
            binding.textViewImcValue.text = String.format(locale, "%.1f", imcResult.value)
            binding.textViewImcClassification.text = imcResult.classification
            binding.textViewImcClassification.setTextColor(ContextCompat.getColor(requireContext(), imcResult.color))
        } else {
            binding.layoutImc.visibility = View.INVISIBLE
        }
        if (goalStatus != null) {
            binding.progressWeightGoal.visibility = View.VISIBLE
            binding.textViewWeightGoalProgress.visibility = View.VISIBLE
            binding.textViewWeightGoalProgress.text = goalStatus.progressText
            binding.progressWeightGoal.progress = goalStatus.progress
            binding.progressWeightGoal.setIndicatorColor(ContextCompat.getColor(requireContext(), goalStatus.color))
        } else {
            binding.progressWeightGoal.visibility = View.GONE
            binding.textViewWeightGoalProgress.visibility = View.GONE
        }
    }

    private fun navigateToCategory(category: DashboardCategory) {
        val isCaregiver = userPreferences.getIsCaregiver()
        val dependentId = args.dependentId
        val dependentName = args.dependentName
        val dependentDob = viewModel.dependente.value?.dataDeNascimento ?: ""

        val action = when (category.actionId) {
            ACTION_ID_EMERGENCY -> { viewModel.onEmergencyClicked(); null }
            ACTION_ID_VIEW_CREDENTIALS -> { viewModel.onViewCredentialsClicked(); null }
            ACTION_ID_TOGGLE_ALARM -> { viewModel.onToggleAlarmClicked(); null }
            ACTION_ID_DELETE_DEPENDENT -> { viewModel.onDeleteDependentClicked(); null }
            ACTION_ID_SHOW_ANALYSIS_DIALOG -> { showPredictiveAnalysisDialog(); null }
            R.id.listMedicamentosFragment -> NavGraphDirections.actionGlobalToMedications(args.dependentId, isCaregiver)
            R.id.wellbeingDiaryFragment -> NavGraphDirections.actionGlobalToWellbeingDiaryFragment(dependentId, dependentName)
            R.id.healthNotesFragment -> NavGraphDirections.actionGlobalToHealthNotes(args.dependentId, args.dependentName)
            R.id.healthScheduleFragment -> NavGraphDirections.actionGlobalToHealthScheduleFragment(args.dependentId, args.dependentName)
            R.id.action_global_to_vaccinationCardFragment -> NavGraphDirections.actionGlobalToVaccinationCardFragment(dependentId, dependentName, dependentDob)
            R.id.healthDocumentsFragment -> NavGraphDirections.actionGlobalToHealthDocumentsFragment(args.dependentId, args.dependentName)
            R.id.timelineFragment -> NavGraphDirections.actionGlobalToTimelineFragment(args.dependentId, args.dependentName)
            R.id.farmacinhaFragment -> NavGraphDirections.actionGlobalToFarmacinhaFragment(dependentId, dependentName)
            R.id.action_global_to_educationCenterFragment -> NavGraphDirections.actionGlobalToEducationCenterFragment()
            R.id.action_global_to_achievementsFragment -> NavGraphDirections.actionGlobalToAchievementsFragment(dependentId, dependentName)
            R.id.action_global_to_chatFragment -> NavGraphDirections.actionGlobalToChatFragment(dependentId, dependentName)
            R.id.action_global_to_cycleTrackerFragment -> NavGraphDirections.actionGlobalToCycleTrackerFragment(dependentId, dependentName)
            R.id.action_global_to_geriatricCareFragment -> NavGraphDirections.actionGlobalToGeriatricCareFragment(dependentId, dependentName)
            R.id.reportsFragment -> NavGraphDirections.actionGlobalToReportsFragment(args.dependentId, args.dependentName)
            R.id.action_global_to_prescriptionScannerFragment -> NavGraphDirections.actionGlobalToPrescriptionScannerFragment(dependentId, dependentName)
            R.id.action_global_to_archivedMedicationsFragment -> NavGraphDirections.actionGlobalToArchivedMedicationsFragment(args.dependentId, args.dependentName)
            R.id.action_global_to_doseHistoryFragment -> NavGraphDirections.actionGlobalToDoseHistoryFragment(args.dependentId)
            R.id.action_global_to_premiumPlansFragment -> NavGraphDirections.actionGlobalToPremiumPlansFragment()
            R.id.action_global_to_addEditDependentFragment -> NavGraphDirections.actionGlobalToAddEditDependentFragment(viewModel.dependente.value)
            R.id.action_global_to_manageCaregiversFragment -> NavGraphDirections.actionGlobalToManageCaregiversFragment(viewModel.dependente.value)
            R.id.action_global_to_healthGoalsFragment -> NavGraphDirections.actionGlobalToHealthGoalsFragment(dependentId)
            R.id.action_global_to_reminders -> NavGraphDirections.actionGlobalToReminders(dependentId)
            R.id.action_global_to_pharmacySelectionFragment -> NavGraphDirections.actionGlobalToPharmacySelectionFragment()
            else -> null
        }
        action?.let { findNavController().navigate(it) }
    }

    private fun showPredictiveAnalysisDialog() {
        val dialogBinding = DialogPredictiveAnalysisBinding.inflate(LayoutInflater.from(context))
        val dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format_dd_mm_yyyy))
        var analysisStartDate: LocalDate = LocalDate.now().minusDays(29)
        var analysisEndDate: LocalDate = LocalDate.now()

        fun updateDateRangeText() {
            val startFormatted = analysisStartDate.format(dateFormatter)
            val endFormatted = analysisEndDate.format(dateFormatter)
            dialogBinding.textViewDateRange.text = "$startFormatted – $endFormatted"
        }

        dialogBinding.chipGroupDateRange.check(R.id.chip30Days)
        updateDateRangeText()

        dialogBinding.chipToday.setOnClickListener {
            analysisStartDate = LocalDate.now()
            analysisEndDate = LocalDate.now()
            updateDateRangeText()
        }
        dialogBinding.chip7Days.setOnClickListener {
            analysisStartDate = LocalDate.now().minusDays(6)
            analysisEndDate = LocalDate.now()
            updateDateRangeText()
        }
        dialogBinding.chip30Days.setOnClickListener {
            analysisStartDate = LocalDate.now().minusDays(29)
            analysisEndDate = LocalDate.now()
            updateDateRangeText()
        }
        dialogBinding.chipCustomRange.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.dialog_select_period_title))
                .setSelection(
                    androidx.core.util.Pair(
                        analysisStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        analysisEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    )
                )
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                analysisStartDate = Instant.ofEpochMilli(selection.first).atZone(ZoneOffset.UTC).toLocalDate()
                analysisEndDate = Instant.ofEpochMilli(selection.second).atZone(ZoneOffset.UTC).toLocalDate()
                updateDateRangeText()
            }
            datePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.dialog_button_generate_analysis), null)
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val symptoms = dialogBinding.editTextSymptoms.text.toString().trim()
                if (symptoms.isEmpty()) {
                    dialogBinding.tilSymptoms.error = getString(R.string.error_field_required)
                    return@setOnClickListener
                }

                dialog.dismiss()

                lifecycleScope.launch {
                    val result = viewModel.getPredictiveAnalysis(
                        symptoms = symptoms,
                        startDate = analysisStartDate,
                        endDate = analysisEndDate,
                        includeDoseHistory = dialogBinding.switchDoseHistory.isChecked,
                        includeHealthNotes = dialogBinding.switchHealthNotes.isChecked,
                        includeContinuousMeds = dialogBinding.switchContinuousMeds.isChecked
                    )
                    result.onSuccess { analysisText ->
                        viewModel.saveAnalysisToHistory(symptoms, analysisText)
                        val action = NavGraphDirections.actionGlobalToAnalysisResultFragment(
                            dependentId = args.dependentId,
                            dependentName = args.dependentName,
                            prompt = symptoms,
                            analysisResult = analysisText
                        )
                        findNavController().navigate(action)
                    }.onFailure { error ->
                        Toast.makeText(context, getString(R.string.error_generating_analysis, error.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showDependentCredentialsDialog(code: String, password: String) {
        val message = getString(R.string.dialog_message_credentials, args.dependentName, code, password)
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_title_credentials))
            .setMessage(message)
            .setPositiveButton(getString(R.string.understand), null)
            .setNeutralButton(getString(R.string.button_copy)) { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.credentials_label), "Código: $code\nSenha: $password")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, getString(R.string.credentials_copied), Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog(dependente: Dependente) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_title_confirm_delete))
            .setMessage(getString(R.string.dialog_message_confirm_delete, dependente.nome))
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton(getString(R.string.button_delete)) { _, _ ->
                viewModel.confirmDeleteDependent()
                findNavController().popBackStack(R.id.caregiverDashboardFragment, false)
            }
            .create()
            .show()
    }

    private fun showEmergencyConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_title_emergency_alert))
            .setMessage(getString(R.string.dialog_message_emergency_alert))
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton(getString(R.string.button_send_alert)) { _, _ ->
                viewModel.confirmEmergencyAlert()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}