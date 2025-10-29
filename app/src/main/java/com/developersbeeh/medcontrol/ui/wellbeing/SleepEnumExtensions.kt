package com.developersbeeh.medcontrol.ui.sleep

import android.content.Context
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.QualidadeSono

// Esta função converte o Enum QualidadeSono para a string correta do strings.xml
fun QualidadeSono.getDisplayName(context: Context): String {
    val resId = when (this) {
        QualidadeSono.RUIM -> R.string.sleep_quality_bad
        QualidadeSono.RAZOAVEL -> R.string.sleep_quality_reasonable
        QualidadeSono.BOM -> R.string.sleep_quality_good
    }
    return context.getString(resId)
}