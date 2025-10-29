package com.developersbeeh.medcontrol.data.model

import com.developersbeeh.medcontrol.R

enum class TipoDocumento(val displayName: String, val iconRes: Int) {
    EXAME_LABORATORIAL("Exame Laboratorial", R.drawable.ic_lab_test),
    EXAME_IMAGEM("Exame de Imagem", R.drawable.ic_image_search),
    RECEITUARIO("Receituário Médico", R.drawable.ic_prescription),
    RELATORIO("Relatório/Atestado", R.drawable.ic_file_document),
    OUTRO("Outro", R.drawable.ic_file)
}