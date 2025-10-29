package com.developersbeeh.medcontrol.util

/**
 * Separa uma string de dosagem (ex: "50 mg") em seu valor numérico e sua unidade.
 * @return Um Pair onde o primeiro elemento é o valor (String) e o segundo é a unidade (String).
 */
fun String.separarDosagem(): Pair<String, String> {
    // Regex para encontrar o número (incluindo decimais com , ou .) e o resto da string como unidade.
    val matchResult = Regex("(\\d[\\d,.]*)\\s*([\\w()]+.*)").find(this)
    return if (matchResult != null) {
        val valor = matchResult.groups[1]?.value ?: ""
        val unidade = matchResult.groups[2]?.value?.trim() ?: ""
        Pair(valor, unidade)
    } else {
        // Se não encontrar um número no início, considera toda a string como a unidade.
        Pair("", this.trim())
    }
}