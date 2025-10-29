package com.developersbeeh.medcontrol.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.R as MaterialR
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.FragmentMedicationAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class MedicationAnalyticsFragment : Fragment() {

    private var _binding: FragmentMedicationAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MedicationAnalyticsViewModel by viewModels()
    private val args: MedicationAnalyticsFragmentArgs by navArgs()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")

    private lateinit var dependentName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)

        dependentName = args.dependentName

        setupWeeklyChart()
        setupMonthlyChart()
        observeViewModel()
    }

    private fun setupWeeklyChart() {
        binding.chartWeeklyAdherence.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
                valueFormatter = IndexAxisValueFormatter(emptyArray())
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
            }
            axisRight.isEnabled = false
        }
    }

    private fun setupMonthlyChart() {
        binding.chartMonthlyAdherence.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = false
            setPinchZoom(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
                valueFormatter = IndexAxisValueFormatter(emptyArray())
            }
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 105f
                textColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
            }
            axisRight.isEnabled = false
        }
    }

    private fun observeViewModel() {
        viewModel.analyticsData.observe(viewLifecycleOwner) { analyticsData ->
            // --- MUDANÇA: PASSA O NOVO OBJETO WeeklyChartData ---
            if (analyticsData.weeklyData.entries.isNotEmpty()) {
                updateWeeklyChart(analyticsData.weeklyData)
            } else {
                binding.chartWeeklyAdherence.clear()
            }

            // Atualiza o gráfico mensal
            if (analyticsData.monthlyData.isNotEmpty()) {
                updateMonthlyChart(analyticsData.monthlyData, analyticsData.monthlyLabels)
            } else {
                binding.chartMonthlyAdherence.clear()
            }

            // --- MUDANÇA: ATUALIZA O RESUMO DIÁRIO COM A LÓGICA DE SUPERDOSAGEM ---
            val summary = analyticsData.dailySummary
            if (summary != null && summary.dosesExpected > 0) {
                binding.cardDailySummary.visibility = View.VISIBLE
                val personalizedText = if (summary.dosesTaken > summary.dosesExpected) {
                    "Superdosagem hoje: ${summary.dosesTaken} de ${summary.dosesExpected} doses esperadas."
                } else {
                    "Aderência hoje: ${summary.dosesTaken} de ${summary.dosesExpected} doses esperadas."
                }
                binding.textViewDailySummary.text = personalizedText
                binding.progressBarDaily.max = summary.dosesExpected
                binding.progressBarDaily.progress = summary.dosesTaken.coerceAtMost(summary.dosesExpected)
            } else {
                binding.cardDailySummary.visibility = View.GONE
            }
            // --- FIM DA MUDANÇA ---

            // Atualiza as estatísticas esporádicas
            val stats = analyticsData.sporadicStats
            if (stats.isNullOrEmpty()) {
                binding.cardSporadicStats.visibility = View.GONE
            } else {
                binding.cardSporadicStats.visibility = View.VISIBLE
                binding.linearLayoutSporadicStats.removeAllViews()
                stats.forEach { stat ->
                    val textView = TextView(context).apply {
                        text = "• ${stat.name}: Tomado ${stat.timesTaken} ${if (stat.timesTaken > 1) "vezes" else "vez"}"
                        TextViewCompat.setTextAppearance(this, MaterialR.style.TextAppearance_Material3_BodyMedium)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant))
                        setPadding(0, 8, 0, 8)
                    }
                    binding.linearLayoutSporadicStats.addView(textView)
                }
            }

            // Atualiza as estatísticas de dose variável
            val variableStats = analyticsData.variableDoseStats
            if (variableStats.isNullOrEmpty()) {
                binding.cardVariableDoseStats.visibility = View.GONE
            } else {
                binding.cardVariableDoseStats.visibility = View.VISIBLE
                binding.linearLayoutVariableDoseStats.removeAllViews()
                variableStats.forEach { stat ->
                    val textView = TextView(context).apply {
                        val avg = String.format(Locale.getDefault(), "%.1f", stat.doseMedia)
                        val min = String.format(Locale.getDefault(), "%.1f", stat.doseMin)
                        val max = String.format(Locale.getDefault(), "%.1f", stat.doseMax)
                        text = "• ${stat.nome}: Média ${avg} (Mín ${min}, Máx ${max}) ${stat.unidade}"
                        TextViewCompat.setTextAppearance(this, MaterialR.style.TextAppearance_Material3_BodyMedium)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant))
                        setPadding(0, 8, 0, 8)
                    }
                    binding.linearLayoutVariableDoseStats.addView(textView)
                }
            }

            updateOverallVisibility(analyticsData)
        }
    }

    // --- MUDANÇA: FUNÇÃO DE ATUALIZAÇÃO DO GRÁFICO SEMANAL ATUALIZADA ---
    private fun updateWeeklyChart(weeklyData: WeeklyChartData) {
        val barEntries = weeklyData.entries.map {
            // A barra nunca passará de 100% no gráfico para não sair da tela
            BarEntry(it.index, it.adherencePercentage.coerceAtMost(100f))
        }

        // Cria uma lista de cores baseada na informação de superdosagem
        val colors = weeklyData.entries.map {
            if (it.isOverdosed) {
                ContextCompat.getColor(requireContext(), R.color.error_red) // Vermelho para superdosagem
            } else {
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary) // Cor padrão
            }
        }

        val dataSet = BarDataSet(barEntries, "Aderência Diária").apply {
            setColors(colors) // Define a lista de cores para as barras
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
            }
        }
        val data = BarData(dataSet)
        data.barWidth = 0.6f
        binding.chartWeeklyAdherence.data = data
        binding.chartWeeklyAdherence.xAxis.valueFormatter = IndexAxisValueFormatter(weeklyData.labels.map { it.format(dateFormatter) })
        binding.chartWeeklyAdherence.animateY(1000)
    }
    // --- FIM DA MUDANÇA ---


    private fun updateMonthlyChart(entries: List<Entry>, labels: List<LocalDate>) {
        val dataSet = LineDataSet(entries, "Aderência Mensal").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_secondary)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
            valueTextSize = 10f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.md_theme_secondaryContainer)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val data = LineData(dataSet)
        binding.chartMonthlyAdherence.data = data
        binding.chartMonthlyAdherence.xAxis.valueFormatter = IndexAxisValueFormatter(labels.map { it.format(dateFormatter) })
        binding.chartMonthlyAdherence.animateX(1000)
    }

    private fun updateOverallVisibility(analyticsData: AnalyticsData) {
        val hasAdherenceData = analyticsData.dailySummary?.dosesExpected ?: 0 > 0
        val hasSporadicData = !analyticsData.sporadicStats.isNullOrEmpty()
        val hasVariableDoseData = !analyticsData.variableDoseStats.isNullOrEmpty()

        if (hasAdherenceData || hasSporadicData || hasVariableDoseData) {
            binding.emptyStateLayout.visibility = View.GONE
            binding.scrollViewAnalytics.visibility = View.VISIBLE
        } else {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.scrollViewAnalytics.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}