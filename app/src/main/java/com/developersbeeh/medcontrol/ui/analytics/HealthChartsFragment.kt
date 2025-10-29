// src/main/java/com/developersbeeh/medcontrol/ui/analytics/HealthChartsFragment.kt

package com.developersbeeh.medcontrol.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentHealthChartsBinding

import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HealthChartsFragment : Fragment() {

    private var _binding: FragmentHealthChartsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HealthChartsViewModel by viewModels()
    private val args: HealthChartsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)
        setupChart()
        setupListeners()
        observeViewModel()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = false
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
                valueFormatter = object : ValueFormatter() {
                    private val formatter = DateTimeFormatter.ofPattern("dd/MM")
                    override fun getFormattedValue(value: Float): String {
                        return Instant.ofEpochSecond(value.toLong()).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
                    }
                }
            }
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
            axisRight.isEnabled = false
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
        }
    }

    private fun setupListeners() {
        binding.chipGroupChartType.setOnCheckedStateChangeListener { group, checkedIds ->
            val type = when (checkedIds.firstOrNull()) {
                R.id.chipBloodSugar -> ChartType.BLOOD_SUGAR
                R.id.chipWeight -> ChartType.WEIGHT
                R.id.chipTemperature -> ChartType.TEMPERATURE
                else -> ChartType.BLOOD_PRESSURE
            }
            viewModel.selectChartType(type)
        }

        binding.chipGroupPeriod.setOnCheckedStateChangeListener { group, checkedIds ->
            val days = when (checkedIds.firstOrNull()) {
                R.id.chip7days -> 7L
                R.id.chip90days -> 90L
                else -> 30L
            }
            viewModel.setPeriod(days)
        }
    }

    private fun observeViewModel() {
        viewModel.chartData.observe(viewLifecycleOwner) { data ->
            if (data == null || data.line1.isNullOrEmpty()) {
                binding.lineChart.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.lineChart.clear()
            } else {
                binding.lineChart.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE

                val primaryColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
                val secondaryColor = ContextCompat.getColor(requireContext(), R.color.md_theme_secondary)
                val goalColor = ContextCompat.getColor(requireContext(), R.color.success_green)

                val dataSet1 = LineDataSet(data.line1, getLabelForSelectedChart(line1 = true)).apply {
                    color = primaryColor
                    setCircleColor(primaryColor)
                    valueTextColor = primaryColor
                }

                val lineData = LineData(dataSet1)

                if (!data.line2.isNullOrEmpty()) {
                    val label = getLabelForSelectedChart(line1 = false)
                    val dataSet2 = LineDataSet(data.line2, label).apply {
                        if (label == "Meta de Peso") {
                            color = goalColor
                            enableDashedLine(10f, 5f, 0f)
                            setDrawCircles(false)
                            setDrawValues(false)
                        } else {
                            color = secondaryColor
                            setCircleColor(secondaryColor)
                            valueTextColor = secondaryColor
                        }
                    }
                    lineData.addDataSet(dataSet2)
                }

                binding.lineChart.data = lineData
                binding.lineChart.invalidate()
                binding.lineChart.animateX(1000)
            }
        }
    }

    private fun getLabelForSelectedChart(line1: Boolean): String {
        return when(binding.chipGroupChartType.checkedChipId) {
            R.id.chipBloodSugar -> "Glicemia"
            R.id.chipWeight -> if (line1) "Peso" else "Meta de Peso"
            R.id.chipTemperature -> "Temperatura"
            else -> if (line1) "Sistólica" else "Diastólica"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}