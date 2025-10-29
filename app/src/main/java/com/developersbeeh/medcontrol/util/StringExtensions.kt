package com.developersbeeh.medcontrol.util

import java.util.Locale

/**
 * Capitaliza a primeira letra de cada palavra em uma String.
 * Lida com espaços extras no início, fim e entre as palavras.
 */
fun String.capitalizeWords(): String {
    // Retorna a string original se ela for vazia ou só tiver espaços.
    if (this.isBlank()) return this

    // Remove espaços do início/fim e divide por um ou mais espaços em branco.
    return this.trim().lowercase(Locale.getDefault()).split(Regex("\\s+"))
        .joinToString(" ") { word ->
            // Capitaliza a primeira letra de cada palavra.
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
}
