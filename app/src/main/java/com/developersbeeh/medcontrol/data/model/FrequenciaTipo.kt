package com.developersbeeh.medcontrol.data.model

enum class FrequenciaTipo {
    DIARIA, // A cada X horas no mesmo dia
    SEMANAL, // Em dias específicos da semana (ex: Seg, Qua, Sex)
    INTERVALO_DIAS // Em dias alternados (ex: a cada 2 dias)
}