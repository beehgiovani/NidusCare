package com.developersbeeh.medcontrol.ui.cycletracker

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.databinding.CalendarDayCycleLayoutBinding
import com.developersbeeh.medcontrol.databinding.DialogAddDailyCycleLogBinding
import com.developersbeeh.medcontrol.databinding.FragmentCycleTrackerBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class CycleTrackerFragment : Fragment() {

    private var _binding: FragmentCycleTrackerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CycleTrackerViewModel by viewModels()
    private val args: CycleTrackerFragmentArgs by navArgs()

    // ✅ ROBUSTEZ: Formatters agora usam recursos de string
    private lateinit var monthTitleFormatter: DateTimeFormatter
    private lateinit var dateFormatter: DateTimeFormatter
    private lateinit var locale: Locale

    // ✅ ROBUSTEZ: Mapas para vincular Chips a Enums de forma segura
    private val flowChipMap = mutableMapOf<Int, FlowIntensity>()
    private val symptomChipMap = mutableMapOf<Int, Symptom>()
    private val moodChipMap = mutableMapOf<Int, Mood>()

    private var selectedDate: LocalDate? = null

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = CalendarDayCycleLayoutBinding.bind(view).calendarDayText
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    val oldDate = selectedDate
                    selectedDate = day.date
                    binding.calendarView.notifyDateChanged(day.date)
                    oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                    updateDetailsCard(day.date)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCycleTrackerBinding.inflate(inflater, container, false)

        // ✅ ROBUSTEZ: Inicializa formatters e locale aqui
        locale = Locale(getString(R.string.locale_pt), getString(R.string.locale_br))
        monthTitleFormatter = DateTimeFormatter.ofPattern(getString(R.string.calendar_month_year_format), locale)
        dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_format_dd_mm_yyyy))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.cycle_tracker_title, args.dependentName)

        setupCalendar()
        setupListeners()
        observeViewModel()
    }

    private fun setupCalendar() {
        val calendarView = binding.calendarView
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = java.time.DayOfWeek.SUNDAY)

        binding.weekDaysHeader.children.forEachIndexed { index, view ->
            (view as TextView).text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, locale).uppercase()
        }

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()
                updateDayAppearance(day, container)
            }
        }

        calendarView.monthScrollListener = { month ->
            binding.textViewMonthYear.text = month.yearMonth.format(monthTitleFormatter).replaceFirstChar { it.uppercase() }
        }
    }

    private fun setupListeners() {
        binding.buttonNextMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let { it.yearMonth.plusMonths(1) }?.let { binding.calendarView.smoothScrollToMonth(it) }
        }
        binding.buttonPreviousMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let { it.yearMonth.minusMonths(1) }?.let { binding.calendarView.smoothScrollToMonth(it) }
        }
        binding.buttonLogToday.setOnClickListener {
            val dateToLog = selectedDate ?: LocalDate.now()
            showDailyLogDialog(dateToLog)
        }
        binding.buttonHistory.setOnClickListener {
            findNavController().navigate(R.id.action_cycleTrackerFragment_to_cycleHistoryFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state == null) return@observe

            if (binding.cardHeader.visibility == View.INVISIBLE) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in)
                binding.cardHeader.startAnimation(animation)
                binding.cardHeader.visibility = View.VISIBLE
            }

            binding.calendarView.notifyCalendarChanged()
            binding.textViewPrediction.text = state.predictionText
            binding.textViewCurrentPhase.text = state.currentPhase

            // Atualiza o card de detalhes para a data selecionada ou para hoje
            updateDetailsCard(selectedDate ?: LocalDate.now())
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDailyLogDialog(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            Toast.makeText(context, getString(R.string.error_cannot_log_future_date), Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddDailyCycleLogBinding.inflate(layoutInflater)
        val existingLog = viewModel.getLogForDate(date)

        // Limpa os mapas para evitar IDs duplicados
        flowChipMap.clear()
        symptomChipMap.clear()
        moodChipMap.clear()

        dialogBinding.editTextNotes.setText(existingLog.notes)

        // ✅ ROBUSTEZ: Cria chips dinamicamente e os mapeia para os Enums
        FlowIntensity.values().forEach {
            val chip = (layoutInflater.inflate(R.layout.chip_choice, dialogBinding.chipGroupFlow, false) as Chip).apply {
                text = it.getDisplayName(requireContext())
                isChecked = existingLog.flow == it
                id = View.generateViewId()
                flowChipMap[id] = it // Mapeia o ID ao Enum
            }
            dialogBinding.chipGroupFlow.addView(chip)
        }

        Symptom.values().forEach {
            val chip = (layoutInflater.inflate(R.layout.chip_filter, dialogBinding.chipGroupSymptoms, false) as Chip).apply {
                text = it.getDisplayName(requireContext())
                isChecked = existingLog.symptoms.contains(it)
                id = View.generateViewId()
                symptomChipMap[id] = it // Mapeia o ID ao Enum
            }
            dialogBinding.chipGroupSymptoms.addView(chip)
        }

        Mood.values().forEach {
            val chip = (layoutInflater.inflate(R.layout.chip_choice, dialogBinding.chipGroupMood, false) as Chip).apply {
                text = it.getDisplayName(requireContext())
                isChecked = existingLog.mood == it
                id = View.generateViewId()
                moodChipMap[id] = it // Mapeia o ID ao Enum
            }
            dialogBinding.chipGroupMood.addView(chip)
        }

        dialogBinding.switchSexualActivity.isChecked = existingLog.hadSexualActivity
        dialogBinding.switchProtectedSex.isVisible = existingLog.hadSexualActivity
        dialogBinding.switchProtectedSex.isChecked = existingLog.wasProtected == true

        dialogBinding.switchSexualActivity.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.switchProtectedSex.isVisible = isChecked
            if (!isChecked) dialogBinding.switchProtectedSex.isChecked = false
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_title_cycle_log_for_date, date.format(dateFormatter)))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.save)) { _, _ ->

                // ✅ ROBUSTEZ: Busca os Enums pelo ID do Chip checado
                val selectedFlow = flowChipMap[dialogBinding.chipGroupFlow.checkedChipId] ?: existingLog.flow

                val selectedSymptoms = dialogBinding.chipGroupSymptoms.checkedChipIds.mapNotNull { id ->
                    symptomChipMap[id]
                }

                val selectedMood = moodChipMap[dialogBinding.chipGroupMood.checkedChipId]

                val hadActivity = dialogBinding.switchSexualActivity.isChecked
                val wasProtected = if (hadActivity) dialogBinding.switchProtectedSex.isChecked else null
                val notesText = dialogBinding.editTextNotes.text.toString()

                val newLog = existingLog.copy(
                    dateString = date.toString(),
                    flow = selectedFlow,
                    symptoms = selectedSymptoms,
                    mood = selectedMood,
                    hadSexualActivity = hadActivity,
                    wasProtected = wasProtected,
                    notes = notesText
                )
                viewModel.saveDailyLog(newLog)
            }
            .show()
    }

    private fun updateDetailsCard(date: LocalDate) {
        val log = viewModel.getLogForDate(date)
        val card = binding.detailsCard

        binding.textViewDetailsDate.text = date.format(dateFormatter)

        val hasFlow = log.flow != FlowIntensity.NONE
        binding.labelFlow.isVisible = hasFlow
        binding.textViewDetailsFlow.isVisible = hasFlow
        binding.textViewDetailsFlow.text = if (hasFlow) log.flow.getDisplayName(requireContext()) else ""

        val hasSymptoms = log.symptoms.isNotEmpty()
        binding.labelSymptoms.isVisible = hasSymptoms
        binding.textViewDetailsSymptoms.isVisible = hasSymptoms
        binding.textViewDetailsSymptoms.text = if (hasSymptoms) log.symptoms.joinToString(", ") { it.getDisplayName(requireContext()) } else ""

        val hasMood = log.mood != null
        binding.labelMood.isVisible = hasMood
        binding.textViewDetailsMood.isVisible = hasMood
        binding.textViewDetailsMood.text = if (hasMood) log.mood?.getDisplayName(requireContext()) else ""

        val hadActivity = log.hadSexualActivity
        binding.labelActivity.isVisible = hadActivity
        binding.textViewDetailsActivity.isVisible = hadActivity
        if (hadActivity) {
            binding.textViewDetailsActivity.text = if (log.wasProtected == true)
                getString(R.string.sexual_activity_protected)
            else getString(R.string.sexual_activity_unprotected)
        } else {
            binding.textViewDetailsActivity.text = ""
        }

        val hasNotes = !log.notes.isNullOrBlank()
        binding.labelNotes.isVisible = hasNotes
        binding.textViewDetailsNotes.isVisible = hasNotes
        binding.textViewDetailsNotes.text = if (hasNotes) log.notes else ""

        val hasAnyData = hasFlow || hasSymptoms || hasMood || hadActivity || hasNotes
        card.isVisible = true

        if (!hasAnyData) {
            binding.labelFlow.isVisible = true
            binding.textViewDetailsFlow.text = getString(R.string.no_record_for_this_day)
            binding.labelSymptoms.isVisible = false
            binding.textViewDetailsSymptoms.isVisible = false
            binding.labelMood.isVisible = false
            binding.textViewDetailsMood.isVisible = false
            binding.labelActivity.isVisible = false
            binding.textViewDetailsActivity.isVisible = false
            binding.labelNotes.isVisible = false
            binding.textViewDetailsNotes.isVisible = false
        }
    }

    private fun updateDayAppearance(day: CalendarDay, container: DayViewContainer) {
        val textView = container.textView
        textView.background = null

        if (day.position != DayPosition.MonthDate) {
            textView.visibility = View.INVISIBLE
            return
        }
        textView.visibility = View.VISIBLE

        val state = viewModel.uiState.value ?: return

        textView.setTextColor(
            if (day.date.isAfter(LocalDate.now())) ContextCompat.getColor(requireContext(), R.color.md_theme_outline)
            else ContextCompat.getColor(requireContext(), R.color.md_theme_onSurface)
        )

        when {
            state.periodDays.contains(day.date) -> {
                textView.setBackgroundResource(R.drawable.calendar_period_day_bg)
                textView.setTextColor(Color.WHITE)
            }
            state.predictedPeriodDays.contains(day.date) -> {
                textView.setBackgroundResource(R.drawable.calendar_predicted_day_bg)
            }
            state.fertileWindowDays.contains(day.date) -> {
                textView.setBackgroundResource(R.drawable.calendar_fertile_day_bg)
            }
        }

        if (day.date == selectedDate) {
            textView.setBackgroundResource(R.drawable.calendar_selected_day_bg)
            if (state.periodDays.contains(day.date)) {
                textView.setTextColor(Color.WHITE)
            }
        } else if (day.date == LocalDate.now() && textView.background == null) {
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_primary))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}