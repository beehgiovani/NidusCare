package com.developersbeeh.medcontrol.ui.hydration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Hidratacao
import com.developersbeeh.medcontrol.databinding.FragmentHydrationTrackerBinding
import com.developersbeeh.medcontrol.util.UiState
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HydrationTrackerFragment : Fragment() {

    private var _binding: FragmentHydrationTrackerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HydrationTrackerViewModel by viewModels()
    private val args: HydrationTrackerFragmentArgs by navArgs()
    private val adapter by lazy { HydrationHistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        // ✅ REFATORADO: Seta o título da tela
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.hydration_history_title)

        binding.recyclerViewHydrationHistory.adapter = adapter
        binding.recyclerViewHydrationHistory.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.emptyState.root.isVisible = state is UiState.Success && state.data.history.isEmpty()
            binding.recyclerViewHydrationHistory.isVisible = state is UiState.Success

            when (state) {
                is UiState.Success -> {
                    if (state.data.history.isEmpty()) {
                        binding.emptyState.tvErrorMessage.text = getString(R.string.empty_state_no_hydration_records)
                    } else {
                        setupChart(state.data.history, state.data.dependent.metaHidratacaoMl.toFloat())
                        adapter.submitList(state.data.history)
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

    private fun setupChart(records: List<Hidratacao>, goal: Float) {
        val sevenDaysAgo = LocalDate.now().minusDays(6)
        val dailyTotals = records
            .filter { !it.timestamp.toLocalDate().isBefore(sevenDaysAgo) }
            .groupBy { it.timestamp.toLocalDate() }
            .mapValues { (_, entries) -> entries.sumOf { it.quantidadeMl } }

        val entries = ArrayList<BarEntry>()
        val xAxisLabels = ArrayList<String>()
        val dayFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format_dd_mm))

        for (i in 0..6) {
            val date = sevenDaysAgo.plusDays(i.toLong())
            val totalMl = dailyTotals[date]?.toFloat() ?: 0f
            entries.add(BarEntry(i.toFloat(), totalMl))
            xAxisLabels.add(date.format(dayFormatter))
        }

        val dataSet = BarDataSet(entries, getString(R.string.hydration_chart_label)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
            setDrawValues(false)
        }

        binding.barChartHydration.data = BarData(dataSet)
        configureChartAppearance(goal)
        binding.barChartHydration.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        binding.barChartHydration.invalidate()
    }

    private fun configureChartAppearance(goal: Float) {
        binding.barChartHydration.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant)
            }
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(context, R.color.md_theme_outline)
                textColor = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant)

                removeAllLimitLines()

                val goalLine = LimitLine(goal, getString(R.string.chart_label_goal)).apply {
                    lineWidth = 2f
                    lineColor = ContextCompat.getColor(context, R.color.md_theme_tertiary)
                    textColor = ContextCompat.getColor(context, R.color.md_theme_tertiary)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    textSize = 10f
                }
                addLimitLine(goalLine)
            }
            axisRight.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}