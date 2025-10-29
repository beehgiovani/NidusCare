// src/main/java/com/developersbeeh/medcontrol/util/PdfReportGenerator.kt
package com.developersbeeh.medcontrol.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.ContextCompat
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.ui.reports.ReportOptions
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class PdfReportGenerator(private val context: Context) {

    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val LEFT_MARGIN = 40f
    private val RIGHT_MARGIN = (PAGE_WIDTH - 40).toFloat()
    private val TOP_MARGIN = 40f
    private val BOTTOM_MARGIN = (PAGE_HEIGHT - 40).toFloat()
    private val CONTENT_WIDTH = (RIGHT_MARGIN - LEFT_MARGIN).toInt()

    private lateinit var document: PdfDocument
    private lateinit var currentPage: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var yPosition = 0f
    private var pageNumber = 0

    private lateinit var currentDependente: Dependente
    private lateinit var reportStartDate: LocalDate
    private lateinit var reportEndDate: LocalDate
    private lateinit var reportLogo: Bitmap
    private var isPremiumUser = false

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy 'às' HH:mm")

    private val titlePaint = TextPaint().apply { color = ContextCompat.getColor(context, R.color.md_theme_primary); textSize = 18f; isFakeBoldText = true }
    private val subtitlePaint = TextPaint().apply { color = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant); textSize = 10f }
    private val sectionTitlePaint = TextPaint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
    private val tableHeaderPaint = TextPaint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }
    private val bodyPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 10f }
    private val bodyMutedPaint = TextPaint().apply { color = Color.GRAY; textSize = 9f }
    private val footerPaint = Paint().apply { color = Color.LTGRAY; textSize = 9f; textAlign = Paint.Align.CENTER }
    private val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

    fun createReport(
        cuidador: Usuario,
        dependente: Dependente,
        medicamentos: List<Medicamento>,
        doseHistory: List<DoseHistory>,
        healthNotes: List<HealthNote>,
        schedules: List<AgendamentoSaude>,
        dailyCycleLogs: List<DailyCycleLog>,
        startDate: LocalDate,
        endDate: LocalDate,
        logoBitmap: Bitmap,
        isPremium: Boolean,
        options: ReportOptions
    ): File {
        this.currentDependente = dependente
        this.reportStartDate = startDate
        this.reportEndDate = endDate
        this.reportLogo = logoBitmap
        this.isPremiumUser = isPremium

        document = PdfDocument()
        pageNumber = 0
        startNewPage()

        drawPatientSummarySection()

        if (isPremiumUser && options.includeAdherenceChart) {
            drawChartsSection(medicamentos, doseHistory)
        }

        if (options.includeAdherenceSummary && medicamentos.any { !it.isUsoEsporadico }) {
            drawAdherenceSection(medicamentos, doseHistory, startDate, endDate)
        }

        if (options.includedNoteTypes.contains(HealthNoteType.WEIGHT)) {
            drawWeightEvolutionSection(healthNotes)
        }

        if (dependente.sexo == Sexo.FEMININO.name) {
            drawMenstrualCycleSection(dailyCycleLogs)
        }

        if (options.includeDoseHistory) {
            drawDoseHistorySection(doseHistory, medicamentos)
        }

        if (options.includeAppointments) {
            drawSchedulesSection(schedules)
        }

        val reportableNoteTypes = options.includedNoteTypes.filterNot { it == HealthNoteType.WEIGHT }
        reportableNoteTypes.forEach { type ->
            val notesOfType = healthNotes.filter { it.type == type }
            if (notesOfType.isNotEmpty()) {
                drawHealthNotesSection(notesOfType, type)
            }
        }

        drawFooter()
        document.finishPage(currentPage)

        val fileName = "NidusCare_Relatorio_${dependente.nome.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    private fun startNewPage() {
        if (this::currentPage.isInitialized && pageNumber > 0) {
            drawFooter()
            document.finishPage(currentPage)
        }
        pageNumber++
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        currentPage = document.startPage(pageInfo)
        canvas = currentPage.canvas
        yPosition = TOP_MARGIN
        drawHeader()
    }

    private fun checkAndAddNewPage(requiredHeight: Float) {
        if (yPosition + requiredHeight > BOTTOM_MARGIN) {
            startNewPage()
        }
    }

    private fun drawHeader() {
        val logoSize = 40f
        val logoDestRect = RectF(LEFT_MARGIN, yPosition, LEFT_MARGIN + logoSize, yPosition + logoSize)
        canvas.drawBitmap(reportLogo, null, logoDestRect, null)
        val headerTextX = LEFT_MARGIN + logoSize + 15f
        canvas.drawText("Relatório de Saúde NidusCare", headerTextX, yPosition + (logoSize / 2) + 6f, titlePaint)
        yPosition += logoSize + 15f
        val dateRangeText = if (reportStartDate == reportEndDate) reportStartDate.format(dateFormatter) else "${reportStartDate.format(dateFormatter)} a ${reportEndDate.format(dateFormatter)}"
        val generatedDate = LocalDate.now().format(dateFormatter)
        val headerInfo = "Relatório para ${currentDependente.nome} | Período: $dateRangeText | Gerado em: $generatedDate"
        canvas.drawText(headerInfo, LEFT_MARGIN, yPosition, subtitlePaint)
        yPosition += 25f
    }

    private fun drawFooter() {
        val footerText = "Página $pageNumber"
        canvas.drawText(footerText, (PAGE_WIDTH / 2).toFloat(), PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawSectionHeader(title: String) {
        checkAndAddNewPage(40f)
        canvas.drawText(title, LEFT_MARGIN, yPosition, sectionTitlePaint)
        yPosition += 20f
        canvas.drawLine(LEFT_MARGIN, yPosition - 5f, RIGHT_MARGIN, yPosition - 5f, linePaint)
    }

    private fun drawWrappedText(text: String, paint: TextPaint, width: Int = CONTENT_WIDTH, xOffset: Float = 0f) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width).build()
        checkAndAddNewPage(staticLayout.height.toFloat() + 5)
        canvas.save()
        canvas.translate(LEFT_MARGIN + xOffset, yPosition)
        staticLayout.draw(canvas)
        canvas.restore()
        yPosition += staticLayout.height.toFloat()
    }

    private fun drawPatientSummarySection() {
        drawSectionHeader("Resumo do Paciente")
        val summaryText = buildString {
            append("Nome: ${currentDependente.nome}\n")
            if (currentDependente.dataDeNascimento.isNotBlank()) append("Data de Nascimento: ${currentDependente.dataDeNascimento}\n")
            if (currentDependente.condicoesPreexistentes.isNotBlank()) append("Condições Pré-existentes: ${currentDependente.condicoesPreexistentes}\n")
            if (currentDependente.alergias.isNotBlank()) append("Alergias: ${currentDependente.alergias}\n")
        }.trim()
        drawWrappedText(summaryText, bodyPaint)
        yPosition += 25f
    }

    private fun drawChartsSection(medicamentos: List<Medicamento>, doseHistory: List<DoseHistory>) {
        val scheduledMeds = medicamentos.filter { !it.isUsoEsporadico && it.horarios.isNotEmpty() }
        if (scheduledMeds.isEmpty()) return

        drawSectionHeader("Gráfico de Adesão (Últimos 7 dias)")

        try {
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(6)
            val weeklyDates = (0..6).map { sevenDaysAgo.plusDays(it.toLong()) }

            val barEntries = weeklyDates.mapIndexed { index, date ->
                val dosesOnDay = doseHistory.filter { it.timestamp.toLocalDate() == date }
                val expectedOnDay = DoseTimeCalculator.calculateExpectedDosesForPeriod(scheduledMeds, date, date)
                val uniqueTakenTimestamps = dosesOnDay.map { it.timestamp }.toSet()
                val takenOnDay = uniqueTakenTimestamps.size

                val adherencePercent = if (expectedOnDay > 0) {
                    min((takenOnDay.toDouble() / expectedOnDay) * 100, 100.0).toFloat()
                } else {
                    100f
                }
                BarEntry(index.toFloat(), adherencePercent)
            }

            if (barEntries.isEmpty()) {
                drawWrappedText("Não há dados de adesão para exibir no período.", bodyMutedPaint)
                yPosition += 25f
                return
            }

            val chartBitmap = generateBarChartBitmap(barEntries, weeklyDates)
            checkAndAddNewPage(chartBitmap.height.toFloat() + 25f)
            canvas.drawBitmap(chartBitmap, LEFT_MARGIN, yPosition, null)
            yPosition += chartBitmap.height.toFloat() + 25f

        } catch (e: Exception) {
            Log.e("PdfReportGenerator", "Falha ao gerar o bitmap do gráfico de adesão", e)
            drawWrappedText("Não foi possível gerar o gráfico de adesão devido a um erro.", bodyMutedPaint)
            yPosition += 25f
        }
    }

    private fun generateBarChartBitmap(entries: List<BarEntry>, labels: List<LocalDate>): Bitmap {
        val chart = BarChart(context)
        chart.layout(0, 0, CONTENT_WIDTH, 250)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels.map { it.format(DateTimeFormatter.ofPattern("dd/MM")) })
        }
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 105f
        chart.axisRight.isEnabled = false
        val dataSet = BarDataSet(entries, "Adesão").apply {
            color = ContextCompat.getColor(context, R.color.md_theme_primary)
            setDrawValues(false)
        }
        chart.data = BarData(dataSet)
        chart.invalidate()
        val bitmap = Bitmap.createBitmap(CONTENT_WIDTH, 250, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        chart.draw(canvas)
        return bitmap
    }

    private fun drawAdherenceSection(medicamentos: List<Medicamento>, doseHistory: List<DoseHistory>, startDate: LocalDate, endDate: LocalDate) {
        drawSectionHeader("Resumo de Adesão")
        val scheduledMeds = medicamentos.filter { !it.isUsoEsporadico }
        if (scheduledMeds.isEmpty()) {
            drawWrappedText("Nenhum medicamento de uso regular no período.", bodyMutedPaint)
            yPosition += 25f
            return
        }
        var totalExpected = 0
        var totalTaken = 0
        scheduledMeds.forEach { med ->
            val expected = DoseTimeCalculator.calculateExpectedDosesForPeriod(listOf(med), startDate, endDate)
            val taken = doseHistory.count { it.medicamentoId == med.id }
            totalExpected += expected
            totalTaken += taken
            val adherence = if (expected > 0) ((taken.toDouble() / expected) * 100).roundToInt() else 100
            val text = "${med.nome}: ${adherence}% de adesão (${taken} de ${expected} doses)"
            drawWrappedText(text, bodyPaint)
            yPosition += 5f
        }
        val overallAdherence = if (totalExpected > 0) ((totalTaken.toDouble() / totalExpected) * 100).roundToInt() else 100
        val overallText = "\nAdesão Geral no Período: $overallAdherence%"
        drawWrappedText(overallText, sectionTitlePaint)
        yPosition += 25f
    }

    private fun drawWeightEvolutionSection(healthNotes: List<HealthNote>) {
        val weightNotes = healthNotes
            .filter { it.type == HealthNoteType.WEIGHT && it.values["weight"]?.replace(",",".")?.toFloatOrNull() != null }
            .sortedBy { it.timestamp }
        if (weightNotes.size < 2) return
        drawSectionHeader("Evolução do Peso")
        val firstRecord = weightNotes.first()
        val lastRecord = weightNotes.last()
        val firstWeight = firstRecord.values["weight"]!!.replace(",", ".").toFloat()
        val lastWeight = lastRecord.values["weight"]!!.replace(",", ".").toFloat()
        val difference = lastWeight - firstWeight
        val evolutionText = when {
            abs(difference) < 0.1f -> "O peso se manteve estável em ${String.format("%.1f", lastWeight)} kg."
            difference > 0 -> "Ganho de ${String.format("%.1f", difference)} kg no período (de ${firstWeight}kg para ${lastWeight}kg)."
            else -> "Perda de ${String.format("%.1f", abs(difference))} kg no período (de ${firstWeight}kg para ${lastWeight}kg)."
        }
        drawWrappedText(evolutionText, bodyPaint)
        yPosition += 25f
    }

    private fun drawMenstrualCycleSection(logs: List<DailyCycleLog>) {
        if (logs.isEmpty()) return
        val cycles = mutableListOf<Pair<LocalDate, LocalDate?>>()
        val sortedLogsWithFlow = logs.filter { it.flow != FlowIntensity.NONE }.sortedBy { it.getDate() }
        var currentCycleStart: LocalDate? = null
        var lastPeriodDay: LocalDate? = null
        for (log in sortedLogsWithFlow) {
            val date = log.getDate()
            if (currentCycleStart == null) {
                currentCycleStart = date
            } else if (lastPeriodDay != null && ChronoUnit.DAYS.between(lastPeriodDay, date) > 1) {
                cycles.add(Pair(currentCycleStart, lastPeriodDay))
                currentCycleStart = date
            }
            lastPeriodDay = date
        }
        if (currentCycleStart != null) {
            val isOngoing = ChronoUnit.DAYS.between(lastPeriodDay, LocalDate.now()) <= 1
            cycles.add(Pair(currentCycleStart, if (isOngoing) null else lastPeriodDay))
        }
        val relevantCycles = cycles.filter { (start, end) ->
            !(start.isAfter(reportEndDate) || (end != null && end.isBefore(reportStartDate)))
        }
        if (relevantCycles.isEmpty()) return
        drawSectionHeader("Resumo do Ciclo Menstrual")
        relevantCycles.forEach { (start, end) ->
            val startDateFormatted = start.format(dateFormatter)
            val endDateFormatted = end?.format(dateFormatter) ?: "Em andamento"
            val duration = if (end != null) "(${ChronoUnit.DAYS.between(start, end) + 1} dias)" else ""
            val cycleSummary = "Período: $startDateFormatted a $endDateFormatted $duration"
            checkAndAddNewPage(calculateTextHeight(cycleSummary, bodyPaint, CONTENT_WIDTH) + 10f)
            drawWrappedText(cycleSummary, bodyPaint)
            yPosition += 10f
        }
        yPosition += 25f
    }

    private fun drawDoseHistorySection(doseHistory: List<DoseHistory>, medicamentos: List<Medicamento>) {
        drawSectionHeader("Histórico de Doses Detalhado")
        if (doseHistory.isEmpty()) {
            drawWrappedText("Nenhum registro de dose no período.", bodyMutedPaint)
            yPosition += 25f
            return
        }
        val col1X = 0f; val col2X = 80f; val col3X = 250f
        val col1Width = (col2X - col1X).toInt() - 10
        val col2Width = (col3X - col2X).toInt() - 10
        val col3Width = (RIGHT_MARGIN - (LEFT_MARGIN + col3X)).toInt()
        checkAndAddNewPage(20f)
        canvas.drawText("Data/Hora", LEFT_MARGIN + col1X, yPosition, tableHeaderPaint)
        canvas.drawText("Medicamento", LEFT_MARGIN + col2X, yPosition, tableHeaderPaint)
        canvas.drawText("Detalhes", LEFT_MARGIN + col3X, yPosition, tableHeaderPaint)
        yPosition += 15f
        doseHistory.sortedBy { it.timestamp }.forEach { dose ->
            val med = medicamentos.find { it.id == dose.medicamentoId }
            val medName = med?.nome ?: "Desconhecido"
            var details = med?.dosagem ?: ""
            dose.quantidadeAdministrada?.let { details = "$it ${med?.unidadeDeEstoque}" }
            dose.localDeAplicacao?.let { details += " ($it)" }
            val dateText = dose.timestamp.format(dateTimeFormatter)
            val dateHeight = calculateTextHeight(dateText, bodyPaint, col1Width)
            val nameHeight = calculateTextHeight(medName, bodyPaint, col2Width)
            val detailsHeight = calculateTextHeight(details, bodyPaint, col3Width)
            val rowHeight = maxOf(dateHeight, nameHeight, detailsHeight)
            checkAndAddNewPage(rowHeight + 10f)
            val currentY = yPosition
            drawWrappedText(dateText, bodyPaint, col1Width, col1X)
            yPosition = currentY
            drawWrappedText(medName, bodyPaint, col2Width, col2X)
            yPosition = currentY
            drawWrappedText(details, bodyPaint, col3Width, col3X)
            yPosition = currentY + rowHeight + 10f
        }
        yPosition += 15f
    }

    private fun drawHealthNotesSection(notes: List<HealthNote>, type: HealthNoteType) {
        drawSectionHeader("Anotações de ${type.displayName}")
        if (notes.isEmpty()) {
            drawWrappedText("Nenhum registro de '${type.displayName}' no período.", bodyMutedPaint)
            yPosition += 25f
            return
        }
        val col1X = 0f; val col2X = 80f
        val col1Width = (col2X - col1X).toInt() - 10
        val col2Width = (RIGHT_MARGIN - (LEFT_MARGIN + col2X)).toInt()
        checkAndAddNewPage(20f)
        canvas.drawText("Data/Hora", LEFT_MARGIN + col1X, yPosition, tableHeaderPaint)
        canvas.drawText("Detalhes", LEFT_MARGIN + col2X, yPosition, tableHeaderPaint)
        yPosition += 15f
        notes.sortedBy { it.timestamp }.forEach { note ->
            val valueText = formatHealthNoteValues(note)
            val noteText = note.note?.let { "\nObs: $it" } ?: ""
            val fullDetails = valueText + noteText
            val dateText = note.timestamp.format(dateTimeFormatter)
            val dateHeight = calculateTextHeight(dateText, bodyPaint, col1Width)
            val detailsHeight = calculateTextHeight(fullDetails, bodyPaint, col2Width)
            val rowHeight = maxOf(dateHeight, detailsHeight)
            checkAndAddNewPage(rowHeight + 10f)
            val currentY = yPosition
            drawWrappedText(dateText, bodyPaint, col1Width, col1X)
            yPosition = currentY
            drawWrappedText(fullDetails, bodyPaint, col2Width, col2X)
            yPosition = currentY + rowHeight + 10f
        }
        yPosition += 15f
    }

    private fun drawSchedulesSection(schedules: List<AgendamentoSaude>) {
        if (schedules.isEmpty()) return
        drawSectionHeader("Agenda de Saúde")
        schedules.sortedBy { it.timestamp }.forEach { schedule ->
            val scheduleText = "${schedule.timestamp.format(dateTimeFormatter)} - ${schedule.titulo} (${schedule.tipo.displayName})"
            drawWrappedText(scheduleText, bodyPaint)
            yPosition += 5f
        }
        yPosition += 25f
    }

    private fun calculateTextHeight(text: String, paint: TextPaint, width: Int): Float {
        if (text.isBlank()) return 0f
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width).build().height.toFloat()
    }

    private fun formatHealthNoteValues(note: HealthNote): String {
        return when (note.type) {
            HealthNoteType.BLOOD_PRESSURE -> "Sistólica: ${note.values["systolic"]}, Diastólica: ${note.values["diastolic"]}"
            HealthNoteType.BLOOD_SUGAR -> "Nível: ${note.values["sugarLevel"]} mg/dL (${note.values["timing"]})"
            HealthNoteType.WEIGHT -> "Peso: ${note.values["weight"]} kg"
            HealthNoteType.TEMPERATURE -> "Temperatura: ${note.values["temperature"]} °C"
            HealthNoteType.MOOD -> note.values["mood"] ?: ""
            HealthNoteType.SYMPTOM -> note.values["symptom"] ?: ""
            else -> note.values.map { "${it.key}: ${it.value}" }.joinToString(", ")
        }
    }
}