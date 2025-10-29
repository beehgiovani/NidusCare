// src/main/java/com/developersbeeh/medcontrol/ui/reports/ReportsFragment.kt
package com.developersbeeh.medcontrol.ui.reports

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.databinding.FragmentReportsBinding
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()
    private val args: ReportsFragmentArgs by navArgs()

    private var startDate: LocalDate = LocalDate.now().minusDays(29)
    private var endDate: LocalDate = LocalDate.now()
    private var isPremium = false
    private lateinit var userPreferences: UserPreferences
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        viewModel.initialize(args.dependentId, args.dependentName)

        val title = "Gerar Relatório para ${args.dependentName}"
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Gerar Relatório"
        binding.textViewTitle.text = title

        updateDateTexts()
        setupListeners()
        observeViewModel()
        setupPremiumFeatures()

        showGuideIfFirstTime()
    }

    private fun setupPremiumFeatures() {
        isPremium = userPreferences.isPremium()
        binding.chipDetailedReport.isEnabled = isPremium

        if (!isPremium) {
            binding.chipGroupReportType.check(R.id.chipSimpleReport)
            updateUiForReportType(isDetailed = false)
        }
    }

    private fun setupListeners() {
        binding.editTextStartDate.setOnClickListener { showDatePicker(isStartDate = true) }
        binding.tilStartDate.setEndIconOnClickListener { showDatePicker(isStartDate = true) }
        binding.editTextEndDate.setOnClickListener { showDatePicker(isStartDate = false) }
        binding.tilEndDate.setEndIconOnClickListener { showDatePicker(isStartDate = false) }

        binding.chipGroupReportType.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.chipDetailedReport && !isPremium) {
                group.check(R.id.chipSimpleReport)
                Toast.makeText(context, "Relatório Detalhado é um recurso Premium.", Toast.LENGTH_SHORT).show()
            } else {
                updateUiForReportType(checkedId == R.id.chipDetailedReport)
            }
        }

        binding.buttonGenerateReport.setOnClickListener {
            val logoDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logo)
            val logoBitmap = (logoDrawable as BitmapDrawable).bitmap

            val selectedNoteTypes = mutableSetOf<HealthNoteType>()
            if (binding.chipBloodPressure.isChecked) selectedNoteTypes.add(HealthNoteType.BLOOD_PRESSURE)
            if (binding.chipBloodSugar.isChecked) selectedNoteTypes.add(HealthNoteType.BLOOD_SUGAR)
            if (binding.chipWeight.isChecked) selectedNoteTypes.add(HealthNoteType.WEIGHT)
            if (binding.chipTemperature.isChecked) selectedNoteTypes.add(HealthNoteType.TEMPERATURE)
            if (binding.chipMoodAndSymptoms.isChecked) {
                selectedNoteTypes.add(HealthNoteType.SYMPTOM)
                selectedNoteTypes.add(HealthNoteType.MOOD)
            }

            val options = ReportOptions(
                includeDoseHistory = binding.checkDoseHistory.isChecked,
                includeAppointments = binding.checkAppointments.isChecked,
                includedNoteTypes = selectedNoteTypes,
                includeAdherenceSummary = binding.checkAdherenceSummary.isChecked,
                includeAdherenceChart = binding.checkAdherenceChart.isChecked
            )

            viewModel.generateReport(startDate, endDate, options, logoBitmap)
        }
    }

    private fun updateUiForReportType(isDetailed: Boolean) {
        binding.checkAdherenceSummary.isEnabled = isDetailed
        binding.checkAdherenceChart.isEnabled = isDetailed
        binding.chipBloodPressure.isEnabled = isDetailed
        binding.chipBloodSugar.isEnabled = isDetailed
        binding.chipWeight.isEnabled = isDetailed
        binding.chipTemperature.isEnabled = isDetailed
        binding.chipMoodAndSymptoms.isEnabled = isDetailed

        if (!isDetailed) {
            binding.checkAdherenceSummary.isChecked = false
            binding.checkAdherenceChart.isChecked = false
            binding.chipBloodPressure.isChecked = false
            binding.chipBloodSugar.isChecked = false
            binding.chipWeight.isChecked = false
            binding.chipTemperature.isChecked = false
            binding.chipMoodAndSymptoms.isChecked = false
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
                loadingDialog?.dismiss()
                loadingDialog = null
            }
        }

        viewModel.reportState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is ReportGenerationState.Success -> {
                        Toast.makeText(context, "Relatório gerado com sucesso!", Toast.LENGTH_SHORT).show()
                        sharePdf(state.file)
                    }
                    is ReportGenerationState.Error -> {
                        Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showGuideIfFirstTime() {
        if (!userPreferences.hasSeenReportGuide()) {
            binding.root.doOnPreDraw {
                val activity = activity ?: return@doOnPreDraw
                TapTargetSequence(activity)
                    .targets(
                        TapTarget.forView(binding.tilStartDate, "Defina o Período", "Escolha as datas de início e fim para o seu relatório.")
                            .style(),
                        TapTarget.forView(binding.chipDetailedReport, "Escolha o Tipo", "Gere um relatório Simples ou Detalhado (recurso Premium) com mais informações como gráficos e resumos.")
                            .style(),
                        TapTarget.forView(binding.buttonGenerateReport, "Gerar Relatório", "Quando estiver pronto, clique aqui para gerar e compartilhar o seu PDF.")
                            .style()
                    )
                    .listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() { userPreferences.setReportGuideSeen(true) }
                        override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                        override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setReportGuideSeen(true) }
                    }).start()
            }
        }
    }

    private fun TapTarget.style(): TapTarget {
        return this.outerCircleColor(R.color.md_theme_primary)
            .targetCircleColor(R.color.white)
            .titleTextColor(R.color.white)
            .descriptionTextColor(R.color.white)
            .cancelable(true)
            .tintTarget(false)
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val initialDate = if (isStartDate) startDate else endDate
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isStartDate) "Data de Início" else "Data de Fim")
            .setSelection(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            if (isStartDate) {
                startDate = selectedDate
            } else {
                endDate = selectedDate
            }
            updateDateTexts()
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER_${if (isStartDate) "START" else "END"}")
    }

    private fun updateDateTexts() {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))
        binding.editTextStartDate.setText(startDate.format(formatter))
        binding.editTextEndDate.setText(endDate.format(formatter))
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}