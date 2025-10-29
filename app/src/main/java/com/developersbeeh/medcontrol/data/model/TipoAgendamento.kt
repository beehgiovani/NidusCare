package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.developersbeeh.medcontrol.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TipoAgendamento(val displayName: String, val iconRes: Int) : Parcelable {
    CONSULTA("Consulta Médica", R.drawable.ic_medical_services),
    EXAME_LABORATORIAL("Exame Laboratorial", R.drawable.ic_lab_test),
    EXAME_IMAGEM("Exame de Imagem", R.drawable.ic_image_search),
    PROCEDIMENTO("Procedimento", R.drawable.ic_thermostat),
    OUTRO("Outro", R.drawable.ic_calendar)
}