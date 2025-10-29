package com.developersbeeh.medcontrol.ui.addmedicamento

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoDosagem
import com.developersbeeh.medcontrol.data.model.TipoMedicamento
import com.developersbeeh.medcontrol.databinding.FragmentAddMedicamentoBinding
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.UUID

class AddMedicamentoActionHandler(
    private val fragment: AddMedicamentoFragment,
    private val binding: FragmentAddMedicamentoBinding,
    private val viewModel: AddMedicamentoViewModel,
    private val uiHandler: AddMedicamentoUiHandler
) {
    private val context: Context get() = fragment.requireContext()

    fun handleSave() {
        if (!validarCampos()) {
            return
        }

        val isUsoEsporadico = fragment.isUsoEsporadico()
        if (fragment.isNotificationChecked() && !isUsoEsporadico) {
            checkNotificationPermission()
        } else {
            handleSaveWithPermissionResult(granted = false)
        }
    }

    fun handleSaveWithPermissionResult(granted: Boolean) {
        if (!validarCampos()) return

        if (!tryAddLoteFromInlineForm()) {
            Toast.makeText(context, fragment.getString(R.string.lote_invalid_data), Toast.LENGTH_LONG).show()
            return
        }

        // ✅ CORREÇÃO: Garante que 'dataCriacao' seja definida se estiver em branco (ex: editando da farmacinha)
        val dataCriacaoFinal = if (fragment.medicamentoParaEditar?.dataCriacao.isNullOrBlank()) {
            LocalDateTime.now().toString()
        } else {
            fragment.medicamentoParaEditar!!.dataCriacao
        }

        val isUsoEsporadico = fragment.isUsoEsporadico()
        val medicamentoFinal = getCurrentMedicationStateFromUi().copy(
            id = fragment.medicamentoParaEditar?.id ?: UUID.randomUUID().toString(),
            dataCriacao = dataCriacaoFinal,
            usaNotificacao = if (isUsoEsporadico) false else granted
        )
        viewModel.saveMedicamento(medicamentoFinal, fragment.getDependentIdArgs(), fragment.isEditing())
    }

    private fun validarCampos(): Boolean {
        // --- Passo 1: Identificação ---
        val nome = binding.editTextNome.text.toString().trim()
        if (nome.isEmpty()) {
            binding.tilNome.error = context.getString(R.string.field_required)
            uiHandler.expandAndFocus(WizardStep.STEP_1, binding.editTextNome)
            return false
        } else {
            binding.tilNome.error = null
        }

        // --- Passo 2: Dosagem e Frequência ---
        if (!fragment.isUsoEsporadico()) {
            val isDoseFixa = binding.radioDoseFixa.isChecked
            val dosagemValor = binding.editTextDosagemValor.text.toString().trim()
            val dosagemUnidade = binding.autoCompleteUnidade.text.toString().trim()

            if (isDoseFixa) {
                if (dosagemValor.isEmpty()) {
                    binding.tilDosagemValor.error = context.getString(R.string.required_for_fixed_dose)
                    uiHandler.expandAndFocus(WizardStep.STEP_2, binding.editTextDosagemValor)
                    return false
                } else {
                    binding.tilDosagemValor.error = null
                }
                if (dosagemUnidade.isEmpty()) {
                    binding.tilDosagemUnidade.error = context.getString(R.string.required_for_fixed_dose)
                    uiHandler.expandAndFocus(WizardStep.STEP_2, binding.autoCompleteUnidade)
                    return false
                } else {
                    binding.tilDosagemUnidade.error = null
                }
            }

            if (coletarHorarios().isEmpty()) {
                Toast.makeText(context, fragment.getString(R.string.add_at_least_one_time), Toast.LENGTH_SHORT).show()
                viewModel.onStepHeaderClicked(WizardStep.STEP_2)
                return false
            }
        }

        // --- Passo 3: Duração ---
        if (!fragment.isUsoEsporadico()) {
            if (binding.radioTerminoDuracao.isChecked && binding.editTextDuracao.text.toString().isBlank()) {
                binding.tilDuracao.error = context.getString(R.string.field_required)
                uiHandler.expandAndFocus(WizardStep.STEP_3, binding.editTextDuracao)
                return false
            } else {
                binding.tilDuracao.error = null
            }
            if (binding.radioTerminoDataFim.isChecked && binding.editTextDataTermino.text.toString().isBlank()) {
                Toast.makeText(context, fragment.getString(R.string.select_end_date_error), Toast.LENGTH_SHORT).show()
                viewModel.onStepHeaderClicked(WizardStep.STEP_3)
                return false
            }
        }

        return true
    }


    fun checkForDraft() {
        if (viewModel.hasDraft()) {
            MaterialAlertDialogBuilder(context, R.style.AppTheme_DialogAnimation)
                .setTitle("Rascunho Encontrado")
                .setMessage("Encontramos um medicamento não salvo. Deseja continuar a edição?")
                .setPositiveButton("Continuar") { _, _ ->
                    viewModel.getDraft()?.let { fragment.uiHandler.preencherCamposParaEdicao(it) }
                }
                .setNegativeButton("Descartar") { _, _ ->
                    viewModel.clearDraft()
                    viewModel.medicamentoParaEditar.value?.let { fragment.uiHandler.preencherCamposParaEdicao(it) }
                }
                .setCancelable(false)
                .show()
        } else {
            viewModel.medicamentoParaEditar.value?.let { fragment.uiHandler.preencherCamposParaEdicao(it) }
        }
    }

    fun saveDraft() {
        if (fragment.isEditing()) return
        val draftMedication = getCurrentMedicationStateFromUi()
        viewModel.saveDraft(draftMedication)
    }

    fun showDuplicateMedicationDialog(existingMedication: Medicamento) {
        MaterialAlertDialogBuilder(context, R.style.AppTheme_DialogAnimation)
            .setTitle("Medicamento já Existe")
            .setMessage("Já existe um medicamento chamado '${existingMedication.nome}' para ${fragment.getDependentNameArgs()}. Deseja editar as informações do medicamento existente?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Editar Existente") { _, _ ->
                val action = NavGraphDirections.actionGlobalToAddMedicamentoFragment(
                    medicamento = existingMedication,
                    dependentId = fragment.getDependentIdArgs(),
                    dependentName = fragment.getDependentNameArgs(),
                    isCaregiver = fragment.isCaregiverArgs()
                )
                fragment.findNavController().navigate(action)
            }
            .show()
    }

    fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val selectedTime = LocalTime.of(hourOfDay, minute)
            fragment.uiHandler.addHorarioChip(selectedTime)
        }
        TimePickerDialog(context, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    fun showIntervalsDialog() {
        val intervalOptions = arrayOf("A cada 4 horas", "A cada 6 horas", "A cada 8 horas", "A cada 12 horas")
        val intervalsInHours = intArrayOf(4, 6, 8, 12)
        MaterialAlertDialogBuilder(context)
            .setTitle(fragment.getString(R.string.select_interval_pattern))
            .setItems(intervalOptions) { dialog, which ->
                val interval = intervalsInHours[which]
                showTimePickerDialogForInterval(interval)
                dialog.dismiss()
            }
            .show()
    }

    private fun showTimePickerDialogForInterval(intervalHours: Int) {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            generateAndAddTimesForInterval(LocalTime.of(hourOfDay, minute), intervalHours)
        }
        TimePickerDialog(context, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun generateAndAddTimesForInterval(initialTime: LocalTime, intervalHours: Int) {
        fragment.clearHorarioChips()
        var currentTime = initialTime
        val timesToAdd = mutableSetOf<LocalTime>()
        timesToAdd.add(currentTime)
        for (i in 1..23) {
            currentTime = currentTime.plusHours(intervalHours.toLong())
            if (!timesToAdd.contains(currentTime)) {
                timesToAdd.add(currentTime)
            } else {
                break
            }
        }
        timesToAdd.sorted().forEach { time ->
            fragment.uiHandler.addHorarioChip(time)
        }
    }

    fun showDatePickerDialogForTreatmentStart() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(fragment.getString(R.string.treatment_start_date))
            .setSelection(fragment.dataInicioSelecionada.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            fragment.dataInicioSelecionada = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            fragment.uiHandler.atualizarTextoDataInicio()
        }
        datePicker.show(fragment.childFragmentManager, "TREATMENT_START_DATE_PICKER")
    }

    fun showDatePickerDialogForTreatmentEnd() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(fragment.getString(R.string.treatment_end_date_title))
            .setSelection(fragment.dataTerminoSelecionada.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            fragment.dataTerminoSelecionada = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            fragment.uiHandler.atualizarTextoDataTermino()
        }
        datePicker.show(fragment.childFragmentManager, "TREATMENT_END_DATE_PICKER")
    }

    fun showDatePickerDialogForLote() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(fragment.getString(R.string.lote_validity_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            fragment.setLoteValidadeText(selectedDate.format(fragment.dateFormatter))
        }
        datePicker.show(fragment.childFragmentManager, "LOTE_DATE_PICKER")
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    checkExactAlarmPermission()
                }
                else -> fragment.requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    internal fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            } else {
                handleSaveWithPermissionResult(granted = true)
            }
        } else {
            handleSaveWithPermissionResult(granted = true)
        }
    }

    private fun showExactAlarmPermissionDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle(fragment.getString(R.string.permission_needed_title))
            .setMessage(fragment.getString(R.string.exact_alarm_permission_message))
            .setPositiveButton(fragment.getString(R.string.grant)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { intent ->
                        fragment.startActivity(intent)
                    }
                }
            }
            .setNegativeButton(fragment.getString(R.string.not_now)) { dialog, _ ->
                handleSaveWithPermissionResult(granted = false)
                dialog.dismiss()
            }
            .show()
    }

    internal fun tryAddLoteFromInlineForm(): Boolean {
        val quantidadeStr = binding.editTextLoteQuantidade.text.toString().replace(',', '.')
        val validadeStr = binding.editTextLoteValidade.text.toString()
        if (quantidadeStr.isBlank() && validadeStr.isBlank()) {
            return true
        }
        val quantidade = quantidadeStr.toDoubleOrNull()
        if (quantidade == null || quantidade <= 0 || validadeStr.isBlank()) {
            return false
        }
        try {
            val validade = LocalDate.parse(validadeStr, fragment.dateFormatter)
            val novoLote = EstoqueLote(
                quantidade = quantidade,
                quantidadeInicial = quantidade,
                dataValidadeString = validade.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
            fragment.lotesList.add(novoLote)
            fragment.lotesAdapter.submitList(fragment.lotesList.toList())
            binding.dividerLotes.visibility = View.VISIBLE
            binding.editTextLoteQuantidade.text = null
            binding.editTextLoteValidade.text = null
            binding.editTextLoteQuantidade.requestFocus()
            return true
        } catch (e: DateTimeParseException) {
            Toast.makeText(context, fragment.getString(R.string.invalid_date_format), Toast.LENGTH_SHORT).show()
            return false
        }
    }

    internal fun getCurrentMedicationStateFromUi(): Medicamento {
        val isUsoEsporadico = binding.switchUsoEsporadico.isChecked
        val horarios = if (isUsoEsporadico) emptyList() else coletarHorarios()

        var duracaoFinal = 0
        var isUsoContinuoFinal = false
        when (binding.radioGroupTermino.checkedRadioButtonId) {
            R.id.radioTerminoContinuo -> { isUsoContinuoFinal = true }
            R.id.radioTerminoDuracao -> {
                isUsoContinuoFinal = false
                val duracaoValor = binding.editTextDuracao.text.toString().toIntOrNull() ?: 0
                val unidade = binding.autoCompleteUnidadeDuracao.text.toString()
                duracaoFinal = if (unidade.equals("semanas", ignoreCase = true)) duracaoValor * 7 else duracaoValor
            }
            R.id.radioTerminoDataFim -> {
                isUsoContinuoFinal = false
                duracaoFinal = if (fragment.dataTerminoSelecionada.isBefore(fragment.dataInicioSelecionada)) 0 else (ChronoUnit.DAYS.between(fragment.dataInicioSelecionada, fragment.dataTerminoSelecionada) + 1).toInt()
            }
        }

        val frequenciaTipo = when (binding.chipGroupFrequenciaTipo.checkedChipId) {
            R.id.chipDiasDaSemana -> FrequenciaTipo.SEMANAL
            R.id.chipIntervaloDias -> FrequenciaTipo.INTERVALO_DIAS
            else -> FrequenciaTipo.DIARIA
        }

        val diasDaSemanaSelecionados = mutableListOf<Int>()
        if (frequenciaTipo == FrequenciaTipo.SEMANAL) {
            binding.chipGroupDiasSemana.children.forEachIndexed { index, view ->
                if ((view as Chip).isChecked) diasDaSemanaSelecionados.add(index + 1)
            }
        }

        val intervaloDias = if (frequenciaTipo == FrequenciaTipo.INTERVALO_DIAS) binding.editTextFrequenciaValor.text.toString().toIntOrNull() ?: 1 else 1
        val nome = binding.editTextNome.text.toString()
        val dosagemValor = binding.editTextDosagemValor.text.toString().replace(',', '.')
        val dosagemUnidade = binding.autoCompleteUnidade.text.toString()
        val dosagem = if (dosagemValor.isNotBlank()) "$dosagemValor $dosagemUnidade" else dosagemUnidade

        val tipoMedicamento = when (binding.radioGroupTipoMedicamento.checkedRadioButtonId) {
            R.id.radioTopico -> TipoMedicamento.TOPICO
            R.id.radioInjetavel -> TipoMedicamento.INJETAVEL
            else -> TipoMedicamento.ORAL
        }

        val tipoDosagem = when (binding.radioGroupTipoDosagem.checkedRadioButtonId) {
            R.id.radioDoseManual -> TipoDosagem.MANUAL
            R.id.radioDoseCalculada -> TipoDosagem.CALCULADA
            else -> TipoDosagem.FIXA
        }

        return Medicamento(
            nome = nome, dosagem = dosagem, principioAtivo = binding.editTextPrincipioAtivo.text.toString().takeIf { it.isNotBlank() },
            classeTerapeutica = binding.editTextClasseTerapeutica.text.toString().takeIf { it.isNotBlank() },
            dataInicioTratamento = fragment.dataInicioSelecionada, duracaoDias = duracaoFinal,
            usaNotificacao = if (isUsoEsporadico) false else binding.checkboxNotificacao.isChecked,
            isUsoContinuo = isUsoContinuoFinal, isUsoEsporadico = isUsoEsporadico, horarios = horarios,
            anotacoes = binding.editTextAnotacoes.text.toString(), lotes = fragment.lotesList.toList(),
            nivelDeAlertaEstoque = binding.editTextNivelAlerta.text.toString().toIntOrNull() ?: 0,
            tipo = tipoMedicamento, locaisDeAplicacao = coletarLocais(), tipoDosagem = tipoDosagem,
            glicemiaAlvo = if (tipoDosagem == TipoDosagem.CALCULADA) binding.editTextGlicemiaAlvo.text.toString().toDoubleOrNull() else null,
            fatorSensibilidade = if (tipoDosagem == TipoDosagem.CALCULADA) binding.editTextFatorSensibilidade.text.toString().toDoubleOrNull() else null,
            ratioCarboidrato = if (tipoDosagem == TipoDosagem.CALCULADA) binding.editTextRatioCarboidrato.text.toString().toDoubleOrNull() else null,
            frequenciaTipo = frequenciaTipo, diasSemana = diasDaSemanaSelecionados, frequenciaValor = intervaloDias
        )
    }

    internal fun coletarHorarios(): List<LocalTime> {
        val horarios = mutableListOf<LocalTime>()
        binding.chipGroupHorarios.children.forEach { chip ->
            if (chip is Chip) {
                try {
                    horarios.add(LocalTime.parse(chip.text.toString(), fragment.timeFormatter))
                } catch (e: DateTimeParseException) {
                    Log.e("tag", "Erro de formatação de horário: ${chip.text}", e)
                }
            }
        }
        return horarios.sorted()
    }

    internal fun coletarLocais(): List<String> {
        val locais = mutableListOf<String>()
        binding.chipGroupLocais.children.forEach { if (it is Chip) locais.add(it.text.toString()) }
        return locais
    }
}