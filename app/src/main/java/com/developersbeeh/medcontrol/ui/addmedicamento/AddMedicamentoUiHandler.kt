package com.developersbeeh.medcontrol.ui.addmedicamento

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import com.developersbeeh.medcontrol.data.model.FrequenciaTipo
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.TipoDosagem
import com.developersbeeh.medcontrol.data.model.TipoMedicamento
import com.developersbeeh.medcontrol.databinding.FragmentAddMedicamentoBinding
import com.developersbeeh.medcontrol.util.separarDosagem
import com.google.android.material.chip.Chip
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddMedicamentoUiHandler(
    private val fragment: AddMedicamentoFragment,
    private val binding: FragmentAddMedicamentoBinding,
    private val lotesAdapter: LotesAdapter,
    private val lotesList: MutableList<EstoqueLote>
) {
    private val context: Context get() = fragment.requireContext()
    private val timeFormatter: DateTimeFormatter get() = fragment.timeFormatter
    private val dateFormatter: DateTimeFormatter get() = fragment.dateFormatter

    fun updateWizardUi(state: AddMedicamentoUiState) {
        binding.contentStep1.visibility = if (state.expandedStep == WizardStep.STEP_1) View.VISIBLE else View.GONE
        binding.contentStep2.visibility = if (state.expandedStep == WizardStep.STEP_2) View.VISIBLE else View.GONE
        binding.contentStep3.visibility = if (state.expandedStep == WizardStep.STEP_3) View.VISIBLE else View.GONE
        binding.contentStep4.visibility = if (state.expandedStep == WizardStep.STEP_4) View.VISIBLE else View.GONE
        binding.iconStep1.rotation = if (state.expandedStep == WizardStep.STEP_1) 180f else 0f
        binding.iconStep2.rotation = if (state.expandedStep == WizardStep.STEP_2) 180f else 0f
        binding.iconStep3.rotation = if (state.expandedStep == WizardStep.STEP_3) 180f else 0f
        binding.iconStep4.rotation = if (state.expandedStep == WizardStep.STEP_4) 180f else 0f
        binding.iconStep1.setImageResource(if (state.isStep1Complete) R.drawable.ic_check_circle else R.drawable.ic_expand_more)
        binding.iconStep2.setImageResource(if (state.isStep2Complete) R.drawable.ic_check_circle else R.drawable.ic_expand_more)
        binding.iconStep3.setImageResource(if (state.isStep3Complete) R.drawable.ic_check_circle else R.drawable.ic_expand_more)
        binding.summaryStep1.text = state.summaryStep1
        binding.summaryStep2.text = state.summaryStep2
        binding.summaryStep3.text = state.summaryStep3
        binding.summaryStep4.text = state.summaryStep4
        val isSporadic = binding.switchUsoEsporadico.isChecked
        binding.cardStep2.visibility = if (isSporadic) View.GONE else View.VISIBLE
        binding.cardStep3.visibility = if (isSporadic) View.GONE else View.VISIBLE
    }

    fun preencherCamposParaEdicao(medicamento: Medicamento) {
        binding.editTextNome.setText(medicamento.nome)
        val (valor, unidade) = medicamento.dosagem.separarDosagem()
        binding.editTextDosagemValor.setText(valor)
        binding.autoCompleteUnidade.setText(unidade, false)
        binding.editTextAnotacoes.setText(medicamento.anotacoes)
        binding.editTextPrincipioAtivo.setText(medicamento.principioAtivo)
        binding.editTextClasseTerapeutica.setText(medicamento.classeTerapeutica)
        binding.editTextNivelAlerta.setText(if (medicamento.nivelDeAlertaEstoque > 0) medicamento.nivelDeAlertaEstoque.toString() else "")

        if (medicamento.isUsoContinuo) {
            binding.radioGroupTermino.check(R.id.radioTerminoContinuo)
        } else {
            binding.radioGroupTermino.check(R.id.radioTerminoDuracao)
            binding.editTextDuracao.setText(if (medicamento.duracaoDias > 0) medicamento.duracaoDias.toString() else "")
            binding.autoCompleteUnidadeDuracao.setText("dias", false)
        }

        fragment.dataInicioSelecionada = medicamento.dataInicioTratamento
        atualizarTextoDataInicio()

        binding.checkboxNotificacao.isChecked = medicamento.usaNotificacao
        binding.buttonSalvar.text = if (fragment.isEditing()) fragment.getString(R.string.update_button) else fragment.getString(R.string.save_medication)

        binding.chipGroupHorarios.removeAllViews()
        medicamento.horarios.forEach { addHorarioChip(it) }

        binding.switchUsoEsporadico.isChecked = medicamento.isUsoEsporadico
        binding.switchUsoEsporadico.jumpDrawablesToCurrentState()

        lotesList.clear()
        lotesList.addAll(medicamento.lotes)
        lotesAdapter.submitList(lotesList.toList())
        binding.dividerLotes.visibility = if (lotesList.isNotEmpty()) View.VISIBLE else View.GONE

        updateUiForMedicamentoType()
        when (medicamento.tipo) {
            TipoMedicamento.ORAL -> binding.radioGroupTipoMedicamento.check(R.id.radioOral)
            TipoMedicamento.TOPICO -> binding.radioGroupTipoMedicamento.check(R.id.radioTopico)
            TipoMedicamento.INJETAVEL -> binding.radioGroupTipoMedicamento.check(R.id.radioInjetavel)
        }

        binding.chipGroupLocais.removeAllViews()
        medicamento.locaisDeAplicacao.forEach { addLocalChip(it) }

        when (medicamento.tipoDosagem) {
            TipoDosagem.FIXA -> binding.radioGroupTipoDosagem.check(R.id.radioDoseFixa)
            TipoDosagem.MANUAL -> binding.radioGroupTipoDosagem.check(R.id.radioDoseManual)
            TipoDosagem.CALCULADA -> binding.radioGroupTipoDosagem.check(R.id.radioDoseCalculada)
        }

        if (medicamento.tipoDosagem == TipoDosagem.CALCULADA) {
            binding.editTextGlicemiaAlvo.setText(medicamento.glicemiaAlvo?.toString() ?: "")
            binding.editTextFatorSensibilidade.setText(medicamento.fatorSensibilidade?.toString() ?: "")
            binding.editTextRatioCarboidrato.setText(medicamento.ratioCarboidrato?.toString() ?: "")
        }
        updateDosageUI()

        when (medicamento.frequenciaTipo) {
            FrequenciaTipo.DIARIA -> binding.chipGroupFrequenciaTipo.check(R.id.chipDiariamente)
            FrequenciaTipo.SEMANAL -> {
                binding.chipGroupFrequenciaTipo.check(R.id.chipDiasDaSemana)
                medicamento.diasSemana.forEach { dia ->
                    (binding.chipGroupDiasSemana.getChildAt(dia - 1) as Chip).isChecked = true
                }
            }
            FrequenciaTipo.INTERVALO_DIAS -> {
                binding.chipGroupFrequenciaTipo.check(R.id.chipIntervaloDias)
                binding.editTextFrequenciaValor.setText(medicamento.frequenciaValor.toString())
            }
        }
        updateFrequencyControlsVisibility()
        updateAddTimeButtonState()
        fragment.updateAllSummaries()
    }

    fun updateUiForMedicamentoType() {
        val selectedId = binding.radioGroupTipoMedicamento.checkedRadioButtonId
        val tipo = when (selectedId) {
            R.id.radioTopico -> TipoMedicamento.TOPICO
            R.id.radioInjetavel -> TipoMedicamento.INJETAVEL
            else -> TipoMedicamento.ORAL
        }
        val isTopicoOuInjetavel = tipo == TipoMedicamento.TOPICO || tipo == TipoMedicamento.INJETAVEL
        binding.layoutLocaisAplicacao.visibility = if (isTopicoOuInjetavel) View.VISIBLE else View.GONE
        binding.radioDoseCalculada.visibility = if (tipo == TipoMedicamento.INJETAVEL) View.VISIBLE else View.GONE
        if (tipo != TipoMedicamento.INJETAVEL && binding.radioGroupTipoDosagem.checkedRadioButtonId == R.id.radioDoseCalculada) {
            binding.radioGroupTipoDosagem.check(R.id.radioDoseFixa)
        }
        if (tipo == TipoMedicamento.TOPICO) {
            setupUnidadesDropdown(fragment.unidadesDeDosagemTopico)
        } else {
            setupUnidadesDropdown(fragment.unidadesDeDosagemPadrao)
        }
    }

    fun updateDosageUI() {
        val selectedId = binding.radioGroupTipoDosagem.checkedRadioButtonId
        val tipoDosagem = when (selectedId) {
            R.id.radioDoseManual -> TipoDosagem.MANUAL
            R.id.radioDoseCalculada -> TipoDosagem.CALCULADA
            else -> TipoDosagem.FIXA
        }
        binding.layoutDoseFixa.visibility = if (tipoDosagem == TipoDosagem.FIXA) View.VISIBLE else View.GONE
        binding.layoutDoseCalculada.visibility = if (tipoDosagem == TipoDosagem.CALCULADA) View.VISIBLE else View.GONE
        binding.tilDosagemValor.isEnabled = tipoDosagem == TipoDosagem.FIXA
        if (tipoDosagem == TipoDosagem.MANUAL || tipoDosagem == TipoDosagem.CALCULADA) {
            binding.tilDosagemValor.hint = fragment.getString(R.string.unit_hint)
        } else {
            binding.tilDosagemValor.hint = fragment.getString(R.string.quantity_hint)
        }
    }

    fun updateFrequencyControlsVisibility() {
        val checkedId = binding.chipGroupFrequenciaTipo.checkedChipId
        binding.layoutFrequenciaDiaria.visibility = if (checkedId == R.id.chipDiariamente) View.VISIBLE else View.GONE
        binding.layoutFrequenciaSemanal.visibility = if (checkedId == R.id.chipDiasDaSemana) View.VISIBLE else View.GONE
        binding.layoutFrequenciaIntervalo.visibility = if (checkedId == R.id.chipIntervaloDias) View.VISIBLE else View.GONE
    }

    fun updateAddTimeButtonState() {
        val selectedFrequencyId = binding.chipGroupFrequenciaTipo.checkedChipId
        var isEnabled = true
        if (selectedFrequencyId == R.id.chipDiasDaSemana) {
            isEnabled = binding.chipGroupDiasSemana.checkedChipIds.isNotEmpty()
        } else if (selectedFrequencyId == R.id.chipIntervaloDias) {
            isEnabled = !binding.editTextFrequenciaValor.text.isNullOrBlank()
        }
        binding.buttonAddHorario.isEnabled = isEnabled
        binding.buttonAddIntervalo.isEnabled = isEnabled
    }

    fun addHorarioChip(horario: LocalTime) {
        val chip = Chip(context).apply {
            text = horario.format(timeFormatter)
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipGroupHorarios.removeView(this)
            }
        }
        binding.chipGroupHorarios.addView(chip)
    }

    fun addLocalChip(nomeLocal: String) {
        val chip = Chip(context).apply {
            text = nomeLocal
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipGroupLocais.removeView(this)
            }
        }
        binding.chipGroupLocais.addView(chip)
    }

    fun atualizarTextoDataInicio() {
        binding.editTextDataInicio.setText(fragment.dataInicioSelecionada.format(dateFormatter))
    }

    fun atualizarTextoDataTermino() {
        binding.editTextDataTermino.setText(fragment.dataTerminoSelecionada.format(dateFormatter))
    }

    fun setupUnidadesDropdown(unidades: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, unidades)
        binding.autoCompleteUnidade.setAdapter(adapter)
        if (!unidades.contains(binding.autoCompleteUnidade.text.toString())) {
            binding.autoCompleteUnidade.setText(unidades.firstOrNull() ?: "", false)
        }
    }

    fun setupDurationUnitDropdown() {
        val units = listOf("dias", "semanas")
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, units)
        binding.autoCompleteUnidadeDuracao.setAdapter(adapter)
        binding.autoCompleteUnidadeDuracao.setText(units[0], false)
    }

    // ✅ ADIÇÃO: Funções auxiliares para expandir o card e focar no campo
    fun expandAndFocus(step: WizardStep, viewToFocus: View) {
        // Garante que o card correto esteja expandido
        if (fragment.viewModel.uiState.value?.expandedStep != step) {
            fragment.viewModel.onStepHeaderClicked(step)
        }

        // Adiciona um pequeno delay para garantir que a view esteja visível antes de focar
        viewToFocus.postDelayed({
            viewToFocus.requestFocus()
            showKeyboard(viewToFocus)
        }, 250) // Aumentado para garantir que a animação de expansão termine
    }

    private fun showKeyboard(view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}