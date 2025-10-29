package com.developersbeeh.medcontrol.ui.meals

import android.content.Context
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.TipoRefeicao

// Esta função converte o Enum TipoRefeicao para a string correta do strings.xml
fun TipoRefeicao.getDisplayName(context: Context): String {
    val resId = when (this) {
        TipoRefeicao.CAFE_DA_MANHA -> R.string.meal_type_breakfast
        TipoRefeicao.ALMOCO -> R.string.meal_type_lunch
        TipoRefeicao.JANTAR -> R.string.meal_type_dinner
        TipoRefeicao.LANCHE -> R.string.meal_type_snack
    }
    return context.getString(resId)
}