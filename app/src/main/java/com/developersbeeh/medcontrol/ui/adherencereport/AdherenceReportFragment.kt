package com.developersbeeh.medcontrol.ui.adherencereport

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentAdherenceReportBinding
import com.developersbeeh.medcontrol.util.UiState
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdherenceReportFragment : Fragment() {

    private var _binding: FragmentAdherenceReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdherenceReportViewModel by viewModels()
    private val args: AdherenceReportFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdherenceReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Relatório de Adesão"

        setupCharts()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.chipGroupPeriod.setOnCheckedChangeListener { _, checkedId ->
            val period = when (checkedId) {
                R.id.chip7days -> ReportPeriod.DAYS_7
                R.id.chip90days -> ReportPeriod.DAYS_90
                else -> ReportPeriod.DAYS_30
            }
            viewModel.generateReport(period)
        }
        // ✅ BOTÃO DE TENTAR NOVAMENTE CONECTADO
        binding.errorStateLayout.btnRetry.setOnClickListener {
            viewModel.initialize(args.dependentId)
        }
    }

    private fun observeViewModel() {
        viewModel.reportData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.contentScrollView.visibility = View.GONE
                    binding.errorStateLayout.root.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE

                    if (state.data.totalDosesExpected == 0) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.contentScrollView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.contentScrollView.visibility = View.VISIBLE
                        updateUiWithData(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.contentScrollView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.GONE

                    // ✅ ACESSO SEGURO AOS COMPONENTES DO LAYOUT DE ERRO
                    binding.errorStateLayout.root.visibility = View.VISIBLE
                    binding.errorStateLayout.tvErrorMessage.text = state.message
                }
            }
        }
    }

    private fun updateUiWithData(data: AdherenceReportData) {
        binding.textViewOverallAdherence.text = "${data.overallAdherence}%"
        binding.textViewDoseCount.text = "${data.totalDosesTaken} de ${data.totalDosesExpected} doses tomadas"
        updateMedicationChart(data.byMedication)
        updateTimeOfDayChart(data.byTimeOfDay)
    }

    private fun setupCharts() {
        binding.chartByMedication.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            setPinchZoom(false)
            isDragEnabled = true
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                textColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
                setDrawGridLines(false)
                textColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }
            axisRight.isEnabled = false
        }

        binding.chartByTimeOfDay.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
                setDrawGridLines(false)
                textColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            }
            axisRight.isEnabled = false
        }
    }

    private fun updateMedicationChart(data: List<AdherenceByMedication>) {
        val entries = data.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.adherencePercentage.toFloat())
        }
        val labels = data.map { it.medicationName }

        val dataSet = BarDataSet(entries, "Adesão por Medicamento").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
            valueTextColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
            }
        }

        binding.chartByMedication.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chartByMedication.data = BarData(dataSet)
        binding.chartByMedication.invalidate()
        binding.chartByMedication.animateY(1000)
    }

    private fun updateTimeOfDayChart(data: List<AdherenceByTimeOfDay>) {
        val entries = data.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.adherencePercentage.toFloat())
        }
        val labels = data.map { it.periodName }

        val dataSet = BarDataSet(entries, "Adesão por Período").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_secondary)
            valueTextColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
            }
        }

        binding.chartByTimeOfDay.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.chartByTimeOfDay.data = BarData(dataSet)
        binding.chartByTimeOfDay.invalidate()
        binding.chartByTimeOfDay.animateY(1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}