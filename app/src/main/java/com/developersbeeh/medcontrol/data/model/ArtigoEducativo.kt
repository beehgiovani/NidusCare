// src/main/java/com/developersbeeh/medcontrol/data/model/ArtigoEducativo.kt

package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArtigoEducativo(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val categoria: String,
    val imageUrl: String,
    val conteudo: String
) : Parcelable