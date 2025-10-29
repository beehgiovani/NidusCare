package com.developersbeeh.medcontrol.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developersbeeh.medcontrol.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _pages = MutableLiveData<List<OnboardingPage>>()
    val pages: LiveData<List<OnboardingPage>> = _pages

    init {
        _pages.value = listOf(
            OnboardingPage(
                title = "Bem-vindo ao NidusCare!",
                description = "Seu assistente completo para o gerenciamento da saúde. Organize medicamentos, vacinas, estilo de vida e muito mais em um só lugar.",
                imageRes = R.drawable.ic_logo
            ),
            OnboardingPage(
                title = "Gestão de Saúde Simplificada",
                description = "Use a câmera para escanear receitas ou adicione medicamentos manualmente. Nunca mais perca um horário com nossos lembretes inteligentes.",
                imageRes = R.drawable.ic_onborad_scan
            ),
            OnboardingPage(
                title = "Acompanhe o Bem-Estar Diário",
                description = "Monitore hidratação, calorias, sono e peso. Inicie um cronômetro para suas atividades físicas e registre tudo sem esforço.",
                imageRes = R.drawable.ic_onboard_timelinenew
            ),
            OnboardingPage(
                title = "Cuidado em Equipe, Sem Dúvidas",
                description = "Convide familiares e cuidadores para gerenciar a saúde em conjunto, com alertas de emergência e compartilhamento de informações.",
                imageRes = R.drawable.ic_onboard_cuidado
            ),
            OnboardingPage(
                title = "✨ Um Assistente que Pensa por Você",
                description = "Nossa IA analisa os dados e gera insights, responde suas dúvidas sobre saúde e até calcula as calorias de uma refeição a partir de uma foto (Premium).",
                imageRes = R.drawable.ic_onbord_ai
            ),
            OnboardingPage(
                title = "Desbloqueie o Poder do Premium",
                description = "Acesse dependentes e cuidadores ilimitados, análises preditivas, relatórios avançados e todas as nossas ferramentas de IA com o plano Premium.",
                imageRes = R.drawable.ic_premium_onboard
            )
        )
    }
}