package com.developersbeeh.medcontrol.ui.weight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.HealthNote
import com.developersbeeh.medcontrol.databinding.FragmentWeightTrackerBinding
import com.developersbeeh.medcontrol.util.UiState
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WeightTrackerFragment : Fragment() {

    private var _binding: FragmentWeightTrackerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightTrackerViewModel by viewModels()
    private val args: WeightTrackerFragmentArgs by navArgs()
    private val adapter by lazy { WeightHistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        binding.recyclerViewWeightHistory.adapter = adapter
        binding.recyclerViewWeightHistory.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.emptyState.root.isVisible = state is UiState.Success && state.data.history.isEmpty()
            binding.recyclerViewWeightHistory.isVisible = state is UiState.Success

            when (state) {
                is UiState.Success -> {
                    if (state.data.history.isEmpty()) {
                        binding.emptyState.tvErrorMessage.text = getString(R.string.empty_state_no_weight_records)
                    } else {
                        setupChart(state.data.history, state.data.dependent.pesoMeta)
                        adapter.submitList(state.data.history.reversed())
                    }
                }
                is UiState.Error -> {
                    binding.emptyState.root.isVisible = true
                    binding.emptyState.tvErrorMessage.text = state.message
                }
                else -> {}
            }
        }
    }

    private fun setupChart(records: List<HealthNote>, goalStr: String) {
        val entries = ArrayList<Entry>()
        val xAxisLabels = ArrayList<String>()
        val dayFormatter = DateTimeFormatter.ofPattern("dd/MM")

        records.forEachIndexed { index, record ->
            val weight = record.values["weight"]?.replace(',', '.')?.toFloatOrNull()
            if (weight != null) {
                entries.add(Entry(index.toFloat(), weight))
                xAxisLabels.add(record.timestamp.toLocalDate().format(dayFormatter))
            }
        }

        if(entries.isEmpty()) return

        val dataSet = LineDataSet(entries, getString(R.string.chart_label_weight_evolution)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary))
            circleRadius = 4f
            setDrawCircleHole(false)
            lineWidth = 2.5f
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
        }

        binding.lineChartWeight.data = LineData(dataSet)
        configureChartAppearance(goalStr)
        binding.lineChartWeight.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        binding.lineChartWeight.invalidate()
    }

    private fun configureChartAppearance(goalStr: String) {
        binding.lineChartWeight.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(context, R.color.md_theme_outline)
                textColor = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant)

                removeAllLimitLines()
                val goal = goalStr.replace(',', '.').toFloatOrNull()
                if (goal != null && goal > 0) {
                    val goalLine = LimitLine(goal, getString(R.string.chart_label_goal)).apply {
                        lineWidth = 2f
                        lineColor = ContextCompat.getColor(context, R.color.md_theme_tertiary)
                        textColor = ContextCompat.getColor(context, R.color.md_theme_tertiary)
                        labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                        textSize = 10f
                    }
                    addLimitLine(goalLine)
                }
            }
            axisRight.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}