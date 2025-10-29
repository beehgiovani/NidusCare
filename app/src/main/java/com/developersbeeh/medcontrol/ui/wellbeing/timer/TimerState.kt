package com.developersbeeh.medcontrol.ui.wellbeing.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Representa os possíveis estados do cronômetro
sealed class TimerState {
    object Idle : TimerState()
    data class Running(val elapsedTime: Long) : TimerState()
    data class Paused(val elapsedTime: Long) : TimerState()
}

// Objeto Singleton para compartilhar o estado do timer entre o Serviço e a UI
object TimerServiceManager {
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState = _timerState.asStateFlow()

    fun updateState(newState: TimerState) {
        _timerState.value = newState
    }
}