package com.developersbeeh.medcontrol.util

import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.HealthNoteType
import com.developersbeeh.medcontrol.data.model.TipoAtividade

object TimelineIconMapper {

    fun getIconRes(iconName: String): Int {
        return when (iconName) {
            "ic_dose" -> R.drawable.ic_pill // Mapeamento para 'historico_doses'
            "ic_note" -> R.drawable.ic_notes // Mapeamento para 'health_notes'
            "ic_activity_log" -> R.drawable.ic_info // Mapeamento para 'atividades'
            "ic_ai_analysis" -> R.drawable.ic_ai_analysis // Mapeamento para 'insights'

            // Mapeamentos para 'WELLBEING' (Bem-Estar)
            "ic_water_drop" -> R.drawable.ic_water_drop
            "ic_fitness" -> R.drawable.ic_fitness
            "ic_meal" -> R.drawable.ic_food
            "ic_sleep" -> R.drawable.ic_sleep

            // Mapeamentos antigos (podem ser redundantes agora)
            HealthNoteType.BLOOD_PRESSURE.name -> R.drawable.ic_blood_pressure
            HealthNoteType.BLOOD_SUGAR.name -> R.drawable.ic_blood_glucose
            HealthNoteType.WEIGHT.name -> R.drawable.ic_weight
            HealthNoteType.TEMPERATURE.name -> R.drawable.ic_thermostat
            HealthNoteType.MOOD.name -> R.drawable.ic_sentiment
            HealthNoteType.SYMPTOM.name -> R.drawable.ic_symptom
            HealthNoteType.GENERAL.name -> R.drawable.ic_notes
            TipoAtividade.DOCUMENTO_ADICIONADO.name -> R.drawable.ic_file_document
            TipoAtividade.AGENDAMENTO_CRIADO.name -> R.drawable.ic_calendar
            TipoAtividade.MEDICAMENTO_CRIADO.name -> R.drawable.ic_add_circle
            TipoAtividade.MEDICAMENTO_EDITADO.name -> R.drawable.ic_edit
            TipoAtividade.MEDICAMENTO_EXCLUIDO.name -> R.drawable.ic_delete
            TipoAtividade.TRATAMENTO_PAUSADO.name -> R.drawable.ic_pause
            TipoAtividade.TRATAMENTO_REATIVADO.name -> R.drawable.ic_play_arrow

            else -> R.drawable.ic_info
        }
    }

    fun getColorRes(type: String): Int {
        return when (type) {
            "DOSE" -> R.color.md_theme_primary
            "NOTE" -> R.color.md_theme_secondary
            "INSIGHT" -> R.color.md_theme_tertiary
            "ACTIVITY" -> R.color.info_blue
            "WELLBEING" -> R.color.success_green // Cor para Bem-Estar
            else -> R.color.md_theme_onSurfaceVariant
        }
    }
}