package com.developersbeeh.medcontrol.data.model

enum class TipoDosagem {
    FIXA, // Dose padrão, sempre a mesma
    MANUAL, // Usuário insere a quantidade a cada dose
    CALCULADA // Para insulina, baseada em glicemia e carboidratos
}